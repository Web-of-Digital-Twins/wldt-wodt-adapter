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

import it.wldt.core.state.DigitalTwinStateAction;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.core.state.DigitalTwinStateRelationship;

/**
 * This interface models the DTD Manager component of the Abstract Architecture.
 */
public interface DTDManager extends DTDManagerReader {
    /**
     * Add a property to the DTD.
     * @param property the property to add
     */
    void addProperty(DigitalTwinStateProperty<?> property);

    /**
     * Remove a property from the DTD.
     * @param property the property to remove
     * @return true is correctly removed, false if not present
     */
    boolean removeProperty(DigitalTwinStateProperty<?> property);

    /**
     * Add a relationship to the DTD.
     * @param relationship the relationship to add
     */
    void addRelationship(DigitalTwinStateRelationship<?> relationship);

    /**
     * Remove a relationship from the DTD.
     * @param relationship the relationship to remove
     * @return true is correctly removed, false if not present
     */
    boolean removeRelationship(DigitalTwinStateRelationship<?> relationship);

    /**
     * Add an action to the DTD.
     * @param action the action to add
     */
    void addAction(DigitalTwinStateAction action);

    /**
     * Remove an action from the DTD.
     * @param action the action to remove
     * @return true is correctly removed, false if not present
     */
    boolean removeAction(DigitalTwinStateAction action);
}
