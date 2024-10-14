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
import io.github.webbasedwodt.application.component.DTKGEngine;
import io.github.webbasedwodt.integration.wldt.LampDTSemantics;
import io.github.webbasedwodt.model.dtd.DTVersion;
import io.github.webbasedwodt.model.ontology.DigitalTwinSemantics;
import io.github.webbasedwodt.model.ontology.WoDTVocabulary;
import io.javalin.Javalin;
import io.javalin.http.Header;
import io.javalin.http.HttpStatus;
import io.javalin.testtools.JavalinTest;
import it.wldt.core.state.DigitalTwinStateAction;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.exception.WldtDigitalTwinStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link WoDTDigitalTwinInterfaceControllerImpl}.
 */
class WoDTDigitalTwinInterfaceControllerTest {
    private static final int TEST_PORT_NUMBER = 3000;
    private static final URI TEST_DIGITAL_TWIN_URI = URI.create("http://example:" + TEST_PORT_NUMBER + "/dt");
    private final DigitalTwinSemantics dtSemantics = new LampDTSemantics();
    private Javalin app;
    private DTKGEngine dtkgEngine;
    private DTDManager dtdManager;

    @BeforeEach
    public void init() {
        this.app = Javalin.create();
        this.dtkgEngine = new JenaDTKGEngine(TEST_DIGITAL_TWIN_URI, dtSemantics);
        this.dtdManager = new WoTDTDManager(
                TEST_DIGITAL_TWIN_URI,
                new DTVersion(1, 0, 0),
                new LampDTSemantics(),
                "lampPA",
                new PlatformManagementInterfaceReaderTestDouble());
        new WoDTDigitalTwinInterfaceControllerImpl(this.dtkgEngine, this.dtdManager, (action, body) -> true)
                .registerRoutes(this.app);
    }

    @Test
    @DisplayName("A HTTP GET request on the DTKG URL should return the HTTP status NoContent if empty")
    void testEmptyDtkg() {
        this.dtkgEngine.removeDigitalTwin();
        JavalinTest.test(this.app, (server, client) -> {
            final var response = client.get("/dtkg");
            assertEquals(HttpStatus.NO_CONTENT.getCode(), response.code());
            assertEquals("</dtd>; rel=\"" + WoDTVocabulary.DTD + "\"", response.header(Header.LINK));
            assertTrue(response.body().string().isEmpty());
        });
    }

    @Test
    @DisplayName("A HTTP GET request on existent DTKG should correctly return the current DTKG")
    void testGetDtkg() throws WldtDigitalTwinStateException {
        this.dtkgEngine.addDigitalTwinProperty(new DigitalTwinStateProperty<>("luminosity", 100));
        JavalinTest.test(this.app, (server, client) -> {
            final var response = client.get("/dtkg");
            assertEquals(HttpStatus.OK.getCode(), response.code());
            assertEquals("</dtd>; rel=\"" + WoDTVocabulary.DTD + "\"", response.header(Header.LINK));
            assertEquals(this.dtkgEngine.getCurrentDigitalTwinKnowledgeGraph(), response.body().string());
        });
    }

    @Test
    @DisplayName("A HTTP GET request on the Digital Twin Descriptor should respect the specification")
    void testGetDTD() {
        JavalinTest.test(this.app, (server, client) -> {
            final var response = client.get("/dtd");
            assertEquals(HttpStatus.OK.getCode(), response.code());
            assertEquals(this.dtdManager.getDTD().toJsonString(), response.body().string());
        });
    }

    @Test
    @DisplayName("It should be possible to invoke an existent action")
    void testInvokeExistingAction() throws WldtDigitalTwinStateException {
        final DigitalTwinStateAction action = new DigitalTwinStateAction("switch", "", "");
        this.dtdManager.addAction(action);
        JavalinTest.test(this.app, (server, client) -> {
            final var response = client.post("/action/" + action.getKey());
            assertEquals(HttpStatus.ACCEPTED.getCode(), response.code());
        });
    }

    @Test
    @DisplayName("Requests to execute non-existent actions should be correctly handled")
    void testInvokeNonExistentAction() {
        JavalinTest.test(this.app, (server, client) -> {
            final var response = client.post("/action/actionName");
            assertEquals(HttpStatus.BAD_REQUEST.getCode(), response.code());
        });
    }
}
