# TWT Testbed Server

This project is a CoAP server implemented in Java using the Californium library.

It runs two servers:
- A CoAP server on port 5683.
- A secure CoAPs server using DTLS on port 5684.

The secure server uses PSK (Pre-Shared Key) authentication with the following credentials:
- **ID**: `twttestbed`
- **PSK**: `secretkey`

This server is used by the TWT Testbed application:

- [TWT Testbed](https://github.com/svankappel/twt-testbed)

## Prerequisites

To run the server, you need to have Java installed on your system.

### Installing Java

This project requires Java 11. You can install OpenJDK 11 on Ubuntu using the following commands:

```sh
sudo apt update
sudo apt install openjdk-11-jdk
```

Verify the installation by checking the Java version:

```sh
java --version
```

You should see an output similar to:

```sh
openjdk 11.0.25 2024-10-15
OpenJDK Runtime Environment (build 11.0.25+9-post-Ubuntu-1ubuntu124.04)
OpenJDK 64-Bit Server VM (build 11.0.25+9-post-Ubuntu-1ubuntu124.04, mixed mode, sharing)
```

## Running the Pre-Built Executable

A pre-built executable JAR file is included in the repository. You can run the server without having to compile the project.

### Run
To run the server, use the following command:

```sh
java -jar twt-testbed-server.jar
```

## Building the Project
To build the project and create an executable JAR file with dependencies, you need to have Maven installed.

### Installing Maven
Maven is required to build and manage the project dependencies. You can install Maven on Ubuntu using the following commands:

```sh
sudo apt update
sudo apt install maven
```

Verify the installation by checking the Maven version:

```sh
mvn -v
```

You should see an output similar to:

```sh
Apache Maven 3.8.7
Maven home: /usr/share/maven
Java version: 11.0.25, vendor: Ubuntu, runtime: /usr/lib/jvm/java-11-openjdk-amd64
Default locale: en_US, platform encoding: UTF-8
OS name: "linux", version: "6.8.0-49-generic", arch: "amd64", family: "unix"
```

### Build

To download the dependencies and build the project, use the following command:

```sh
mvn clean package
```
This will generate a JAR file that includes the dependencies in the ```target``` directory, typically named ```twt-testbed-server-1.0-SNAPSHOT-jar-with-dependencies.jar```.

### Run

To run the server, use the following command:

```sh
java -jar target/twt-testbed-server-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Liscence

### Third-Party Licenses

This project uses the Californium library, which is licensed under the Eclipse Public License (EPL) and the Eclipse Distribution License (EDL).

- [Eclipse Public License (EPL)](https://www.eclipse.org/legal/epl-2.0/)
- [Eclipse Distribution License (EDL)](https://www.eclipse.org/org/documents/edl-v10.php)

### Acknowledgements

This project uses the following third-party libraries:

- [Californium (Cf)](https://www.eclipse.org/californium/) - A Java CoAP library