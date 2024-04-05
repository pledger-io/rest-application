dependencies {
    annotationProcessor(mn.lombok)

    implementation(libs.lang)
    implementation(project(":core"))
    implementation(project(":domain"))

    compileOnly(mn.lombok)

    testImplementation(libs.bundles.junit)
}
