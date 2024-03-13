plugins {
    id("io.micronaut.application")
    id("io.freefair.lombok")
}
//mainClassName = "com.jongsoft.finance.Application"


//jar {
//    manifest {
//        attributes('Main-Class': 'com.jongsoft.finance.Application')
//    }
//}

dependencies {
    annotationProcessor("io.micronaut.openapi:micronaut-openapi")

    implementation(libs.camunda)
    implementation(mn.swagger.annotations)
    implementation(mn.micronaut.security.annotations)
    implementation(mn.micronaut.security.jwt)
    implementation(mn.micronaut.http.server.jetty)

    implementation(mn.micronaut.hibernate.validator)

    implementation("at.favre.lib:bcrypt:0.9.0")
    implementation(libs.bouncy)
    implementation(libs.bcpkix)
    implementation(libs.otp)
    implementation(libs.lang)

    // Contains the health checker
    implementation(mn.micronaut.management)

    // needed for application.yml
    runtimeOnly(mn.snakeyaml)

    // Investigate if this can be swapped for micronaut serde
    implementation(mn.micronaut.jackson.databind)

    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(project(":jpa-repository"))
    implementation(project(":rule-engine"))
    implementation(project(":bpmn-process"))

    testImplementation(mn.micronaut.test.rest.assured)
    testImplementation(mn.micronaut.test.junit5)
    testImplementation(libs.bundles.junit)
}
