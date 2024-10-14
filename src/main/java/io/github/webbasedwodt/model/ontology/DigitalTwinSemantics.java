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

package io.github.webbasedwodt.model.ontology;

import io.github.webbasedwodt.model.ontology.rdf.RdfClass;
import io.github.webbasedwodt.model.ontology.rdf.RdfUnSubjectedTriple;
import io.github.webbasedwodt.model.ontology.rdf.RdfUriResource;
import it.wldt.core.state.DigitalTwinStateAction;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.core.state.DigitalTwinStateRelationship;
import it.wldt.core.state.DigitalTwinStateRelationshipInstance;

import java.util.List;
import java.util.Optional;

/**
 * This interface models the entire semantics of the Digital Twin useful to create the HWoDT Uniform Interface.
 * This interface once implemented, it has the responsibility of obtaining the Digital Twin classes, the domain tags,
 * and map raw data to RDF data, considering the domain ontology.
 * This interface is the one that DT Developer must implement.
 */
public interface DigitalTwinSemantics {
    /**
     * Obtain the Digital Twin types, expressed as RDF Classes.
     * The Digital Twin could have more than one type.
     * @return the list of RDF Classes that represent the domain-oriented type of the Digital Twin.
     */
    List<RdfClass> getDigitalTwinTypes();

    /**
     * Get the Domain Tag of a Digital Twin property.
     * If the Digital Twin has no properties, return an empty {@link Optional}.
     * @param property the Digital Twin property for which obtain the corresponding Domain Tag.
     * @return the Domain Tag of the Digital Twin property.
     */
    Optional<RdfUriResource> getDomainTag(DigitalTwinStateProperty<?> property);

    /**
     * Get the Domain Tag of a Digital Twin relationship.
     * If the Digital Twin has no relationships, return an empty {@link Optional}.
     * @param relationship the Digital Twin relationship for which obtain the corresponding Domain Tag.
     * @return the Domain Tag of the Digital Twin relationship.
     */
    Optional<RdfUriResource> getDomainTag(DigitalTwinStateRelationship<?> relationship);

    /**
     * Get the Domain Tag of a Digital Twin action.
     * If the Digital Twin has no actions, return an empty {@link Optional}.
     * @param action the Digital Twin action for which obtain the corresponding Domain Tag.
     * @return the Domain Tag of the Digital Twin action.
     */
    Optional<RdfUriResource> getDomainTag(DigitalTwinStateAction action);

    /**
     * Map the value of a Digital Twin property in a set of RDF triples.
     * @param property the property to map data.
     * @return the list of triples, if the property is mapped.
     */
    Optional<List<RdfUnSubjectedTriple>> mapData(DigitalTwinStateProperty<?> property);

    /**
     * Map a Digital Twin relationship instance in a set of RDF triples.
     * @param relationshipInstance the relationship instance to map.
     * @return the list of triples, if the relationship is mapped.
     */
    Optional<List<RdfUnSubjectedTriple>> mapData(DigitalTwinStateRelationshipInstance<?> relationshipInstance);
}
