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

package io.github.webbasedwodt.model.dtd;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link DTVersion}.
 */
class DTVersionTest {
    private final DTVersion version = new DTVersion(1, 2, 3);

    @Test
    @DisplayName("Version is correctly converted in string")
    void testStringConversion() {
        assertEquals("1.2.3", this.version.toString());
    }

    @Test
    @DisplayName("Version cannot contain negative numbers")
    void testNegativeNumber() {
        assertThrows(IllegalArgumentException.class, () -> new DTVersion(-1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new DTVersion(0, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> new DTVersion(0, 0, -1));
    }

    @Test
    @DisplayName("Version cannot be zero")
    void testVersionZero() {
        assertThrows(IllegalArgumentException.class, () -> new DTVersion(0, 0, 0));
    }
}
