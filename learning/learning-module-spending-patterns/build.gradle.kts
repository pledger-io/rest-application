micronaut {
    testRuntime("junit5")
}

java {
    modularity.inferModulePath.set(true)
}

dependencies {
    annotationProcessor(mn.lombok)
    annotationProcessor(mn.micronaut.data.processor)
    annotationProcessor(mn.micronaut.validation.processor)

    compileOnly(mn.lombok)

    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(project(":learning:learning-module"))
    implementation(libs.lang)
    implementation(llm.bundles.embeddings)

//    implementation(mn.micronaut.jdbc.hikari)
//    implementation(mn.micronaut.hibernate.jpa)
//    implementation(mn.micronaut.data.tx.hibernate)
    implementation(mn.micronaut.data.jpa)
//    implementation(mn.micronaut.data.jdbc)
    implementation(mn.micronaut.micrometer.core)
    implementation(mn.micronaut.runtime)
    implementation(mn.validation)

    // Machine learning dependencies
    implementation("org.apache.commons:commons-math3:3.6.1")

    testRuntimeOnly(mn.logback.classic)
    testImplementation(libs.bundles.junit)}
