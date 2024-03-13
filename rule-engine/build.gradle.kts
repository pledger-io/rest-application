plugins {
    id("io.freefair.lombok")
}

dependencies {
    implementation(libs.lang)
    implementation(project(":core"))
    implementation(project(":domain"))

    testImplementation(libs.bundles.junit)
}
