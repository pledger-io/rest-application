micronaut {
    testRuntime("junit5")
}

dependencies {
    annotationProcessor(mn.lombok)

    implementation(libs.lang)
    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(project(":learning:learning-module"))

    compileOnly(mn.lombok)

    testImplementation(libs.bundles.junit)
}
