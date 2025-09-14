/*
 * Copyright (c) 2023. Andrea Giulianelli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.webbasedwodt.adapter;

import io.github.webbasedwodt.application.component.DTKGEngine;
import io.github.webbasedwodt.application.component.observer.DTKGObserver;
import io.github.webbasedwodt.model.ontology.DigitalTwinSemantics;
import io.github.webbasedwodt.model.ontology.rdf.RdfBlankNode;
import io.github.webbasedwodt.model.ontology.rdf.RdfLiteral;
import io.github.webbasedwodt.model.ontology.WoDTVocabulary;
import io.github.webbasedwodt.model.ontology.rdf.RdfUnSubjectedTriple;
import io.github.webbasedwodt.model.ontology.rdf.RdfUriResource;
import it.wldt.core.state.DigitalTwinStateAction;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.core.state.DigitalTwinStateRelationshipInstance;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.shared.Lock;
import org.apache.jena.vocabulary.RDF;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * This class provides an implementation of the {@link io.github.webbasedwodt.application.component.DTKGEngine} using
 * Apache Jena.
 */
final class JenaDTKGEngine implements DTKGEngine {
    private final DigitalTwinSemantics digitalTwinSemantics;
    private final Model dtkgModel;
    private final Resource digitalTwinResource;
    private final List<DTKGObserver> observers;
    private final Set<String> propertyKeys;

    /**
     * Default constructor.
     * @param digitalTwinUri the uri of the Digital Twin for which this class creates the DTKG
     * @param digitalTwinSemantics the digital twin semantics used for the creation of the rdf graph
     */
    JenaDTKGEngine(final URI digitalTwinUri, final DigitalTwinSemantics digitalTwinSemantics) {
        this.digitalTwinSemantics = digitalTwinSemantics;
        this.propertyKeys = new HashSet<>();
        this.dtkgModel = ModelFactory.createDefaultModel();
        this.digitalTwinResource = this.dtkgModel.createResource(digitalTwinUri.toString());
        this.digitalTwinSemantics.getDigitalTwinTypes().forEach(type ->
            this.digitalTwinResource.addProperty(
                    RDF.type,
                    this.dtkgModel.createResource(type.getUri().map(URI::toString).orElse(""))
            )
        );
        this.observers = new ArrayList<>();
    }

    @Override
    public void removeDigitalTwin() {
        this.writeModel(Model::removeAll);
    }

    @Override
    public void addDigitalTwinProperty(final DigitalTwinStateProperty<?> property) {
        if (propertyKeys.contains(property.getKey())) {
            throw new IllegalStateException("Property already present. Maybe you want to update it!");
        }
        final Optional<List<RdfUnSubjectedTriple>> mappedData = this.digitalTwinSemantics.mapData(property);
        if (mappedData.isPresent()) {
            this.writeModel(model ->
                addTriples(this.dtkgModel, this.digitalTwinResource, mappedData.get())
            );
            this.propertyKeys.add(property.getKey());
        } else {
            throw new IllegalArgumentException("Mapping for property not present.");
        }
    }

    @Override
    public void updateDigitalTwinProperty(
            final DigitalTwinStateProperty<?> property,
            final DigitalTwinStateProperty<?> oldProperty
    ) {
        final Optional<List<RdfUnSubjectedTriple>> oldMappedData = this.digitalTwinSemantics.mapData(oldProperty);
        final Optional<List<RdfUnSubjectedTriple>> mappedData = this.digitalTwinSemantics.mapData(property);

        if (oldMappedData.isPresent() && mappedData.isPresent()) {
            this.writeModel(model -> {
                removeTriples(this.digitalTwinResource, oldMappedData.get());
                addTriples(this.dtkgModel, this.digitalTwinResource, mappedData.get());
            });
        } else {
            throw new IllegalArgumentException("Mapping for properties not present.");
        }
    }

    @Override
    public boolean removeProperty(final DigitalTwinStateProperty<?> property) {
        final Optional<List<RdfUnSubjectedTriple>> mappedData = this.digitalTwinSemantics.mapData(property);
        if (propertyKeys.contains(property.getKey()) && mappedData.isPresent()) {
            this.writeModel(model ->
                removeTriples(this.digitalTwinResource, mappedData.get())
            );
            this.propertyKeys.remove(property.getKey());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void addRelationship(final DigitalTwinStateRelationshipInstance<?> relationshipInstance) {
        final Optional<List<RdfUnSubjectedTriple>> mappedData = this.digitalTwinSemantics.mapData(relationshipInstance);
        if (mappedData.isPresent()) {
            this.writeModel(model ->
                addTriples(this.dtkgModel, this.digitalTwinResource, mappedData.get())
            );
        } else {
            throw new IllegalArgumentException("Mapping for relationship not present.");
        }
    }

    @Override
    public boolean removeRelationship(final DigitalTwinStateRelationshipInstance<?> relationshipInstance) {
        final Optional<List<RdfUnSubjectedTriple>> mappedData = this.digitalTwinSemantics.mapData(relationshipInstance);
        if (mappedData.isPresent()) {
            this.writeModel(model ->
                removeTriples(this.digitalTwinResource, mappedData.get())
            );
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void addAction(final DigitalTwinStateAction action) {
        this.writeModel(model ->
            this.digitalTwinResource.addLiteral(
                this.dtkgModel.createProperty(WoDTVocabulary.AVAILABLE_ACTION_ID.getUri()),
                action.getKey()
            )
        );
    }

    @Override
    public boolean removeAction(final DigitalTwinStateAction action) {
        if (this.dtkgModel.containsLiteral(
            this.digitalTwinResource,
            this.dtkgModel.getProperty(WoDTVocabulary.AVAILABLE_ACTION_ID.getUri()),
            action.getKey())
        ) {
            this.writeModel(model ->
                model.remove(
                    this.digitalTwinResource,
                    model.getProperty(WoDTVocabulary.AVAILABLE_ACTION_ID.getUri()),
                    model.createTypedLiteral(action.getKey())
                )
            );
            return true;
        }
        return false;
    }

    @Override
    public String getCurrentDigitalTwinKnowledgeGraph() {
        try {
            this.dtkgModel.enterCriticalSection(Lock.READ);
            return RDFWriter.create().lang(Lang.TTL).source(this.dtkgModel).asString();
        } finally {
            this.dtkgModel.leaveCriticalSection();
        }
    }

    @Override
    public void addDTKGObserver(final DTKGObserver observer) {
        this.observers.add(observer);
    }

    @Override
    public void commitUpdateTransaction() {
        this.notifyObservers();
    }

    private void notifyObservers() {
        final String currentDtkg = this.getCurrentDigitalTwinKnowledgeGraph();
        this.observers.forEach(observer -> observer.notifyNewDTKG(currentDtkg));
    }

    private void addTriples(final Model model, final Resource resourceToAdd, final List<RdfUnSubjectedTriple> tripleList) {
        tripleList.forEach(triple -> {
            final String predicateUri = triple.getTriplePredicate().getUri().map(URI::toString).orElse("");
            final var property = model.createProperty(predicateUri);
            if (triple.getTripleObject() instanceof RdfBlankNode) {
                final Resource blankNode = model.createResource(
                        new AnonId(((RdfBlankNode) triple.getTripleObject()).getBlankNodeId()));
                this.addTriples(model, blankNode, ((RdfBlankNode) triple.getTripleObject()).getPredicates());
                resourceToAdd.addProperty(property, blankNode);
            } else if (triple.getTripleObject() instanceof RdfLiteral<?>) {
                resourceToAdd.addLiteral(property, ((RdfLiteral<?>) triple.getTripleObject()).getValue());
            } else if (triple.getTripleObject() instanceof RdfUriResource) {
                resourceToAdd.addProperty(
                    property,
                        model.createResource(((RdfUriResource) triple.getTripleObject())
                        .getUri()
                        .map(URI::toString)
                        .orElse("")
                    )
                );
            }
        });
    }

    private void removeTriples(final Resource resource, final List<RdfUnSubjectedTriple> tripleList) {
        tripleList.forEach(triple -> {
            if (triple.getTripleObject() instanceof RdfBlankNode) {
                final Model modelToRemove = ModelFactory.createDefaultModel();
                final Resource resourceOfTheModelToRemove = modelToRemove.createResource(resource.getURI());
                this.addTriples(modelToRemove, resourceOfTheModelToRemove, List.of(triple));
                this.dtkgModel.remove(modelToRemove);
            } else if (triple.getTripleObject() instanceof RdfLiteral<?>) {
                this.dtkgModel.remove(
                    resource,
                    this.dtkgModel.getProperty(triple.getTriplePredicate().getUri().map(URI::toString).orElse("")),
                    this.dtkgModel.createTypedLiteral(((RdfLiteral<?>) triple.getTripleObject()).getValue())
                );
            } else if (triple.getTripleObject() instanceof RdfUriResource) {
                this.dtkgModel.remove(
                    resource,
                    this.dtkgModel.getProperty(triple.getTriplePredicate().getUri().map(URI::toString).orElse("")),
                    this.dtkgModel.getResource(((RdfUriResource) triple.getTripleObject()).getUri().map(URI::toString).orElse(""))
                );
            }
        });
    }

    private void writeModel(final Consumer<Model> modelConsumer) {
        this.dtkgModel.enterCriticalSection(Lock.WRITE);
        modelConsumer.accept(this.dtkgModel);
        this.dtkgModel.leaveCriticalSection();
    }
}
