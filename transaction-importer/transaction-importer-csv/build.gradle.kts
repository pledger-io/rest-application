
micronaut {
    testRuntime("junit5")
}

dependencies {
    implementation(libs.csv)
    implementation(libs.lang)
    implementation(mn.micronaut.serde.jackson)

    implementation(project(":transaction-importer:transaction-importer-api"))
    implementation(project(":core"))
    implementation(project(":domain"))

    testImplementation(libs.bundles.junit)
    testRuntimeOnly(mn.logback.classic)
}