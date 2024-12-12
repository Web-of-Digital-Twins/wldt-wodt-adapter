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

import java.net.URI;

/** Class that wraps utilities for URIs. */
public final class UriUtil {
    private UriUtil() { }

    /**
     * Similar method to {@link URI#resolve(URI) resolve} but that always attach the path to the end.
     * @param originalUri the original URI to attach the input path
     * @param path the path to attach to the original URI.
     * @return a relative resolve of the original URI with respect to the input path.
     */
    public static URI uriRelativeResolve(final URI originalUri, final String path) {
        URI baseUri = originalUri;
        String relativePath = path;
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        if (!baseUri.toString().endsWith("/") && !relativePath.startsWith("#")) {
            if (baseUri.toString().endsWith("#")) {
                baseUri = URI.create(baseUri.toString().substring(0, baseUri.toString().length() - 1));
            }
            baseUri = URI.create(baseUri + "/");
        }
        return baseUri.resolve(relativePath);
    }
}
