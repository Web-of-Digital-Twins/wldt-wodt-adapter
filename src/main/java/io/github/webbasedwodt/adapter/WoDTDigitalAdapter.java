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
import io.github.webbasedwodt.application.component.DTKGEngine;
import io.github.webbasedwodt.application.component.PlatformManagementInterface;
import io.github.webbasedwodt.application.component.WoDTWebServer;
import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.core.state.DigitalTwinStateAction;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.core.state.DigitalTwinStateRelationship;
import it.wldt.core.state.DigitalTwinStateRelationshipInstance;
import it.wldt.core.state.DigitalTwinStateResource;
import it.wldt.exception.EventBusException;
import it.wldt.exception.WldtDigitalTwinStateActionException;
import it.wldt.exception.WldtDigitalTwinStatePropertyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * This class represents the WLDT Framework Digital Adapter that allows to implement the WoDT Digital Twin layer
 * implementing the components of the Abstract Architecture.
 */
public final class WoDTDigitalAdapter extends DigitalAdapter<WoDTDigitalAdapterConfiguration> {
    private final DTKGEngine dtkgEngine;
    private final DTDManager dtdManager;
    private final WoDTWebServer woDTWebServer;
    private final PlatformManagementInterface platformManagementInterface;

    private static final Logger LOGGER = LoggerFactory.getLogger(WoDTDigitalAdapter.class);
    /**
     * Default constructor.
     * @param digitalAdapterId the id of the Digital Adapter
     * @param configuration the configuration of the Digital Adapter
     */
    public WoDTDigitalAdapter(final String digitalAdapterId, final WoDTDigitalAdapterConfiguration configuration) {
        super(digitalAdapterId, configuration);
        this.platformManagementInterface = new BasePlatformManagementInterface(
                this.getConfiguration().getDigitalTwinUri());
        this.dtkgEngine = new JenaDTKGEngine(
                this.getConfiguration().getDigitalTwinUri(),
                this.getConfiguration().getDigitalTwinSemantics());
        this.dtdManager = new WoTDTDManager(
                this.getConfiguration().getDigitalTwinUri(),
                this.getConfiguration().getDtVersion(),
                this.getConfiguration().getDigitalTwinSemantics(),
                this.getConfiguration().getPhysicalAssetId(),
                this.platformManagementInterface
        );
        this.woDTWebServer = new WoDTWebServerImpl(
                this.getConfiguration().getPortNumber(),
                this.dtkgEngine,
                this.dtdManager,
                (actionName, body) -> {
                    try {
                        publishDigitalActionWldtEvent(actionName, body);
                        return true;
                    } catch (EventBusException e) {
                        this.logMessage("Impossible to forward action: " + e);
                        return false;
                    }
                },
                this.platformManagementInterface
        );
    }

    @Override
    protected void onEventNotificationReceived(
            final DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) { }

    @Override
    protected void onStateUpdate(
            final DigitalTwinState newDigitalTwinState,
            final DigitalTwinState previousDigitalTwinState,
            final ArrayList<DigitalTwinStateChange> digitalTwinStateChanges
    ) {
        if (digitalTwinStateChanges != null && !digitalTwinStateChanges.isEmpty()) {
            this.logMessage("New State Update Received");
            //TODO this for seems to be sending multiple updates for a single state change
            // implement a sort of transaction for state updates would be best
            for (final DigitalTwinStateChange change : digitalTwinStateChanges) {
                final DigitalTwinStateChange.Operation operationPerformed = change.getOperation();
                final DigitalTwinStateChange.ResourceType changeResourceType = change.getResourceType();
                final DigitalTwinStateResource changedResource = change.getResource();

                switch (changeResourceType) {
                    case PROPERTY:
                    case PROPERTY_VALUE:
                        if (changedResource instanceof DigitalTwinStateProperty<?>) {
                            if (previousDigitalTwinState != null) {
                                try {
                                    this.handlePropertyUpdate(
                                            (DigitalTwinStateProperty<?>) changedResource,
                                            previousDigitalTwinState.getProperty(
                                                    ((DigitalTwinStateProperty<?>) changedResource)
                                                    .getKey())
                                                    .orElse(null),
                                            operationPerformed);
                                } catch (WldtDigitalTwinStatePropertyException e) {
                                    this.handlePropertyUpdate((DigitalTwinStateProperty<?>) changedResource,
                                            null,
                                            operationPerformed);
                                }
                            } else {
                                this.handlePropertyUpdate((DigitalTwinStateProperty<?>) changedResource,
                                        null,
                                        operationPerformed);
                            }
                        }
                        break;
                    case RELATIONSHIP:
                        if (changedResource instanceof DigitalTwinStateRelationship<?>) {
                            this.handleRelationshipUpdate(
                                    (DigitalTwinStateRelationship<?>) changedResource, operationPerformed);
                        }
                        break;
                    case RELATIONSHIP_INSTANCE:
                        if (changedResource instanceof DigitalTwinStateRelationshipInstance<?>) {
                            this.handleRelationshipInstanceUpdate(
                                    (DigitalTwinStateRelationshipInstance<?>) changedResource, operationPerformed);
                        }
                        break;
                    case ACTION:
                        if (changedResource instanceof DigitalTwinStateAction) {
                            this.handleActionUpdate((DigitalTwinStateAction) changedResource, operationPerformed);
                        }
                        break;
                    case EVENT:
                        this.logMessage("Events are not currently supported");
                        break;
                    default:
                        break;
                }
            }
            this.logMessage("New state update sent");
        }
    }

    private void handlePropertyUpdate(
            final DigitalTwinStateProperty<?> updatedProperty,
            final DigitalTwinStateProperty<?> oldProperty,
            final DigitalTwinStateChange.Operation operationPerformed
    ) {
        switch (operationPerformed) {
            case OPERATION_ADD:
                this.dtdManager.addProperty(updatedProperty);
                break;
            case OPERATION_REMOVE:
                this.dtkgEngine.removeProperty(updatedProperty);
                this.dtdManager.removeProperty(updatedProperty);
                break;
            case OPERATION_UPDATE:
            case OPERATION_UPDATE_VALUE:
                if (oldProperty == null) {
                    this.dtkgEngine.addDigitalTwinProperty(updatedProperty);
                } else {
                    this.dtkgEngine.updateDigitalTwinProperty(updatedProperty, oldProperty);
                }
                break;
            default:
                break;
        }
    }

    private void handleRelationshipUpdate(
            final DigitalTwinStateRelationship<?> updatedRelationship,
            final DigitalTwinStateChange.Operation operationPerformed
    ) {
        switch (operationPerformed) {
            case OPERATION_ADD:
                this.dtdManager.addRelationship(updatedRelationship);
                break;
            case OPERATION_REMOVE:
                this.dtdManager.removeRelationship(updatedRelationship);
                break;
            default:
                break;
        }
    }

    private void handleRelationshipInstanceUpdate(
            final DigitalTwinStateRelationshipInstance<?> updatedRelationshipInstance,
            final DigitalTwinStateChange.Operation operationPerformed
    ) {
        switch (operationPerformed) {
            case OPERATION_ADD:
                this.dtkgEngine.addRelationship(updatedRelationshipInstance);
                break;
            case OPERATION_REMOVE:
                this.dtkgEngine.removeRelationship(updatedRelationshipInstance);
                break;
            default:
                break;
        }
    }

    private void handleActionUpdate(
            final DigitalTwinStateAction updatedAction,
            final DigitalTwinStateChange.Operation operationPerformed
    ) {
        switch (operationPerformed) {
            case OPERATION_ADD: // adds and enables the action
                this.dtdManager.addAction(updatedAction);
                this.dtkgEngine.addAction(updatedAction);
                break;
            case OPERATION_REMOVE: // only disables the action
                this.dtkgEngine.removeAction(updatedAction);
                break;
            case OPERATION_UPDATE: // enables the action
                this.dtkgEngine.addAction(updatedAction);
                break;
            default:
                break;
        }
    }

    @Override
    public void onAdapterStart() {
        this.woDTWebServer.start();
        this.getConfiguration().getPlatformToRegister().forEach(platform ->
                this.platformManagementInterface.registerToPlatform(platform, this.dtdManager.getDTD().toJsonString()));
    }

    @Override
    public void onAdapterStop() {
        this.platformManagementInterface.signalDigitalTwinDeletion();
    }

    @Override
    public void onDigitalTwinSync(final DigitalTwinState digitalTwinState) {
        try {
            digitalTwinState.getPropertyList().ifPresent(properties ->
                    properties.forEach(property -> {
                        this.dtkgEngine.addDigitalTwinProperty(property);
                        this.dtdManager.addProperty(property);
                    }));
            digitalTwinState.getRelationshipList().ifPresent(relationships ->
                    relationships.forEach(this.dtdManager::addRelationship));
            digitalTwinState.getActionList().ifPresent(actions ->
                    actions.forEach(action -> {
                        this.dtdManager.addAction(action);
                        this.dtkgEngine.addAction(action);
                    }));
        } catch (WldtDigitalTwinStatePropertyException | WldtDigitalTwinStateActionException e) {
            this.logMessage("Error during loading: " + e);
        }
    }

    @Override
    public void onDigitalTwinUnSync(final DigitalTwinState digitalTwinState) { }

    @Override
    public void onDigitalTwinCreate() { }

    @Override
    public void onDigitalTwinStart() { }

    @Override
    public void onDigitalTwinStop() { }

    @Override
    public void onDigitalTwinDestroy() { }

    private void logMessage(final String message) {
        LOGGER.info("[{}] - {}", this.getId(), message);
    }
}
