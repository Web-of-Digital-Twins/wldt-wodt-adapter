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

package io.github.webbasedwodt.application.component;

import io.github.webbasedwodt.application.component.observer.DTKGObserver;
import it.wldt.core.state.DigitalTwinStateAction;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.core.state.DigitalTwinStateRelationshipInstance;

/**
 * This interface models the DTKGEngine component of the Abstract Architecture in a compatible way with the WLDT
 * Framework.
 */
public interface DTKGEngine extends DTKGEngineReader {
    /**
     * Method that allows to signal the deletion or the stop of the underlying Digital Twin.
     */
    void removeDigitalTwin();

    /**
     * Add a Digital Twin property within the Digital Twin Knowledge Graph.
     * @param property the property to add
     */
    void addDigitalTwinProperty(DigitalTwinStateProperty<?> property);

    /**
     * Update a Digital Twin property within the Digital Twin Knowledge Graph.
     * @param property the property to update
     * @param oldProperty the old property that has been updated
     */
    void updateDigitalTwinProperty(DigitalTwinStateProperty<?> property, DigitalTwinStateProperty<?> oldProperty);

    /**
     * Remove a Digital Twin property within the Digital Twin Knowledge Graph.
     * @param property the property to delete.
     * @return true if deleted, false if not-existent.
     */
    boolean removeProperty(DigitalTwinStateProperty<?> property);

    /**
     * Add a relationship with another Digital Twin.
     * @param relationshipInstance the relationship instance to add.
     */
    void addRelationship(DigitalTwinStateRelationshipInstance<?> relationshipInstance);

    /**
     * Delete an existing relationship with another Digital Twin.
     * @param relationshipInstance the relationship instance to remove.
     * @return true if correctly deleted, false if the relationship doesn't exist
     */
    boolean removeRelationship(DigitalTwinStateRelationshipInstance<?> relationshipInstance);

    /**
     * Add an available action on the Digital Twin Knowledge Graph.
     * @param action the action to add.
     */
    void addAction(DigitalTwinStateAction action);

    /**
     * Remove an action from the Digital Twin Knowledge Graph.
     * @param action the action to remove
     * @return true if correctly deleted, false if the action id doesn't exist
     */
    boolean removeAction(DigitalTwinStateAction action);

    /**
     * Add a {@link DTKGObserver} that will be notified for each DTKG update.
     * @param observer the observer to add.
     */
    void addDTKGObserver(DTKGObserver observer);
}
