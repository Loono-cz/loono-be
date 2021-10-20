plugins {
    id("org.springframework.boot") version "2.5.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.jlleitschuh.gradle.ktlint") version "10.1.0"
    id("org.openapi.generator") version "5.2.1"
    id("de.undercouch.download") version "4.1.2"
    kotlin("jvm") version "1.5.20"
    kotlin("plugin.spring") version "1.5.20"
    jacoco
}

group = "cz.loono"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("reflect"))
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-security")

    implementation("com.google.firebase:firebase-admin:8.0.0")

    implementation("org.slf4j:slf4j-api:1.7.31")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("ch.qos.logback:logback-core:1.2.3")

    runtimeOnly("org.postgresql:postgresql")

    testRuntimeOnly("com.h2database:h2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.0")
}

val artifactFinalName = "loono-be.jar"
tasks.bootJar {
    archiveFileName.set(artifactFinalName)
    destinationDirectory.set(file("$buildDir/dists"))
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

setUpOpenApiGenerator()

fun setUpOpenApiGenerator() {
    tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask> {
        dependsOn("openApiGenerate")
    }

    tasks.compileKotlin.configure {
        dependsOn("openApiGenerate")
    }

    val localSpecFile = projectDir.toPath().resolve("src/main/resources/doc/openapi.json")

    tasks.register<de.undercouch.gradle.tasks.download.Download>("downloadOpenApiSpec") {
        src("https://stoplight.io/api/v1/projects/okarmazin/loono/nodes/openapi.json")
        dest(localSpecFile.toFile())
    }

    openApiGenerate {
        val outputPath = project.buildDir.toPath().resolve("generated/openapi")
        val sourceSetPath = outputPath.resolve("src/main/kotlin")

        sourceSets.main {
            java.srcDir(sourceSetPath)
        }

        ktlint {
            filter {
                exclude { element ->
                    if (element.isDirectory) return@exclude false

                    val file = sourceSetPath.resolve(element.path).toFile()
                    file.exists()
                }
            }
        }

        outputDir.set(outputPath.toString())
        modelPackage.set("cz.loono.backend.api.dto")
        modelNameSuffix.set("Dto")
        generatorName.set("kotlin-spring")
        configOptions.put("enumPropertyNaming", "original")
        configOptions.put("serializationLibrary", "jackson")
        globalProperties.set(
            mapOf(
                "apis" to "false",
                "models" to "",
            )
        )
        inputSpec.set(localSpecFile.toString())
    }
}
