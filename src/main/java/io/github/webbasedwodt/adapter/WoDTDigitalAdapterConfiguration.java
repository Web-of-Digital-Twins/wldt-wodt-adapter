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

import io.github.webbasedwodt.model.dtd.DTVersion;
import io.github.webbasedwodt.model.ontology.DigitalTwinSemantics;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * Configuration for the {@link WoDTDigitalAdapter}.
 */
public final class WoDTDigitalAdapterConfiguration {
    private final DigitalTwinSemantics digitalTwinSemantics;
    private final URI digitalTwinUri;
    private final DTVersion dtVersion;
    private final int portNumber;
    private final String physicalAssetId;
    private final Set<URI> platformToRegister;

    /**
     * Default constructor.
     * @param digitalTwinUri the uri of the WoDT Digital Twin.
     *                       It also acts as the base URI (port included) for exposed services
     * @param dtVersion the version of the dt
     * @param digitalTwinSemantics the Digital Twin semantics
     * @param portNumber the port number where to expose services
     * @param physicalAssetId the id of the associated physical asset
     * @param platformToRegister the platforms to which register
     */
    public WoDTDigitalAdapterConfiguration(
            final URI digitalTwinUri,
            final DTVersion dtVersion,
            final DigitalTwinSemantics digitalTwinSemantics,
            final int portNumber,
            final String physicalAssetId,
            final Set<URI> platformToRegister) {
        this.digitalTwinUri = digitalTwinUri;
        this.dtVersion = dtVersion;
        this.digitalTwinSemantics = digitalTwinSemantics;
        this.portNumber = portNumber;
        this.physicalAssetId = physicalAssetId;
        this.platformToRegister = new HashSet<>(platformToRegister);
    }

    /**
     * Obtain the WoDT Digital Twin URI.
     * @return the URI.
     */
    public URI getDigitalTwinUri() {
        return this.digitalTwinUri;
    }

    /**
     * Obtain the WoDT Digital Twin version.
     * @return the dt version.
     */
    public DTVersion getDtVersion() {
        return this.dtVersion;
    }

    /**
     * Obtain the ontology to describe the Digital Twin data.
     * @return the ontology.
     */
    public DigitalTwinSemantics getDigitalTwinSemantics() {
        return this.digitalTwinSemantics;
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

    /**
     * Obtain the platform to which register.
     * @return the platforms urls.
     */
    public Set<URI> getPlatformToRegister() {
        return new HashSet<>(this.platformToRegister);
    }
}
