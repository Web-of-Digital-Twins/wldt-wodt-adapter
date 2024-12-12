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

package io.github.webbasedwodt.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Tests for {@link UriUtil}. */
class UriUtilTest {
    private static final String SIMPLE_PATH = "dtkg";
    private static final URI SIMPLE_STRUCTURED_URI = URI.create("http://example.com/a/b/c");

    @Test
    @DisplayName("It should be possible to resolve a simple URI with a relative one")
    void testSimpleUri() {
        assertEquals(
            URI.create("http://example.com/dtkg"),
            UriUtil.uriRelativeResolve(URI.create("http://example.com"), SIMPLE_PATH)
        );
    }

    @Test
    @DisplayName("It should be possible to resolve an URI with already a path with a relative one")
    void testStructuredUri() {
        assertEquals(
            URI.create("http://example.com/a/dtkg"),
            UriUtil.uriRelativeResolve(URI.create("http://example.com/a"), SIMPLE_PATH)
        );
        assertEquals(
            URI.create("http://example.com/a/b/c/dtkg"),
            UriUtil.uriRelativeResolve(SIMPLE_STRUCTURED_URI, SIMPLE_PATH)
        );
    }

    @Test
    @DisplayName("It should be possible to resolve an URI with already a path with a relative one independently of fragment")
    void testStructuredUriWithFragment() {
        assertEquals(
            URI.create("http://example.com/dtkg"),
            UriUtil.uriRelativeResolve(URI.create("http://example.com#"), SIMPLE_PATH)
        );
        assertEquals(
            URI.create("http://example.com/a/b/c/dtkg"),
            UriUtil.uriRelativeResolve(URI.create("http://example.com/a/b/c#"), SIMPLE_PATH)
        );
    }

    @Test
    @DisplayName("It should be possible to append a fragment to base uri")
    void testFragmentPath() {
        assertEquals(
            URI.create("http://example.com#dtkg"),
            UriUtil.uriRelativeResolve(URI.create("http://example.com"), "#dtkg")
        );
        assertEquals(
            URI.create("http://example.com/a/b/c#dtkg"),
            UriUtil.uriRelativeResolve(SIMPLE_STRUCTURED_URI, "#dtkg")
        );
        assertEquals(
            URI.create("http://example.com/a/b/c/d/e/f#dtkg"),
            UriUtil.uriRelativeResolve(SIMPLE_STRUCTURED_URI, "d/e/f#dtkg")
        );
    }

    @Test
    @DisplayName("It should be possible to append at the end even when the relative path starts with /")
    void testRootPath() {
        assertEquals(
            URI.create("http://example.com/dtkg"),
            UriUtil.uriRelativeResolve(URI.create("http://example.com"), "/dtkg")
        );
        assertEquals(
            URI.create("http://example.com/a/b/c/dtkg"),
            UriUtil.uriRelativeResolve(SIMPLE_STRUCTURED_URI, "/dtkg")
        );
        assertEquals(
            URI.create("http://example.com/a/b/c/path/to/dtkg"),
            UriUtil.uriRelativeResolve(SIMPLE_STRUCTURED_URI, "/path/to/dtkg")
        );
        assertEquals(
                URI.create("http://example.com/a/b/c/path/to/dtkg"),
                UriUtil.uriRelativeResolve(URI.create("http://example.com/a/b/c/"), "/path/to/dtkg")
        );
    }
}
