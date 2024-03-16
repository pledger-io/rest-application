
micronaut {
    testRuntime("junit5")
}

dependencies {
    annotationProcessor(mn.lombok)
    annotationProcessor(mn.micronaut.data.processor)
    annotationProcessor(mn.micronaut.validation.processor)

    compileOnly(mn.lombok)

    implementation(mn.micronaut.jdbc.hikari)
    implementation(mn.micronaut.hibernate.jpa)
    implementation(mn.micronaut.data.tx.hibernate)
    implementation(mn.micronaut.data.jpa)
    implementation(mn.micronaut.data.jdbc)
    implementation(mn.micronaut.flyway)
    implementation(mn.validation)

    implementation(libs.otp)
    implementation(libs.lang)

    implementation(project(":core"))
    implementation(project(":domain"))

    runtimeOnly(mn.h2)
    runtimeOnly(mn.mysql.connector.java)
    runtimeOnly(mn.flyway.mysql)

    runtimeOnly(mn.snakeyaml)
    testRuntimeOnly(mn.logback.classic)
    testImplementation(mn.micronaut.test.junit5)
    testImplementation(libs.bundles.junit)
}

