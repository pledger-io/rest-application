micronaut {
    testRuntime("junit5")
}

tasks.compileJava {
    options.compilerArgs.add("-Amicronaut.jsonschema.baseUri=https://www.pledger.io/schemas") // (1)
}

dependencies {
    annotationProcessor(mn.micronaut.json.schema.processor)
    annotationProcessor(mn.lombok)

    implementation(libs.lang)
    implementation(libs.lang.xml)
    implementation(libs.camunda)

    implementation(libs.bouncy)
    implementation(libs.csv)

    compileOnly(mn.lombok)
    testCompileOnly(mn.lombok)

    // Investigate if this can be swapped for micronaut serde
    implementation(mn.micronaut.jackson.databind)
    implementation(mn.micronaut.serde.jackson)
    implementation(mn.validation)
    implementation(mn.micronaut.json.schema.annotations)

    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(project(":learning:rule-engine"))
    implementation(project(":transaction-importer:transaction-importer-api"))

    // needed for the testing of the application
    runtimeOnly(mn.h2)
    runtimeOnly(mn.snakeyaml)
    testRuntimeOnly(mn.logback.classic)
    testImplementation(mn.micronaut.test.junit5)
    testImplementation(libs.bundles.junit)
    testRuntimeOnly(project(":transaction-importer:transaction-importer-csv"))
}
