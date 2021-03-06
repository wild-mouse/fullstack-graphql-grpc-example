import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.ofSourceSet
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

buildscript {
    dependencies {
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.8.8")
    }
}

plugins {
    id("idea")
    id("org.springframework.boot") version "2.2.2.RELEASE"
    id("io.spring.dependency-management") version "1.0.8.RELEASE"
    id("com.google.protobuf") version "0.8.8"
    id("com.google.cloud.tools.jib") version "1.8.0"
    kotlin("jvm") version "1.3.61"
    kotlin("plugin.spring") version "1.3.61"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("com.linecorp.armeria:armeria-bom:0.97.0")
        mavenBom("io.netty:netty-bom:4.1.43.Final")
    }
}

protobuf {
    protoc { artifact = "com.google.protobuf:protoc:3.11.0" }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.26.0"
        }
        id("reactorGrpc") {
            artifact = "com.salesforce.servicelibs:reactor-grpc:1.0.0"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc")
                id("reactorGrpc")
            }
        }
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("mysql:mysql-connector-java")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.salesforce.servicelibs:reactor-grpc-stub:1.0.0")
    listOf("armeria",
//            "armeria-brave",
            "armeria-grpc",
//            "armeria-jetty",
//            "armeria-kafka",
            "armeria-logback",
//            "armeria-retrofit2",
//            "armeria-rxjava",
//            "armeria-saml",
//            "armeria-spring-boot-autoconfiguration",
            "armeria-spring-boot-webflux-starter"
//            "armeria-thrift",
//            "armeria-tomcat",
//            "armeria-zookeeper"
    ).forEach {
        implementation("com.linecorp.armeria:$it:0.97.0")
    }

    implementation("io.grpc:grpc-netty-shaded:1.26.0")
    implementation("io.grpc:grpc-protobuf:1.26.0")
    implementation("io.grpc:grpc-stub:1.26.0")

    runtimeOnly("ch.qos.logback:logback-classic:1.2.3")
    runtimeOnly("org.slf4j:log4j-over-slf4j:1.7.29")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

jib {
    to {
        image = "wildmouse/greeting_api"
    }
    container {
        // TODO: make active profile variable
        args = listOf("--spring.profiles.active=local")
    }
}

