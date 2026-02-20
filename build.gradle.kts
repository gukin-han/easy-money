plugins {
    java
    id("org.springframework.boot") version "4.0.2"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.easymoney"
version = "0.0.1-SNAPSHOT"
description = "easymoney"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.ai:spring-ai-starter-model-openai:2.0.0-M2")
    compileOnly("org.projectlombok:lombok")
    runtimeOnly("com.mysql:mysql-connector-j")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.20.4"))
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.awaitility:awaitility:4.2.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// --- Frontend build tasks (skipped when frontend/ dir is absent, e.g. Docker) ---
val frontendDir = file("frontend")

if (frontendDir.resolve("package.json").exists()) {
    val frontendInstall by tasks.registering(Exec::class) {
        workingDir = frontendDir
        commandLine("npm", "install")
        inputs.file(frontendDir.resolve("package.json"))
        inputs.file(frontendDir.resolve("package-lock.json"))
        outputs.dir(frontendDir.resolve("node_modules"))
    }

    val frontendBuild by tasks.registering(Exec::class) {
        dependsOn(frontendInstall)
        workingDir = frontendDir
        commandLine("npm", "run", "build")
        inputs.dir(frontendDir.resolve("src"))
        inputs.file(frontendDir.resolve("index.html"))
        inputs.file(frontendDir.resolve("vite.config.ts"))
        inputs.file(frontendDir.resolve("tsconfig.json"))
        inputs.file(frontendDir.resolve("tsconfig.app.json"))
        outputs.dir(frontendDir.resolve("dist"))
    }

    val copyFrontend by tasks.registering(Copy::class) {
        dependsOn(frontendBuild)
        from(frontendDir.resolve("dist"))
        into(layout.buildDirectory.dir("resources/main/static"))
    }

    tasks.named("processResources") {
        dependsOn(copyFrontend)
    }
}
