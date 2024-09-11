plugins {
    id("io.micronaut.application")
    id("org.openapi.generator")
}

application {
    mainClass.set("com.jongsoft.finance.Application")
}

micronaut {
    runtime("jetty")
    testRuntime("junit5")
}

openApiGenerate {
    inputSpec.set(layout.buildDirectory.dir("classes/java/main/META-INF/swagger").get().file("pledger-2.0.0.yml").toString())
    outputDir.set(layout.buildDirectory.dir("asciidoc").get().toString())
    cleanupOutput.set(true)
    generatorName.set("asciidoc")
    skipValidateSpec.set(true)
}

val integration by sourceSets.creating

configurations[integration.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
configurations[integration.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())

tasks.register<Test>("itTest") {
    description = "Runs the integration tests."
    group = "verification"

    testClassesDirs = integration.output.classesDirs
    classpath = configurations[integration.runtimeClasspathConfigurationName] + integration.output + sourceSets.main.get().output

    shouldRunAfter(tasks.test)
}

tasks.jacocoTestReport {
    executionData(layout.buildDirectory.files("/jacoco/test.exec", "jacoco/itTest.exec"))
}

tasks.check {
    dependsOn("itTest")
}

tasks.withType<JavaCompile> {
    finalizedBy(tasks.getByName("openApiGenerate"))
}

configurations {
    create("api-docs")
}

val apiArtifact = artifacts.add("api-docs", layout.buildDirectory.file("asciidoc/index.adoc")) {
    type = "asciidoc"
    builtBy(tasks.getByName("openApiGenerate"))
}

publishing.publications {
    create<MavenPublication>("maven") {
        groupId = "com.jongsoft.finance"
        version = System.getProperty("version")
        from(components["java"])
        artifact(apiArtifact)
    }
}

dependencies {
    annotationProcessor(mn.micronaut.openapi.asProvider())
    annotationProcessor(mn.micronaut.http.validation)
    annotationProcessor(mn.micronaut.validation.processor)
    annotationProcessor(mn.micronaut.inject.java)

    implementation(libs.camunda)
    implementation(mn.swagger.annotations)

    implementation(mn.micronaut.validation)
    implementation(mn.micronaut.security.annotations)
    implementation(mn.micronaut.security.jwt)
    implementation(mn.micronaut.http.server.jetty)
    implementation(mn.micronaut.http.validation)
    implementation(mn.micronaut.email.template)
    implementation(mn.micronaut.views.velocity)

    // Email dependencies
    implementation(mn.micronaut.email.postmark)
    implementation(mn.micronaut.email.javamail)

    implementation(libs.bcrypt)
    implementation(libs.bouncy)
    implementation(libs.bcpkix)
    implementation(libs.otp)
    implementation(libs.lang)

    // Contains the health checker
    implementation(mn.micronaut.management)

    // Investigate if this can be swapped for micronaut serde
    implementation(mn.micronaut.jackson.databind)
    implementation(mn.micronaut.serde.jackson)

    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(project(":jpa-repository"))
    implementation(project(":rule-engine"))
    implementation(project(":bpmn-process"))
    implementation(project(":transaction-importer:transaction-importer-api"))
    implementation(project(":transaction-importer:transaction-importer-csv"))

    // needed for application.yml
    runtimeOnly(mn.snakeyaml)
    runtimeOnly(mn.eclipse.angus)
    runtimeOnly(mn.postmark)

    testImplementation(mn.micronaut.test.rest.assured)
    testImplementation(mn.micronaut.test.junit5)
    testImplementation(libs.bundles.junit)

    configurations["integrationImplementation"](libs.bundles.junit)
    configurations["integrationImplementation"](mn.micronaut.test.junit5)
    configurations["integrationImplementation"](mn.micronaut.test.rest.assured)
}
