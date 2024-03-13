# FinTrack
![Sonatype Nexus (Releases)](https://img.shields.io/nexus/r/com.jongsoft.finance/fintrack-api?server=https%3A%2F%2Foss.sonatype.org)
![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/com.jongsoft.finance/fintrack-api?server=https%3A%2F%2Foss.sonatype.org)
![Bitbucket Pipelines](https://img.shields.io/bitbucket/pipelines/jongsoftdev/fintrack-application/master)
[![SonarCloud Quality](https://sonarcloud.io/api/project_badges/measure?project=FinTrack%3AAPI&metric=alert_status&style=flat-square)](https://sonarcloud.io/dashboard?id=FinTrack%3AAPI)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=FinTrack%3AAPI&metric=coverage&style=flat-square)](https://sonarcloud.io/dashboard?id=FinTrack%3AAPI)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

-----------------------

**[Read the documentation](https://www.pledger.io/)**

[Report Bug](https://jongsoftdev.atlassian.net/issues/?jql=issuetype%20%3D%20Bug%20AND%20project%20%3D%20FIN%20AND%20resolution%20%3D%20Unresolved%20ORDER%20BY%20priority%20DESC) - [Request Feature](https://jongsoftdev.atlassian.net/browse/FIN-23?jql=issuetype%20%3D%20Story%20AND%20project%20%3D%20FIN%20AND%20resolution%20%3D%20Unresolved%20ORDER%20BY%20priority%20DESC)

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

## License
Copyright 2024 Jong Soft Development

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and 
associated documentation files (the "Software"), to deal in the Software without restriction, including 
without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to 
the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial 
portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE 
OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
