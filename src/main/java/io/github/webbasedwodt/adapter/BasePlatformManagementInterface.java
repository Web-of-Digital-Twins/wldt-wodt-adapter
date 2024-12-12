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

import io.github.webbasedwodt.application.component.PlatformManagementInterface;
import io.github.webbasedwodt.utils.UriUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Base implementation of the {@link PlatformManagementInterface}.
 */
final class BasePlatformManagementInterface implements PlatformManagementInterface {
    private static final String PATH_TO_PLATFORM_WODT = "wodt";
    private static final int ACCEPTED_REQUEST_STATUS_CODE = 202;
    private final URI digitalTwinUri;
    private final Set<URI> platforms;

    /**
     * Default constructor.
     * @param digitalTwinUri the uri of the WoDT Digital Twin
     */
    BasePlatformManagementInterface(final URI digitalTwinUri) {
        this.digitalTwinUri = digitalTwinUri;
        this.platforms = Collections.synchronizedSet(new HashSet<>());
    }

    @Override
    public boolean registerToPlatform(final URI platformUrl, final String currentDtd) {
        if (!this.platforms.contains(platformUrl)) {
            final HttpClient httpClient = HttpClient.newHttpClient();
            final HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(getPlatformWoDT(platformUrl))
                    .header("Content-type", "application/td+json")
                    .POST(HttpRequest.BodyPublishers.ofString(currentDtd))
                    .build();
            final boolean status = httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString())
                    .join()
                    .statusCode() == ACCEPTED_REQUEST_STATUS_CODE;
            if (status) {
                notifyNewRegistration(platformUrl);
            }
            return status;
        } else {
            return false;
        }
    }

    @Override
    public void signalDigitalTwinDeletion() {
        final HttpClient httpClient = HttpClient.newHttpClient();
        this.platforms.forEach(platformUrl -> {
            final HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(getPlatformCachedDT(platformUrl, this.digitalTwinUri.toString()))
                    .DELETE()
                    .build();
            httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
        });
        this.platforms.clear();
    }

    private URI getPlatformWoDT(final URI platformUrl) {
        return UriUtil.uriRelativeResolve(platformUrl, PATH_TO_PLATFORM_WODT);
    }

    private URI getPlatformCachedDT(final URI platformUrl, final String resource) {
        return URI.create(getPlatformWoDT(platformUrl) + "/" + resource);
    }

    @Override
    public Set<URI> getRegisteredPlatformUrls() {
        return new HashSet<>(this.platforms);
    }

    @Override
    public boolean notifyNewRegistration(final URI platformUrl) {
        return this.platforms.add(platformUrl);
    }
}
