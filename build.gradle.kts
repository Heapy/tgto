import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm").version(kotlinVersion)
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = jvmTarget
    targetCompatibility = jvmTarget
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = jvmTarget
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

    implementation(komodoConcurrent)
    implementation(komodoDotenv)

    implementation(logback)
    implementation(julSlf4j)

    implementation(hikari)
    implementation(postgresql)
    implementation(jooq)
    implementation(jooqMeta)

    testImplementation(junitApi)
    testRuntimeOnly(junitEngine)
    testImplementation(mockk)
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.kotlin.link") }
}
