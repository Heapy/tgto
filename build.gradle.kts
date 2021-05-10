import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm").version(kotlinVersion)
    kotlin("plugin.serialization").version(kotlinVersion)
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = jvmTarget
    targetCompatibility = jvmTarget
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = jvmTarget
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

application {
    applicationName = "tgto"
    mainClass.set("io.heapy.tgto.Application")
}

dependencies {
    implementation(kotlinStdlib)
    implementation(telegrambots)
    implementation(undertow)
    implementation(coroutines)
    implementation(rome)
    implementation(commonmark)
    implementation(xodus)
    implementation(mapdb)
    implementation(kotlinxSerialization)

    implementation(logback)
    implementation(julSlf4j)

    testImplementation(junitApi)
    testRuntimeOnly(junitEngine)
    testImplementation(mockk)
}

repositories {
    mavenCentral()
}
