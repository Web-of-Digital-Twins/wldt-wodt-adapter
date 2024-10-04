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

import io.github.webbasedwodt.adapter.WoDTDigitalAdapter;
import io.github.webbasedwodt.adapter.WoDTDigitalAdapterConfiguration;
import io.github.webbasedwodt.model.dtd.DTVersion;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.engine.DigitalTwinEngine;
import it.wldt.exception.EventBusException;
import it.wldt.exception.ModelException;
import it.wldt.exception.WldtConfigurationException;
import it.wldt.exception.WldtDigitalTwinStateException;
import it.wldt.exception.WldtEngineException;
import it.wldt.exception.WldtRuntimeException;
import it.wldt.exception.WldtWorkerException;

import java.net.URI;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Manual Integration test for the WoDT Digital Twin implemented with the WLDT Framework.
 */
public final class LampDT {
    private static final int TEST_PORT_NUMBER = 3000;

    private LampDT() {
    }

    /**
     * Main process.
     *
     * @param args the args
     */
    public static void main(final String... args) {
        try {
            final String digitalTwinId = "lamp-dt";
            final DigitalTwin digitalTwin = new DigitalTwin(digitalTwinId, new MirrorShadowingFunction());
            digitalTwin.addPhysicalAdapter(new LampPhysicalAdapter());
            digitalTwin.addDigitalAdapter(new WoDTDigitalAdapter(
                    "wodt-dt-adapter",
                    new WoDTDigitalAdapterConfiguration(
                            URI.create("http://localhost:" + TEST_PORT_NUMBER),
                            new DTVersion(1, 2, 3),
                            new LampDTOntology(),
                            TEST_PORT_NUMBER,
                            "lampPA",
                            Set.of())
            ));

            final DigitalTwinEngine digitalTwinEngine = new DigitalTwinEngine();
            digitalTwinEngine.addDigitalTwin(digitalTwin);
            digitalTwinEngine.startDigitalTwin(digitalTwinId);
        } catch (ModelException
                 | WldtDigitalTwinStateException
                 | WldtWorkerException
                 | WldtRuntimeException
                 | EventBusException
                 | WldtConfigurationException
                 | WldtEngineException e) {
            Logger.getLogger(LampDT.class.getName()).info(e.getMessage());
        }
    }
}
