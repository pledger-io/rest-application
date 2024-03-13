plugins {
    id("io.micronaut.library") apply false
    id("maven-publish")
    id("signing")
    id("java")
}

var isCiCd = System.getProperty("cicd") == "true"

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.micronaut.library")

    apply(plugin = "signing")
    apply(plugin = "maven-publish")

    if (isCiCd) {
        // when running in CI/CD environment we need to build with sources and JavaDoc and publish to maven central
        java {
            withSourcesJar()
            withJavadocJar()
        }

        tasks.javadoc {
            options {
                // silence the Javadoc generation
                this as StandardJavadocDocletOptions
                addBooleanOption("Xdoclint:none", true)
            }
        }

        publishing {
            publications {
                create<MavenPublication>("maven") {
                    groupId = "com.jongsoft.finance"
                    version = "3.3.0-SNAPSHOT"

                    from(components["java"])

                    pom {
                        description = "The REST-API for Pledger.ioS"
                        url = "https://www.pledger.io/"
                        name = project.name
                        scm {
                            connection = "scm:git:git@bitbucket.org:jongsoftdev/fintrack-application.git"
                            developerConnection = "scm:git:git@bitbucket.org:jongsoftdev/fintrack-application.git"
                            url = "https://bitbucket.org/jongsoftdev/fintrack-application/src/master/"
                        }
                        licenses {
                            license {
                                name = "MIT License"
                                url = "http://www.opensource.org/licenses/mit-license.php"
                            }
                        }
                        developers {
                            developer {
                                id = "gjong"
                                name = "Gerben Jongerius"
                                email = "g.jongerius@jong-soft.com"
                            }
                        }
                    }
                }
            }

            repositories {
                maven {
                    url = uri(layout.buildDirectory.dir("repo"))

                    if (System.getenv("CI") == "true")
                        credentials {
                            username = System.getenv("OSSRH_USERNAME")
                            password = System.getenv("OSSRH_PASSWORD")
                        }
                }
            }
        }

        signing {
            sign(publishing.publications["maven"])
        }
    }
}