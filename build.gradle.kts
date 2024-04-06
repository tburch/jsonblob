plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.20"
    id("org.jetbrains.kotlin.kapt") version "1.7.20"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.7.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("io.micronaut.application") version "2.0.2"
}

version = "1.0.2"
group = "com.jsonblob"

val kotlinVersion= project.properties["kotlinVersion"]
val testContainersVersion= project.properties["testContainersVersion"]
val jvmBrotliVersion= project.properties["jvmBrotliVersion"]

repositories {
    mavenCentral()
    jcenter()
}

micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("jsonblob.*")
    }
}

dependencies {
    implementation("io.micronaut:micronaut-validation")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut:micronaut-management")
    implementation("io.micronaut.micrometer:micronaut-micrometer-core")
    implementation("io.micronaut.micrometer:micronaut-micrometer-registry-new-relic")
    implementation("io.micronaut.aws:micronaut-aws-sdk-v2")
    implementation("software.amazon.awssdk:s3")
    implementation("com.fasterxml.uuid:java-uuid-generator:3.1.4")
    implementation("org.mongodb:mongo-java-driver:3.2.2")
    implementation("com.google.guava:guava:30.1-jre")
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.6")
    implementation("com.nixxcode.jvmbrotli:jvmbrotli:$jvmBrotliVersion")
    implementation("io.micronaut.views:micronaut-views-handlebars")
    implementation("commons-codec:commons-codec:1.15")

    runtimeOnly("ch.qos.logback:logback-classic:1.2.8")
    runtimeOnly("com.nixxcode.jvmbrotli:jvmbrotli-darwin-x86-amd64:$jvmBrotliVersion")
    runtimeOnly("com.nixxcode.jvmbrotli:jvmbrotli-linux-x86-amd64:$jvmBrotliVersion")

    testImplementation(platform("org.junit:junit-bom:5.7.1"))
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("io.micronaut.test:micronaut-test-junit5")
    testImplementation("io.micronaut:micronaut-http-client")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.skyscreamer:jsonassert:1.5.0")
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("org.testcontainers:localstack:$testContainersVersion")
    testImplementation("com.amazonaws:aws-java-sdk-s3:1.11.1030") // for localstack

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testRuntimeOnly("com.nixxcode.jvmbrotli:jvmbrotli-darwin-x86-amd64:$jvmBrotliVersion")
}


application {
    mainClass.set("jsonblob.ApplicationKt")
}
java {
    sourceCompatibility = JavaVersion.VERSION_17
}

tasks {
    test {
        useJUnitPlatform()
    }
}
