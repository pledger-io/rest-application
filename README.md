# FinTrack
![Image version](https://img.shields.io/docker/v/gjong/fintrack?sort=semver)
[![SonarCloud Quality](https://sonarcloud.io/api/project_badges/measure?project=com.jongsoft.finance%3Acashflow&metric=alert_status&?style=flat-square)](https://sonarcloud.io/dashboard?id=com.jongsoft.finance%3Acashflow)
![Bitbucket Pipelines](https://img.shields.io/bitbucket/pipelines/jongsoftdev/fintrack-application/master)
[![codecov](https://codecov.io/bb/jongsoftdev/fintrack-application/branch/master/graph/badge.svg)](https://codecov.io/bb/jongsoftdev/fintrack-application)
![APMLicense](https://img.shields.io/apm/l/vim-mode.svg?style=flat-square)

-----------------------

**[Read the documentation](https://fintrack.jongsoft.com/)**

[Report Bug](https://jongsoftdev.atlassian.net/issues/?jql=issuetype%20%3D%20Bug%20AND%20project%20%3D%20FIN%20AND%20resolution%20%3D%20Unresolved%20ORDER%20BY%20priority%20DESC) - [Request Feature](https://jongsoftdev.atlassian.net/browse/FIN-23?jql=issuetype%20%3D%20Story%20AND%20project%20%3D%20FIN%20AND%20resolution%20%3D%20Unresolved%20ORDER%20BY%20priority%20DESC)

-----------------------

## About FinTrack
FinTrack is a "self hosted" application that helps in keeping track of your personal finances. It helps you keep track
of your income and expenses to allow you to spend less many and save more.

Using FinTrack you can keep track of your income vs spending as well as your credit card expenses. FinTrack has the 
following features:

* Categories to group expenses
* Budgets to track income vs. expenses
* Contract notifications
* Importing transactions from bank exports

## Get started

**Note:** the front-end application can be found in the [fintrack-ui](https://bitbucket.org/jongsoftdev/fintrack-ui) repository.

In this repository you will find the backend REST application needed to run FinTrack.

### Building the source

To build the application the following needs to be present on your local PC:

* JDK 14 or higher
* GIT for completing the checkout

You can build the application using the Gradle command:

    ./gradlew build

### Running the backend

The generated phat JAR in the ```fintrack-api``` module can be started, which will run the backend of FinTrack. To access the
API documentation use the url:

    http://localhost:8080/openapi/index.html

## License
Copyright 2020 Jong Soft Development

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
