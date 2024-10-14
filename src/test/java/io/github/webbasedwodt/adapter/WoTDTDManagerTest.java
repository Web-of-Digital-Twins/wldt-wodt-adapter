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

import io.github.webbasedwodt.adapter.testdouble.PlatformManagementInterfaceReaderTestDouble;
import io.github.webbasedwodt.application.component.DTDManager;
import io.github.webbasedwodt.integration.wldt.LampDTSemantics;
import io.github.webbasedwodt.model.dtd.DTVersion;
import io.github.webbasedwodt.model.ontology.DigitalTwinSemantics;
import io.github.webbasedwodt.model.ontology.WoDTVocabulary;
import it.wldt.core.state.DigitalTwinStateAction;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.core.state.DigitalTwinStateRelationship;
import it.wldt.exception.WldtDigitalTwinStateException;
import org.eclipse.ditto.json.JsonKey;
import org.eclipse.ditto.wot.model.Action;
import org.eclipse.ditto.wot.model.Properties;
import org.eclipse.ditto.wot.model.SingleRootFormElementOp;
import org.eclipse.ditto.wot.model.ThingDescription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link WoTDTDManager}.
 */
class WoTDTDManagerTest {
    private final DigitalTwinSemantics dtSemantics = new LampDTSemantics();
    private final DTVersion dtVersion = new DTVersion(1, 0, 0);
    private final DigitalTwinStateProperty<?> dtProperty = new DigitalTwinStateProperty<>("luminosity", 100);
    private final DigitalTwinStateRelationship<?> dtRelationship = new DigitalTwinStateRelationship<>("isInRoom", "");
    private final DigitalTwinStateAction dtAction = new DigitalTwinStateAction("switch", "", "");
    private DTDManager dtdManager;

    WoTDTDManagerTest() throws WldtDigitalTwinStateException {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    @BeforeEach
    public void init() {
        this.dtdManager = new WoTDTDManager(
                URI.create("http://example:3000/dt"),
                this.dtVersion,
                this.dtSemantics,
                "lampPA",
                new PlatformManagementInterfaceReaderTestDouble());
    }

    @Test
    @DisplayName("A DTD should carry the mandatory metadata even when empty")
    void testDTDMetadata() {
        final ThingDescription thingDescription = this.dtdManager.getDTD();
        final Optional<Properties> properties = thingDescription.getProperties();
        assertTrue(properties.isPresent());
        assertTrue(thingDescription.getAtType().isPresent());
        assertTrue(thingDescription.getId().isPresent());
        assertTrue(thingDescription.getActions().isPresent());
        assertTrue(thingDescription.getForms().isPresent());
        assertTrue(thingDescription.getForms().get()
                .stream()
                .anyMatch(form -> form.getOp().equals(SingleRootFormElementOp.OBSERVEALLPROPERTIES)));
        assertTrue(thingDescription.getWrappedObject().asObject()
                .getKeys().contains(JsonKey.of(WoDTVocabulary.PHYSICAL_ASSET_ID.getUri())));
        assertTrue(thingDescription.getVersion().isPresent());
        assertTrue(thingDescription.getLinks().isPresent());
        assertTrue(thingDescription.getLinks().get().stream().anyMatch(link ->
                "type".equals(link.getRel().get())));
        assertTrue(thingDescription.getLinks().get().stream().anyMatch(link ->
                link.getRel().get().equals(WoDTVocabulary.DTKG.getUri())));
    }

    @Test
    @DisplayName("A property should contain the mandatory information")
    void testCorrectInformationOnProperty() {
        this.dtdManager.addProperty(this.dtProperty);
        final ThingDescription thingDescription = this.dtdManager.getDTD();
        generalTestOnThingDescriptionProperty(thingDescription, this.dtProperty);
        final Optional<org.eclipse.ditto.wot.model.Property> property =
                thingDescription.getProperties().flatMap(properties -> properties.getProperty(this.dtProperty.getKey()));
        assertTrue(property.isPresent());
        assertTrue(property.get().getWrappedObject().asObject()
                .getKeys().contains(JsonKey.of(WoDTVocabulary.AUGMENTED_INTERACTION.getUri())));
        assertTrue(property.get().getWrappedObject()
                .stream()
                .anyMatch(jsonfield ->
                        jsonfield.getKey().toString().equals(WoDTVocabulary.AUGMENTED_INTERACTION.getUri())
                        && !jsonfield.getValue().asBoolean()));
    }

    @Test
    @DisplayName("It should be possible to delete a property")
    void testDeletionOfProperty() {
        this.dtdManager.addProperty(this.dtProperty);
        assertTrue(this.dtdManager.removeProperty(this.dtProperty));
        final ThingDescription thingDescription = this.dtdManager.getDTD();
        assertFalse(thingDescription.getProperties().get().getProperty(this.dtProperty.getKey()).isPresent());
    }

    @Test
    @DisplayName("A relationship should be represented in the DTD with the mandatory information")
    void testCorrectInformationOnRelationship() {
        this.dtdManager.addRelationship(this.dtRelationship);
        final ThingDescription thingDescription = this.dtdManager.getDTD();
        generalTestOnThingDescriptionProperty(thingDescription, this.dtRelationship);
    }

    @Test
    @DisplayName("It should be possible to delete a relationship")
    void testDeletionOfRelationship() {
        this.dtdManager.addRelationship(this.dtRelationship);
        assertTrue(this.dtdManager.removeRelationship(this.dtRelationship));
        final ThingDescription thingDescription = this.dtdManager.getDTD();
        assertFalse(thingDescription.getProperties().get().containsKey(this.dtRelationship.getName()));
    }

    @Test
    @DisplayName("It should be possible to add an action")
    void testCorrectInformationOnAction() {
        this.dtdManager.addAction(dtAction);
        final ThingDescription thingDescription = this.dtdManager.getDTD();
        assertTrue(thingDescription.getActions().get().containsKey(dtAction.getKey()));
        final Action action = thingDescription.getActions().get().getAction(dtAction.getKey()).get();
        assertFalse(action.getForms().isEmpty());
        assertTrue(action.getWrappedObject().asObject().getKeys()
                .contains(JsonKey.of(WoDTVocabulary.DOMAIN_TAG.getUri())));
        assertTrue(action.getWrappedObject().asObject().getKeys()
                .contains(JsonKey.of(WoDTVocabulary.AUGMENTED_INTERACTION.getUri())));
        assertTrue(action.getWrappedObject().stream().anyMatch(jsonfield ->
                jsonfield.getKey().toString().equals(WoDTVocabulary.AUGMENTED_INTERACTION.getUri())
                    && !jsonfield.getValue().asBoolean()));
    }

    @Test
    @DisplayName("It should be possible to delete an action")
    void testDeletionOfAction() {
        this.dtdManager.addAction(dtAction);
        assertTrue(this.dtdManager.removeAction(dtAction));
        final ThingDescription thingDescription = this.dtdManager.getDTD();
        assertFalse(thingDescription.getActions().get().containsKey(dtAction.getKey()));
    }

    @Test
    @DisplayName("The DTDManager should be able to obtain the Platforms to which it is registered and link "
            + "them to the descriptor")
    void addPlatform() {
        final ThingDescription thingDescription = this.dtdManager.getDTD();
        assertTrue(thingDescription.getLinks().isPresent());
        assertTrue(thingDescription.getLinks().get().stream().anyMatch(link ->
                link.getRel().get().equals(WoDTVocabulary.REGISTERED_TO_PLATFORM.getUri())));
    }

    void generalTestOnThingDescriptionProperty(
            final ThingDescription thingDescription,
            final DigitalTwinStateProperty<?> property
    ) {
        final Optional<Properties> properties = thingDescription.getProperties();
        assertTrue(properties.isPresent());
        assertTrue(properties.get().containsKey(property.getKey()));
        assertTrue(properties.get().getProperty(property.getKey()).get().isReadOnly());
        assertTrue(properties.get().getProperty(property.getKey()).get().getWrappedObject()
                .asObject().getKeys().contains(JsonKey.of(WoDTVocabulary.DOMAIN_TAG.getUri())));
    }

    void generalTestOnThingDescriptionProperty(
            final ThingDescription thingDescription,
            final DigitalTwinStateRelationship<?> relationship
    ) {
        final Optional<Properties> properties = thingDescription.getProperties();
        assertTrue(properties.isPresent());
        assertTrue(properties.get().containsKey(relationship.getName()));
        assertTrue(properties.get().getProperty(relationship.getName()).get().isReadOnly());
        assertTrue(properties.get().getProperty(relationship.getName()).get().getWrappedObject()
                .asObject().getKeys().contains(JsonKey.of(WoDTVocabulary.DOMAIN_TAG.getUri())));
    }
}
