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

/**
 * This interface models the DTD Manager component of the Abstract Architecture.
 */
public interface DTDManager extends DTDManagerReader {
    /**
     * Add a property to the DTD.
     * @param rawPropertyName the raw name of the property to add
     */
    void addProperty(String rawPropertyName);

    /**
     * Remove a property from the DTD.
     * @param rawPropertyName the raw name of the property to remove
     * @return true is correctly removed, false if not present
     */
    boolean removeProperty(String rawPropertyName);

    /**
     * Add a relationship to the DTD.
     * @param rawRelationshipName the raw name of the relationship to add
     */
    void addRelationship(String rawRelationshipName);

    /**
     * Remove a relationship from the DTD.
     * @param rawRelationshipName the raw name of the relationship to remove
     * @return true is correctly removed, false if not present
     */
    boolean removeRelationship(String rawRelationshipName);

    /**
     * Add an action to the DTD.
     * @param rawActionName the raw name of the action to add
     */
    void addAction(String rawActionName);

    /**
     * Remove an action from the DTD.
     * @param rawActionName the raw name of the action to remove
     * @return true is correctly removed, false if not present
     */
    boolean removeAction(String rawActionName);

    /**
     * Add a WoDT Digital Twins Platform to which the WoDT Digital Twin is registered, to the DTD.
     * @param platformUrl the url of the WoDT Digital Twins Platform
     * @return true is correctly added, false instead
     */
    boolean addPlatform(String platformUrl);
}
