rootProject.name="pledger-io"

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage") // It's gradle, any of their APIs can be considered unstable
    repositories {
        mavenCentral()
    }

    versionCatalogs {
        create("mn") {
            from("io.micronaut.platform:micronaut-platform:5.1.5")
        }
    }
}
