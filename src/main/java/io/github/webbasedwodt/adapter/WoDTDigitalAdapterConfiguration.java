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

import io.github.webbasedwodt.model.ontology.DTOntology;

/**
 * Configuration for the {@link WoDTDigitalAdapter}.
 */
public final class WoDTDigitalAdapterConfiguration {
    private final DTOntology ontology;
    private final String digitalTwinUri;
    private final int portNumber;
    private final String physicalAssetId;

    /**
     * Default constructor.
     * @param digitalTwinUri the uri of the WoDT Digital Twin
     * @param ontology the ontology to use for the semantics
     * @param portNumber the port number where to expose services
     * @param physicalAssetId the id of the associated physical asset
     */
    public WoDTDigitalAdapterConfiguration(
            final String digitalTwinUri,
            final DTOntology ontology,
            final int portNumber,
            final String physicalAssetId) {
        this.digitalTwinUri = digitalTwinUri;
        this.ontology = ontology;
        this.portNumber = portNumber;
        this.physicalAssetId = physicalAssetId;
    }

    /**
     * Obtain the WoDT Digital Twin URI.
     * @return the URI.
     */
    public String getDigitalTwinUri() {
        return this.digitalTwinUri;
    }

    /**
     * Obtain the ontology to describe the Digital Twin data.
     * @return the ontology.
     */
    public DTOntology getOntology() {
        return this.ontology;
    }

    /**
     * Obtain the port number where to expose services.
     * @return the port number
     */
    public int getPortNumber() {
        return this.portNumber;
    }

    /**
     * Obtain the associated physical asset id.
     * @return the id of the associated physical asset
     */
    public String getPhysicalAssetId() {
        return this.physicalAssetId;
    }
}
