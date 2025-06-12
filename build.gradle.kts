repositories {
    mavenCentral()
}

plugins {
    java
    application
    id("org.danilopianini.gradle-java-qa") version "1.87.0"
    id("com.gradleup.shadow") version "8.3.6"
}

dependencies {
    // Use JUnit Jupiter for testing.
    val junitVersion = "5.13.1"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    // SpotBugs
    compileOnly("com.github.spotbugs:spotbugs-annotations:4.9.3")
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging { events("passed", "skipped", "failed") }
    testLogging.showStandardStreams = true
}

application {
    mainClass = "it.ristorantelorma.RistoranteLorMa"
}
