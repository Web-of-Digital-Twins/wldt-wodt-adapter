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

import it.wldt.adapter.physical.PhysicalAdapter;
import it.wldt.adapter.physical.PhysicalAssetAction;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetProperty;
import it.wldt.adapter.physical.PhysicalAssetRelationship;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceDeletedWldtEvent;
import it.wldt.exception.EventBusException;
import it.wldt.exception.PhysicalAdapterException;

import java.util.logging.Logger;

/**
 * The physical adapter of the lamp Digital Twins used during tests.
 */
public final class LampPhysicalAdapter extends PhysicalAdapter {
    private static final String LUMINOSITY_PROPERTY_KEY = "luminosity";
    private static final String ILLUMINANCE_PROPERTY_KEY = "illuminance";
    private static final String SWITCH_ACTION_KEY = "switch";
    private static final String IS_IN_ROOM_RELATIONSHIP_KEY = "isInRoom";
    private static final double LUMINOSITY_ON_VALUE = 85.5;
    private static final int EMULATION_WAIT_TIME = 2000;

    private boolean status;
    private final PhysicalAssetRelationship<String> relationshipIsInRoom =
            new PhysicalAssetRelationship<>(IS_IN_ROOM_RELATIONSHIP_KEY);

    /**
     * Default constructor.
     */
    public LampPhysicalAdapter() {
        super("lamp-physical-adapter");
        this.status = false;
    }

    @Override
    public void onIncomingPhysicalAction(final PhysicalAssetActionWldtEvent<?> physicalActionEvent) {
        if (physicalActionEvent != null && physicalActionEvent.getActionKey().equals(SWITCH_ACTION_KEY)) {
            try {
                //Create a new event to notify the variation of a Physical Property
                this.status = !this.status;
                final PhysicalAssetPropertyWldtEvent<Double> newLuminosity = new PhysicalAssetPropertyWldtEvent<>(
                        LUMINOSITY_PROPERTY_KEY,
                        this.status ? LUMINOSITY_ON_VALUE : 0
                );
                publishPhysicalAssetPropertyWldtEvent(newLuminosity);
                Logger.getLogger(LampPhysicalAdapter.class.getName()).info("ACTION called: Switch");
            } catch (EventBusException e) {
                Logger.getLogger(LampPhysicalAdapter.class.getName()).info(e.getMessage());
            }
        }
    }

    @Override
    public void onAdapterStart() {
        final PhysicalAssetDescription pad = new PhysicalAssetDescription();
        final PhysicalAssetProperty<Double> luminosityProperty = new PhysicalAssetProperty<>(LUMINOSITY_PROPERTY_KEY, 0.0);
        final PhysicalAssetProperty<Double> illuminanceProperty = new PhysicalAssetProperty<>(ILLUMINANCE_PROPERTY_KEY, 0.0);
        pad.getProperties().add(luminosityProperty);
        pad.getProperties().add(illuminanceProperty);
        final PhysicalAssetAction switchAction = new PhysicalAssetAction(SWITCH_ACTION_KEY, "status.switch", "");
        pad.getActions().add(switchAction);
        pad.getRelationships().add(relationshipIsInRoom);

        try {
            this.notifyPhysicalAdapterBound(pad);
        } catch (PhysicalAdapterException | EventBusException e) {
            Logger.getLogger(LampPhysicalAdapter.class.getName()).info(e.getMessage());
        }

        new Thread(this::emulatedDevice).start();
    }

    @Override
    public void onAdapterStop() {

    }

    private void emulatedDevice() {
        try {
            final var instance = relationshipIsInRoom.createRelationshipInstance("http://example.com/house");
            publishPhysicalAssetRelationshipCreatedWldtEvent(
                    new PhysicalAssetRelationshipInstanceCreatedWldtEvent<>(
                            instance
                    )
            );
            Thread.sleep(EMULATION_WAIT_TIME);
            publishPhysicalAssetRelationshipDeletedWldtEvent(
                    new PhysicalAssetRelationshipInstanceDeletedWldtEvent<>(
                            instance
                    )
            );
            Thread.sleep(EMULATION_WAIT_TIME);
            publishPhysicalAssetRelationshipCreatedWldtEvent(
                    new PhysicalAssetRelationshipInstanceCreatedWldtEvent<>(
                            instance
                    )
            );
            while (true) {
                Thread.sleep(EMULATION_WAIT_TIME);
                final PhysicalAssetPropertyWldtEvent<Double> newIlluminance = new PhysicalAssetPropertyWldtEvent<>(
                        ILLUMINANCE_PROPERTY_KEY,
                        Math.random() * 100
                );
                publishPhysicalAssetPropertyWldtEvent(newIlluminance);
                Logger.getLogger(LampPhysicalAdapter.class.getName()).info("STATUS: " + this.status);
            }
        } catch (EventBusException | InterruptedException e) {
            Logger.getLogger(LampPhysicalAdapter.class.getName()).info(e.getMessage());
        }
    }
}
