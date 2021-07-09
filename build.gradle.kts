plugins {
    id("org.springframework.boot") version "2.4.7"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.jlleitschuh.gradle.ktlint") version "10.1.0"
    id("com.palantir.docker") version "0.26.0"
    kotlin("jvm") version "1.5.10"
    kotlin("plugin.spring") version "1.5.10"
    jacoco
}

group = "cz.loono"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

val googleApiVersion = "1.32.1"

dependencies {
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")

    implementation("com.google.api-client:google-api-client:$googleApiVersion")
    implementation("com.google.api-client:google-api-client-appengine:$googleApiVersion")
    implementation("com.google.api-client:google-api-client-gson:$googleApiVersion")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

val artifactFinalName = "$name.jar"
tasks.bootJar {
    archiveFileName.set(artifactFinalName)
    destinationDirectory.set(file("$buildDir/dists"))
}

docker {
    name = "loono/loono-be"
    setDockerfile(File(rootDir, "Dockerfile"))
    files(tasks.bootJar)
    copySpec.rename(artifactFinalName, "build/dists/$artifactFinalName")
    noCache(true)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    reports {
        csv.required.set(true)
    }
}
