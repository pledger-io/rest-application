import java.util.*

micronaut {
    testRuntime("junit5")
}

java {
    modularity.inferModulePath.set(true)
}

tasks.withType<Test> {
    jvmArgs = listOf("--add-modules", "jdk.incubator.vector", "--enable-native-access=ALL-UNNAMED")
}

tasks.withType<JavaExec> {
    jvmArgs = listOf("--add-modules", "jdk.incubator.vector")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))

    implementation(libs.lang)

    implementation(llm.bundles.langchain4j)
    implementation(variantOf(llm.native) {
        classifier(getArchetype())
    })

    testRuntimeOnly(mn.logback.classic)
    testImplementation(libs.bundles.junit)
}

fun getArchetype(): String {
    val osName = System.getProperty("os.name").lowercase(Locale.getDefault())
    val osArch = System.getProperty("os.arch").lowercase(Locale.getDefault())

    val osPart = when {
        osName.contains("win") -> "windows"
        osName.contains("mac") -> "macos"
        osName.contains("nix") || osName.contains("nux") || osName.contains("aix") -> "linux"
        else -> "unknown"
    }

    val archPart = when {
        osArch.contains("64") -> "x86_64"
        osArch.contains("aarch64") -> "aarch_64"
        else -> "unknown"
    }

    return "$osPart-$archPart"
}