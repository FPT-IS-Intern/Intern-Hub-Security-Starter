plugins {
    java
    id("java-library")
    id("maven-publish")
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.intern.hub.starter"
version = "1.0.6"
description = "security-starter"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:4.0.2")
    }
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    api("org.springframework.boot:spring-boot-starter-security")
    api("org.springframework.boot:spring-boot-autoconfigure")
    api("org.springframework:spring-context")

    implementation("org.springframework:spring-web")
    implementation("org.springframework:spring-aop")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.aspectj:aspectjweaver")
    implementation("org.slf4j:slf4j-api")
    implementation("jakarta.servlet:jakarta.servlet-api")
    implementation("jakarta.persistence:jakarta.persistence-api")
    implementation("org.springframework.data:spring-data-jpa")

    implementation("com.github.FPT-IS-Intern:Intern-Hub-Common-Library:2.0.3")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

// Configure jar task to handle duplicate spring-configuration-metadata.json files
tasks.named<org.gradle.jvm.tasks.Jar>("jar") {
    // Set duplicate strategy to allow both auto-generated and additional metadata
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

publishing {
    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
            groupId = group.toString()
            artifactId = "security"
            version = version.toString()
        }
    }
    repositories {
        mavenLocal()
    }
}