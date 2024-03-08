plugins {
    id("io.micronaut.library")
    id("io.freefair.lombok")
}

micronaut {
    testRuntime("junit5")
}

dependencies {
    implementation(libs.lang)
    implementation(mn.micronaut.serde.api)
    implementation(project(":core"))

    testImplementation(libs.bundles.junit)
}
