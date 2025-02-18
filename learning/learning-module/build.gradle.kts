micronaut {
    testRuntime("junit5")
}

java {
    modularity.inferModulePath.set(true)
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))

    testRuntimeOnly(mn.logback.classic)
    testImplementation(libs.bundles.junit)
}