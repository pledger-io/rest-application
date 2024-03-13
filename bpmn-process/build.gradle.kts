micronaut {
    testRuntime("junit5")
}

dependencies {
    annotationProcessor(mn.lombok)
    testAnnotationProcessor(mn.lombok)

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

    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(project(":rule-engine"))

    runtimeOnly(mn.h2)
    runtimeOnly(mn.snakeyaml)
    implementation(mn.validation)

    testRuntimeOnly(mn.logback.classic)
    testImplementation(mn.micronaut.test.junit5)
    testImplementation(libs.bundles.junit)
}
