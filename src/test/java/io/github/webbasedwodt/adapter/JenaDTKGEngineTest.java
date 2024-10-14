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

import io.github.webbasedwodt.integration.wldt.LampDTSemantics;
import io.github.webbasedwodt.utils.TestingUtils;
import it.wldt.core.state.DigitalTwinStateAction;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.core.state.DigitalTwinStateRelationshipInstance;
import it.wldt.exception.WldtDigitalTwinStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link JenaDTKGEngine}.
 */
class JenaDTKGEngineTest {
    private final List<DigitalTwinStateProperty<?>> properties = List.of(
        new DigitalTwinStateProperty<>("luminosity", 100),
        new DigitalTwinStateProperty<>("illuminance", 50)
    );
    private final List<DigitalTwinStateProperty<?>> modifiedProperties = List.of(
        new DigitalTwinStateProperty<>("luminosity", 0.20),
        new DigitalTwinStateProperty<>("illuminance", 0.10)
    );
    private final List<DigitalTwinStateRelationshipInstance<?>> relationships = List.of(
        new DigitalTwinStateRelationshipInstance<>("isInRoom", "http://exampleRoomDT.it", "isInRoom-http://exampleRoomDT.it")
    );
    private final List<DigitalTwinStateRelationshipInstance<?>> modifiedRelationships = List.of(
            new DigitalTwinStateRelationshipInstance<>("isInRoom", "http://roomDT.it", "isInRoom-http://exampleRoomDT.it")
    );

    private final List<DigitalTwinStateAction> actionsList = List.of(new DigitalTwinStateAction("switch", "status.switch", ""));

    private JenaDTKGEngine dtkgEngine;

    JenaDTKGEngineTest() throws WldtDigitalTwinStateException {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    @BeforeEach
    public void init() {
        this.dtkgEngine = new JenaDTKGEngine(URI.create("http://example.com/dt"), new LampDTSemantics());
        this.properties.forEach(property ->
                this.dtkgEngine.addDigitalTwinProperty(property)
        );
        this.relationships.forEach(relationship ->
                this.dtkgEngine.addRelationship(relationship)
        );
        this.actionsList.forEach(action -> this.dtkgEngine.addAction(action));
    }

    @Test
    @DisplayName("It should be possible to obtain the turtle representation of the Digital Twin")
    void testDTKGCreation() {
        assertEquals(
            TestingUtils.readResourceFile("DTKGWithRelationshipsTurtle.ttl").orElse(""),
            this.dtkgEngine.getCurrentDigitalTwinKnowledgeGraph()
        );
    }

    @Test
    @DisplayName("It should be possible to update a Property without seeing duplicates in the generated DTKG")
    void testDTKGPropertyUpdateDuplication() {
        for (int i = 0; i < modifiedProperties.size(); i++) {
            this.dtkgEngine.updateDigitalTwinProperty(modifiedProperties.get(i), properties.get(i));
        }
        for (int i = 0; i < modifiedProperties.size(); i++) {
            this.dtkgEngine.updateDigitalTwinProperty(properties.get(i), modifiedProperties.get(i));
        }
        assertEquals(
                TestingUtils.readResourceFile("DTKGWithRelationshipsTurtle.ttl").orElse(""),
                this.dtkgEngine.getCurrentDigitalTwinKnowledgeGraph()
        );
    }

    @Test
    @DisplayName("An update must update only the mapped data without touching other data")
    void testDTKGPropertyUpdate() {
        for (int i = 0; i < modifiedRelationships.size(); i++) {
            this.dtkgEngine.removeRelationship(relationships.get(i));
            this.dtkgEngine.addRelationship(modifiedRelationships.get(i));
        }
        for (int i = 0; i < modifiedProperties.size(); i++) {
            this.dtkgEngine.updateDigitalTwinProperty(modifiedProperties.get(i), properties.get(i));
        }
        assertEquals(
                TestingUtils.readResourceFile("DTKGWithRelationshipsTurtleUpdated.ttl").orElse(""),
                this.dtkgEngine.getCurrentDigitalTwinKnowledgeGraph()
        );
    }

    @Test
    @DisplayName("When the Digital Twin is deleted, then the DTKG should be empty")
    void testDTKGDigitalTwinDeletion() {
        this.dtkgEngine.removeDigitalTwin();
        assertTrue(this.dtkgEngine.getCurrentDigitalTwinKnowledgeGraph().isEmpty());
    }

    @Test
    @DisplayName("It should be possible to delete an existing relationship")
    void testDTKGRelationshipDeletion() {
        assertTrue(this.dtkgEngine.removeRelationship(this.relationships.get(0)));
    }

    @Test
    @DisplayName("The request of deletion of a non-existent relationship should be correctly handled")
    void testDTKGNotExistentRelationshipDeletion() {
        assertFalse(this.dtkgEngine.removeRelationship(
            new DigitalTwinStateRelationshipInstance<>("not-existent-relationship", "target", "")
        ));
    }

    @Test
    @DisplayName("It should be possible to delete an existent action")
    void testDTKGActionDeletion() {
        assertTrue(this.dtkgEngine.getCurrentDigitalTwinKnowledgeGraph().contains(this.actionsList.get(0).getKey()));
        assertTrue(this.dtkgEngine.removeAction(this.actionsList.get(0)));
        assertFalse(this.dtkgEngine.getCurrentDigitalTwinKnowledgeGraph().contains(this.actionsList.get(0).getKey()));
    }

    @Test
    @DisplayName("The request of deletion of a non-existent action should be correctly handled")
    void testDTKGNotExistentActionDeletion() throws WldtDigitalTwinStateException {
        assertFalse(this.dtkgEngine.removeAction(new DigitalTwinStateAction("not-existent", "", "")));
    }

    @Test
    @DisplayName("The dtkg engine delete the correct statement when deleting an action: adding and removing an action"
            + "will result in the previous state")
    void testDTKGActionAdditionAndDeletion() {
        this.dtkgEngine.removeAction(actionsList.get(0));
        this.dtkgEngine.addAction(actionsList.get(0));
        assertEquals(
                TestingUtils.readResourceFile("DTKGWithRelationshipsTurtle.ttl").orElse(""),
                this.dtkgEngine.getCurrentDigitalTwinKnowledgeGraph()
        );
    }
}
