# Pledger.io
![Sonatype Nexus (Releases)](https://img.shields.io/github/v/release/pledger-io/rest-application?display_name=release&label=Stable)
![Release Build](https://github.com/pledger-io/rest-application/actions/workflows/release-build.yml/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=pledger-io_rest-application&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=pledger-io_rest-application)
![Coverage](https://img.shields.io/sonar/coverage/pledger-io_rest-application?server=https%3A%2F%2Fsonarcloud.io
)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

-----------------------

**[Read the documentation](https://www.pledger.io/)**

-----------------------

## About Pledger.io
Pledger.io is a "self hosted" application that helps in keeping track of your personal finances.
It helps you keep track of your income and expenses to allow you to spend less many and save more.

Using Pledger.io you can keep track of your income vs spending as well as your credit card expenses.
Pledger.io has the following features:

* Categories to group expenses
* Budgets to track income vs. expenses
* Contract notifications
* Importing transactions from bank exports

## Get started

**Note:** the front-end application can be found in the [user-interface](https://github.com/pledger-io/user-interface) repository. The [deployment](https://github.com/pledger-io/build-tooling) build will bundle the front-end with this backend system.

In this repository you will find the backend REST application needed to run Pledger.io.

### Building the source

To build the application the following needs to be present on your local PC:

* JDK 21 or higher
* GIT for completing the checkout

You can build the application using the Gradle command:

    ./gradlew build

### Running the backend

The generated phat JAR in the ```fintrack-api``` module can be started, which will run the backend of Pledger.io. To access the
API documentation use the url:

    http://localhost:8080/spec/index.html

