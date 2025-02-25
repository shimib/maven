/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.repository.internal;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import java.util.Objects;

import org.apache.maven.api.Version;
import org.apache.maven.api.VersionRange;
import org.apache.maven.api.services.VersionParserException;
import org.apache.maven.model.version.VersionParser;
import org.eclipse.aether.version.InvalidVersionSpecificationException;
import org.eclipse.aether.version.VersionScheme;

import static java.util.Objects.requireNonNull;

@Named
@Singleton
public class DefaultModelVersionParser implements VersionParser {
    private final VersionScheme versionScheme;

    @Inject
    public DefaultModelVersionParser(VersionScheme versionScheme) {
        this.versionScheme = requireNonNull(versionScheme, "versionScheme");
    }

    @Override
    public Version parseVersion(String version) {
        requireNonNull(version, "version");
        return new DefaultVersion(versionScheme, version);
    }

    @Override
    public VersionRange parseVersionRange(String range) {
        requireNonNull(range, "range");
        return new DefaultVersionRange(versionScheme, range);
    }

    static class DefaultVersion implements Version {
        private final VersionScheme versionScheme;
        private final org.eclipse.aether.version.Version delegate;

        DefaultVersion(VersionScheme versionScheme, String delegateValue) {
            this.versionScheme = versionScheme;
            try {
                this.delegate = versionScheme.parseVersion(delegateValue);
            } catch (InvalidVersionSpecificationException e) {
                throw new VersionParserException("Unable to parse version: " + delegateValue, e);
            }
        }

        @Override
        public int compareTo(Version o) {
            if (o instanceof DefaultVersion) {
                return delegate.compareTo(((DefaultVersion) o).delegate);
            } else {
                return compareTo(new DefaultVersion(versionScheme, o.asString()));
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            DefaultVersion that = (DefaultVersion) o;
            return delegate.equals(that.delegate);
        }

        @Override
        public int hashCode() {
            return Objects.hash(delegate);
        }

        @Override
        public String asString() {
            return delegate.toString();
        }

        @Override
        public String toString() {
            return asString();
        }
    }

    static class DefaultVersionRange implements VersionRange {
        private final VersionScheme versionScheme;
        private final org.eclipse.aether.version.VersionRange delegate;

        DefaultVersionRange(VersionScheme versionScheme, String delegateValue) {
            this.versionScheme = versionScheme;
            try {
                this.delegate = versionScheme.parseVersionRange(delegateValue);
            } catch (InvalidVersionSpecificationException e) {
                throw new VersionParserException("Unable to parse version range: " + delegateValue, e);
            }
        }

        @Override
        public boolean contains(Version version) {
            if (version instanceof DefaultVersion) {
                return delegate.containsVersion(((DefaultVersion) version).delegate);
            } else {
                return contains(new DefaultVersion(versionScheme, version.asString()));
            }
        }

        @Override
        public String asString() {
            return delegate.toString();
        }

        @Override
        public String toString() {
            return asString();
        }
    }
}
