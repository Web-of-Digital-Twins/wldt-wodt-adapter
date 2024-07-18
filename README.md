# WLDT Web of Digital Twins Adapter

![Release](https://github.com/WebBased-WoDT/wldt-wodt-adapter/actions/workflows/build-and-deploy.yml/badge.svg?style=plastic)
[![License: Apache License](https://img.shields.io/badge/License-Apache_License_2.0-yellow.svg)](https://www.apache.org/licenses/LICENSE-2.0)
![Version](https://img.shields.io/github/v/release/WebBased-WoDT/wldt-wodt-adapter?style=plastic)

A simple library that allows to use the [WLDT Framework](https://github.com/wldt) for the creation of WoDT Digital Twins in a WoDT ecosystem.

## Compatibility
| **wldt-wodt-adapter** |       wldt-core 0.2.1        |  wldt-core 0.3.0   |
|:---------------------:|:----------------------------:|:------------------:|
|       <= 2.x.y        |      :white_check_mark:      |        :x:         |
|       >= 3.x.y         |             :x:              | :white_check_mark: |

## Usage
In order to use the library add the dependency to your project:

 ```kotlin
   repositories {
        mavenCentral()
        maven {
            url = uri("https://git.informatik.uni-hamburg.de/api/v4/groups/sane-public/-/packages/maven")
        }
        maven {
            url = uri("https://maven.pkg.github.com/WebBased-WoDT/wldt-wodt-adapter")
            credentials {
                username = project.findProperty("ghPackagesUsername")?.toString() ?: ghPackageUsername
                password = project.findProperty("ghPackagesPwd")?.toString() ?: ghPackagesPwd
            }
        }
    }

    dependencies {
       implementation("io.github.webbasedwodt:wldt-wodt-adapter:<version>")
    }
```

## Documentation
- Check out the website [here](https://webbased-wodt.github.io/wldt-wodt-adapter/)
- Direct link to the *Code* documentation [here](https://webbased-wodt.github.io/wldt-wodt-adapter/documentation/code-doc/)
- Direct link to the *REST-API* documentation [here](https://webbased-wodt.github.io/wldt-wodt-adapter/documentation/openapi-doc/)
