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

import io.github.webbasedwodt.model.ontology.DTOntology;
import io.github.webbasedwodt.model.ontology.Individual;
import io.github.webbasedwodt.model.ontology.Literal;
import io.github.webbasedwodt.model.ontology.Node;
import io.github.webbasedwodt.model.ontology.Property;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Ontology for the {@link LampDT}.
 */
public final class LampDTOntology implements DTOntology {
    private static Map<String, Pair<String, String>> propertyMap = Map.of(
            "is-on-property-key", Pair.of(
                    "https://lampontology.com/ontology#isOn",
                    "https://www.w3.org/2001/XMLSchema#boolean"
            )
    );
    private static Map<String, Pair<String, String>> relationshipMap = Map.of(
            "located-inside", Pair.of(
                    "https://lampontology/ontology#isLocatedInside",
                    "https://homeontology/ontology#Room"
            )
    );

    @Override
    public String getDigitalTwinType() {
        return "https://lampontology.com/ontology#Lamp";
    }

    @Override
    public Optional<Property> obtainProperty(final String rawProperty) {
        final Map<String, Pair<String, String>> predicates = new HashMap<>(propertyMap);
        relationshipMap.forEach((key, value) -> predicates.merge(key, value, (oldValue, newValue) -> newValue));
        if (predicates.containsKey(rawProperty)) {
            return Optional.of(new Property(predicates.get(rawProperty).getLeft()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> obtainPropertyValueType(final String rawProperty) {
        final Map<String, Pair<String, String>> predicates = new HashMap<>(propertyMap);
        relationshipMap.forEach((key, value) -> predicates.merge(key, value, (oldValue, newValue) -> newValue));
        if (predicates.containsKey(rawProperty)) {
            return Optional.of(predicates.get(rawProperty).getRight());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public <T> Optional<Pair<Property, Node>> convertPropertyValue(final String rawProperty, final T value) {
        if (propertyMap.containsKey(rawProperty)) {
            return Optional.of(Pair.of(new Property(propertyMap.get(rawProperty).getLeft()), new Literal<>(value)));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Pair<Property, Individual>> convertRelationship(
            final String rawRelationship,
            final String targetUri
    ) {
        if (relationshipMap.containsKey(rawRelationship)) {
            return Optional.of(
                    Pair.of(new Property(relationshipMap.get(rawRelationship).getLeft()), new Individual(targetUri))
            );
        } else {
            return Optional.empty();
        }
    }
}
