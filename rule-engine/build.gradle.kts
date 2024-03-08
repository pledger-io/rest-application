plugins {
    id("io.micronaut.library")
    id("io.freefair.lombok")
}

dependencies {
    implementation(libs.lang)
    implementation(project(":core"))
    implementation(project(":domain"))
}
