val logbackVersion = "1.2.10"
val hibernateVersion = "5.6.5.Final"

plugins {
    id("org.springframework.boot") version "2.6.6"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    id("org.openapi.generator") version "5.4.0"
    id("de.undercouch.download") version "5.0.1"
    id("org.owasp.dependencycheck") version "6.5.3"
    id("com.avast.gradle.docker-compose") version "0.15.1"
    id("org.flywaydb.flyway") version "8.5.2"
    kotlin("jvm") version "1.6.20-RC"
    kotlin("plugin.spring") version "1.6.20-RC"
    kotlin("plugin.jpa") version "1.6.20-RC"
    jacoco
}

group = "cz.loono"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

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

    implementation("com.google.firebase:firebase-admin:8.1.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")

    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("ch.qos.logback:logback-core:$logbackVersion")

    runtimeOnly("org.postgresql:postgresql")
    implementation("org.hibernate:hibernate-envers:$hibernateVersion")
    implementation("org.hibernate:hibernate-entitymanager:$hibernateVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.flywaydb:flyway-core:8.5.1")

    testRuntimeOnly("com.h2database:h2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
}

val artifactFinalName = "loono-be.jar"
tasks.bootJar {
    archiveFileName.set(artifactFinalName)
    destinationDirectory.set(file("$buildDir/dists"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    maxHeapSize = "2048m"
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

    tasks.openApiGenerate.configure {
        dependsOn("downloadOpenApiSpec")
    }

    val localSpecFile = projectDir.toPath().resolve("src/main/resources/doc/openapi.json")

    tasks.register<de.undercouch.gradle.tasks.download.Download>("downloadOpenApiSpec") {
        src("https://raw.githubusercontent.com/cesko-digital/loono-api/main/openapi.json")
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
        typeMappings.put("java.time.OffsetDateTime", "java.time.LocalDateTime")
    }
}

dependencyCheck {
    failBuildOnCVSS = 0.0f
    suppressionFile = "cve-suppress.xml"
}

dockerCompose {
    useComposeFiles.set(
        listOf("../docker-compose.yml")
    )
    isRequiredBy(tasks.test)
}

flyway {
    url = "jdbc:postgresql://localhost:5432/postgres"
    user = "postgres"
    password = "postgres"
    locations = arrayOf("classpath:db/migration")
}
