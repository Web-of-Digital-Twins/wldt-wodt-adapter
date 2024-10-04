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

/**
 * Value object that implement the version of the DT, following the semantic versioning rationale.
 */
public final class DTVersion {
    private final int major;
    private final int minor;
    private final int patch;

    /**
     * Default constructor.
     * @param major the major version number.
     * @param minor the minor version number.
     * @param patch the patch version number.
     */
    public DTVersion(final int major, final int minor, final int patch) {
        if (major < 0 || minor < 0 || patch < 0) {
            throw new IllegalArgumentException("Value of major/minor/patch must be greater than zero.");
        }
        if (major == 0 && minor == 0 && patch == 0) {
            throw new IllegalArgumentException("Value of major/minor/patch must not be all zero.");
        }
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    /**
     * Get major version number.
     * @return the major version number.
     */
    public int getMajor() {
        return this.major;
    }

    /**
     * Get minor version number.
     * @return the minor version number.
     */
    public int getMinor() {
        return this.minor;
    }

    /**
     * Get patch version number.
     * @return the patch version number.
     */
    public int getPatch() {
        return this.patch;
    }

    @Override
    public String toString() {
        return String.join(".",
                String.valueOf(this.major),
                String.valueOf(this.minor),
                String.valueOf(this.patch));
    }
}
