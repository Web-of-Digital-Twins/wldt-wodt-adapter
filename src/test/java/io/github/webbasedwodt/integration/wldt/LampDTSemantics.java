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

package io.github.webbasedwodt.integration.wldt;

import io.github.webbasedwodt.model.ontology.DigitalTwinSemantics;
import io.github.webbasedwodt.model.ontology.rdf.RdfBlankNode;
import io.github.webbasedwodt.model.ontology.rdf.RdfClass;
import io.github.webbasedwodt.model.ontology.rdf.RdfIndividual;
import io.github.webbasedwodt.model.ontology.rdf.RdfLiteral;
import io.github.webbasedwodt.model.ontology.rdf.RdfProperty;
import io.github.webbasedwodt.model.ontology.rdf.RdfUnSubjectedTriple;
import io.github.webbasedwodt.model.ontology.rdf.RdfUriResource;
import it.wldt.core.state.DigitalTwinStateAction;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.core.state.DigitalTwinStateRelationship;
import it.wldt.core.state.DigitalTwinStateRelationshipInstance;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Semantics for the {@link LampDT}.
 */
public final class LampDTSemantics implements DigitalTwinSemantics {
    private static final List<RdfClass> DT_CLASSES = List.of(
        new RdfClass(URI.create("https://saref.etsi.org/core/Actuator")),
        new RdfClass(URI.create("https://w3id.org/rec/Lamp"))
    );

    private static final Map<String, RdfUriResource> PROPERTIES_DOMAIN_TAG = Map.of(
    "luminosity", new RdfUriResource(URI.create("https://purl.org/onto/LuminosityFlux")),
    "illuminance", new RdfUriResource(URI.create("https://purl.org/onto/Illuminance"))
    );

    private static final Map<String, RdfUriResource> RELATIONSHIPS_DOMAIN_TAG = Map.of(
    "isInRoom", new RdfUriResource(URI.create("https://brickschema.org/schema/Brick#hasLocation"))
    );

    private static final Map<String, RdfUriResource> ACTIONS_DOMAIN_TAG = Map.of(
    "switch", new RdfUriResource(URI.create("https://purl.org/onto/SwitchCommand"))
    );

    @Override
    public List<RdfClass> getDigitalTwinTypes() {
        return DT_CLASSES;
    }

    @Override
    public Optional<RdfUriResource> getDomainTag(final DigitalTwinStateProperty<?> property) {
        return getOptionalFromMap(PROPERTIES_DOMAIN_TAG, property.getKey());
    }

    @Override
    public Optional<RdfUriResource> getDomainTag(final DigitalTwinStateRelationship<?> relationship) {
        return getOptionalFromMap(RELATIONSHIPS_DOMAIN_TAG, relationship.getName());
    }

    @Override
    public Optional<RdfUriResource> getDomainTag(final DigitalTwinStateAction action) {
        return getOptionalFromMap(ACTIONS_DOMAIN_TAG, action.getKey());
    }

    @Override
    public Optional<List<RdfUnSubjectedTriple>> mapData(final DigitalTwinStateProperty<?> property) {
        if ("luminosity".equals(property.getKey())) {
            return Optional.of(List.of(
                    new RdfUnSubjectedTriple(
                            new RdfProperty(URI.create("https://saref.etsi.org/core/hasProperty")),
                            new RdfIndividual(URI.create("https://purl.org/onto/LuminosityFlux"))
                    ),
                    new RdfUnSubjectedTriple(
                            new RdfProperty(URI.create("https://saref.etsi.org/core/hasPropertyValue")),
                            new RdfBlankNode("luminosityValue", List.of(
                                    new RdfUnSubjectedTriple(
                                            new RdfProperty(URI.create("https://saref.etsi.org/core/hasValue")),
                                            new RdfLiteral<>(Double.valueOf(property.getValue().toString()))
                                    ),
                                    new RdfUnSubjectedTriple(
                                            new RdfProperty(URI.create("https://saref.etsi.org/core/isMeasuredIn")),
                                            new RdfIndividual(URI.create("https://qudt.org/2.1/vocab/unit/LM"))
                                    ),
                                    new RdfUnSubjectedTriple(
                                            new RdfProperty(URI.create("https://saref.etsi.org/core/isValueOfProperty")),
                                            new RdfIndividual(URI.create("https://purl.org/onto/LuminosityFlux"))
                                    )
                            ))
                    )
            ));
        } else if ("illuminance".equals(property.getKey())) {
            return Optional.of(List.of(
                    new RdfUnSubjectedTriple(
                            new RdfProperty(URI.create("https://saref.etsi.org/core/hasProperty")),
                            new RdfIndividual(URI.create("https://purl.org/onto/Illuminance"))
                    ),
                    new RdfUnSubjectedTriple(
                            new RdfProperty(URI.create("https://saref.etsi.org/core/hasPropertyValue")),
                            new RdfBlankNode("illuminanceValue", List.of(
                                    new RdfUnSubjectedTriple(
                                            new RdfProperty(URI.create("https://saref.etsi.org/core/hasValue")),
                                            new RdfLiteral<>(Double.valueOf(property.getValue().toString()))
                                    ),
                                    new RdfUnSubjectedTriple(
                                            new RdfProperty(URI.create("https://saref.etsi.org/core/isValueOfProperty")),
                                            new RdfIndividual(URI.create("https://purl.org/onto/Illuminance"))
                                    )
                            ))
                    )
            ));
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<RdfUnSubjectedTriple>> mapData(final DigitalTwinStateRelationshipInstance<?> relationshipInstance) {
        if ("isInRoom".equals(relationshipInstance.getRelationshipName())) {
            return Optional.of(List.of(
                new RdfUnSubjectedTriple(
                    new RdfProperty(URI.create("https://brickschema.org/schema/Brick#hasLocation")),
                    new RdfIndividual(URI.create(relationshipInstance.getTargetId().toString()))
                )
            ));
        }
        return Optional.empty();
    }

    private <T> Optional<T> getOptionalFromMap(final Map<String, T> map, final String key) {
        if (map.containsKey(key)) {
            return Optional.of(map.get(key));
        }
        return Optional.empty();
    }
}
