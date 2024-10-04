/*
 * Copyright (c) 2024. Andrea Giulianelli
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

import io.github.webbasedwodt.application.component.DTDManager;
import io.github.webbasedwodt.application.component.PlatformManagementInterfaceReader;
import io.github.webbasedwodt.model.dtd.DTVersion;
import io.github.webbasedwodt.model.ontology.DTOntology;
import io.github.webbasedwodt.model.ontology.WoDTVocabulary;
import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonObjectBuilder;

import org.eclipse.ditto.wot.model.Action;
import org.eclipse.ditto.wot.model.ActionFormElement;
import org.eclipse.ditto.wot.model.ActionForms;
import org.eclipse.ditto.wot.model.Actions;
import org.eclipse.ditto.wot.model.AtContext;
import org.eclipse.ditto.wot.model.AtType;
import org.eclipse.ditto.wot.model.BaseLink;
import org.eclipse.ditto.wot.model.IRI;
import org.eclipse.ditto.wot.model.Link;
import org.eclipse.ditto.wot.model.Properties;
import org.eclipse.ditto.wot.model.Property;
import org.eclipse.ditto.wot.model.RootFormElement;
import org.eclipse.ditto.wot.model.Security;
import org.eclipse.ditto.wot.model.SecurityDefinitions;
import org.eclipse.ditto.wot.model.SecurityScheme;
import org.eclipse.ditto.wot.model.SingleRootFormElementOp;
import org.eclipse.ditto.wot.model.SingleUriAtContext;
import org.eclipse.ditto.wot.model.ThingDescription;
import org.eclipse.ditto.wot.model.Version;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class provide an implementation of the {@link io.github.webbasedwodt.application.component.DTDManager} using
 * a WoT Thing Description to implement the Digital Twin Description.
 */
public final class WoTDTDManager implements DTDManager {
    private static final String MODEL_VERSION = "1.0.0";
    private static final String AVAILABLE_ACTIONS_PROPERTY = "availableActions";
    private static final String THING_MODEL_URL = "https://raw.githubusercontent.com/Web-of-Digital-Twins/"
            + "dtd-conceptual-model/refs/heads/main/implementations/wot/dtd-thing-model.tm.jsonld";
    private final URI digitalTwinUri;
    private final DTVersion dtVersion;
    private final String physicalAssetId;
    private final DTOntology ontology;
    private final PlatformManagementInterfaceReader platformManagementInterfaceReader;
    private final Map<String, Property> properties;
    private final Map<String, Property> relationships;
    private final Map<String, Action> actions;

    /**
     * Default constructor.
     * @param digitalTwinUri the uri of the WoDT Digital Twin
     * @param dtVersion the version of the dt
     * @param ontology the ontology used to obtain the semantics
     * @param physicalAssetId the id of the associated physical asset
     * @param platformManagementInterfaceReader the platform management interface reader reference
     */
    WoTDTDManager(final URI digitalTwinUri,
                  final DTVersion dtVersion,
                  final DTOntology ontology,
                  final String physicalAssetId,
                  final PlatformManagementInterfaceReader platformManagementInterfaceReader) {
        this.digitalTwinUri = digitalTwinUri;
        this.dtVersion = dtVersion;
        this.ontology = ontology;
        this.physicalAssetId = physicalAssetId;
        this.platformManagementInterfaceReader = platformManagementInterfaceReader;
        this.properties = new HashMap<>();
        this.relationships = new HashMap<>();
        this.actions = new HashMap<>();
    }

    @Override
    public void addProperty(final String rawPropertyName) {
        this.createDTDProperty(rawPropertyName, true)
                .ifPresent(property -> this.properties.put(rawPropertyName, property));
    }

    @Override
    public boolean removeProperty(final String rawPropertyName) {
        return this.properties.remove(rawPropertyName) != null;
    }

    @Override
    public void addRelationship(final String rawRelationshipName) {
        this.createDTDProperty(rawRelationshipName, false)
                .ifPresent(relationship -> this.relationships.put(rawRelationshipName, relationship));
    }

    @Override
    public boolean removeRelationship(final String rawRelationshipName) {
        return this.relationships.remove(rawRelationshipName) != null;
    }

    @Override
    public void addAction(final String rawActionName) {
        this.createDTDAction(rawActionName).ifPresent(action -> this.actions.put(rawActionName, action));
    }

    @Override
    public boolean removeAction(final String rawActionName) {
        return this.actions.remove(rawActionName) != null;
    }

    @Override
    public Set<String> getAvailableActionIds() {
        return new HashSet<>(this.actions.keySet());
    }

    @Override
    public ThingDescription getDTD() {
        final Map<String, Property> dtdProperties = new HashMap<>(this.properties);
        dtdProperties.putAll(this.relationships);
        if (!actions.isEmpty()) {
            dtdProperties.put(AVAILABLE_ACTIONS_PROPERTY, Property.newBuilder(AVAILABLE_ACTIONS_PROPERTY)
                            .setAtType(AtType.newSingleAtType(WoDTVocabulary.AVAILABLE_ACTIONS.getUri()))
                            .setReadOnly(true)
                            .build());
        }

        final List<BaseLink<?>> links = this.platformManagementInterfaceReader
                .getRegisteredPlatformUrls()
                .stream().map(uri -> BaseLink.newLinkBuilder()
                        .setHref(IRI.of(uri.toString()))
                        .setRel(WoDTVocabulary.REGISTERED_TO_PLATFORM.getUri())
                        .build())
                .collect(Collectors.toList());
        links.add(Link.newBuilder()
            .setHref(IRI.of(THING_MODEL_URL))
            .setRel("type")
            .setType("application/tm+json")
            .build());
        links.add(Link.newBuilder()
            .setHref(IRI.of(this.digitalTwinUri.resolve("/dtkg").toString()))
            .setRel(WoDTVocabulary.DTKG.getUri())
            .build());

        return ThingDescription.newBuilder()
                .setAtContext(AtContext.newSingleUriAtContext(SingleUriAtContext.W3ORG_2022_WOT_TD_V11))
                .setId(IRI.of(this.digitalTwinUri.toString()))
                .setAtType(AtType.newSingleAtType(this.ontology.getDigitalTwinType()))
                .setVersion(Version.newBuilder()
                        .setInstance(this.dtVersion.toString())
                        .setModel(MODEL_VERSION)
                        .build())
                .set(WoDTVocabulary.PHYSICAL_ASSET_ID.getUri(), this.physicalAssetId)
                .setSecurityDefinitions(SecurityDefinitions.of(Map.of("nosec_sc",
                        SecurityScheme.newNoSecurityBuilder("nosec_sc").build())))
                .setSecurity(Security.newSingleSecurity("nosec_sc"))
                .setProperties(Properties.from(dtdProperties.values()))
                .setActions(Actions.from(this.actions.values()))
                .setForms(List.of(RootFormElement.newBuilder()
                                .setHref(IRI.of(
                                    URI.create(this.digitalTwinUri.toString().replaceFirst("([a-zA-Z][a-zA-Z0-9+.-]*):", "ws:"))
                                            .resolve("/dtkg")
                                            .toString()
                                    ))
                                .setSubprotocol("websocket")
                                .setOp(SingleRootFormElementOp.OBSERVEALLPROPERTIES)
                                .build()))
                .setLinks(links)
                .build();

    }

    private Optional<Property> createDTDProperty(
            final String rawPropertyName,
            final boolean indicateAugmentation
    ) {
        final Optional<String> domainPredicateUri = this.ontology
                .obtainProperty(rawPropertyName)
                .flatMap(io.github.webbasedwodt.model.ontology.Property::getUri);

        if (domainPredicateUri.isPresent()) {
            final JsonObjectBuilder metadata = JsonObject.newBuilder()
                    .set(WoDTVocabulary.DOMAIN_TAG.getUri(), domainPredicateUri.get());
            if (indicateAugmentation) {
                metadata.set(WoDTVocabulary.AUGMENTED_INTERACTION.getUri(), false);
            }

            return Optional.of(Property.newBuilder(rawPropertyName, metadata.build())
                            .setReadOnly(true)
                            .build());
        }
        return Optional.empty();
    }

    private Optional<Action> createDTDAction(final String rawActionName) {
        return this.ontology.obtainActionType(rawActionName)
                .map(actionType -> Action.newBuilder(rawActionName)
                        .set(WoDTVocabulary.DOMAIN_TAG.getUri(), actionType)
                        .set(WoDTVocabulary.AUGMENTED_INTERACTION.getUri(), false)
                        .setForms(ActionForms.of(List.of(ActionFormElement.newBuilder()
                            .setHref(IRI.of(this.digitalTwinUri.resolve("/action/" + rawActionName).toString()))
                            .build())))
                        .build());
    }
}
