micronaut {
    testRuntime("junit5")
}

dependencies {
    implementation(mn.micronaut.runtime)
    implementation(mn.validation)
    implementation(libs.lang)

    testImplementation(mn.micronaut.test.junit5)
    testImplementation(libs.bundles.junit)
}
