plugins {
    java
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "6.25.0"
    id("io.freefair.lombok") version "8.10"
    jacoco
}

group = "com.frauddetection"
version = "0.1.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // AWS SQS + SNS
    implementation(platform("io.awspring.cloud:spring-cloud-aws-dependencies:3.3.0"))
    implementation("io.awspring.cloud:spring-cloud-aws-starter-sqs")
    implementation("io.awspring.cloud:spring-cloud-aws-starter-sns")

    // Logging (JSON structured)
    implementation("net.logstash.logback:logstash-logback-encoder:8.0")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    executionData.setFrom(fileTree(buildDir).include("jacoco/*.exec"))
    reports {
        xml.required = true
        html.required = true
    }
}

spotless {
    java {
        googleJavaFormat()
    }
}
