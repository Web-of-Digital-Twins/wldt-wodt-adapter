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

plugins {
    `java-library`
    alias(libs.plugins.gitSemVer)
    alias(libs.plugins.java.qa)
    alias(libs.plugins.publish.on.central)
}

gitSemVer {
    buildMetadataSeparator.set("-")
}

group = "io.github.webbasedwodt"

repositories {
    mavenCentral()
    maven {
        url = uri("https://git.informatik.uni-hamburg.de/api/v4/groups/sane-public/-/packages/maven")
    }
}

dependencies {
    implementation(libs.ditto.wot.model)
    implementation(libs.javalin)
    implementation(libs.jena)
    api(libs.wldt)
    testImplementation(libs.bundles.java.testing)
    testRuntimeOnly(libs.junit.engine)
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
        showCauses = true
        showStackTraces = true
        events(*org.gradle.api.tasks.testing.logging.TestLogEvent.values())
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

publishOnCentral {
    projectUrl.set("https://github.com/Web-of-Digital-Twins/wldt-wodt-adapter")
    scmConnection.set("git:git@github.com:Web-of-Digital-Twins/wldt-wodt-adapter")
    configureMavenCentral.set(false)
    licenseName.set("Apache License, Version 2.0")
    licenseUrl.set("http://www.apache.org/licenses/LICENSE-2.0")

    repository("https://maven.pkg.github.com/Web-of-Digital-Twins/wldt-wodt-adapter", "GitHub") {
        user.set(System.getenv("GITHUB_ACTOR"))
        password.set(System.getenv("GITHUB_TOKEN"))
    }
}

publishing {
    publications {
        withType<MavenPublication> {
            pom {
                developers {
                    developer {
                        name.set("Andrea Giulianelli")
                        email.set("andrea.giulianelli4@studio.unibo.it")
                        url.set("https://github.com/AndreaGiulianelli")
                    }
                }
            }
        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
}
