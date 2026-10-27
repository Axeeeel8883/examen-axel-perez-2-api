plugins {
 kotlin("jvm") version "2.1.21"
 kotlin("plugin.spring") version "2.1.21"
 kotlin("plugin.jpa") version "2.1.21"
 id("org.springframework.boot") version "3.4.5"
 id("io.spring.dependency-management") version "1.1.7"
}
group = "pe.upeu"
version = "1.0.0"
repositories { mavenCentral() }
kotlin { jvmToolchain(21) }
dependencies {
 implementation("org.springframework.boot:spring-boot-starter-web")
 implementation("org.springframework.boot:spring-boot-starter-security")
 implementation("org.springframework.boot:spring-boot-starter-data-jpa")
 implementation("org.springframework.boot:spring-boot-starter-validation")
 implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
 implementation("org.jetbrains.kotlin:kotlin-reflect")
 implementation("io.jsonwebtoken:jjwt-api:0.12.6")
 runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
 runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
 runtimeOnly("com.h2database:h2")
 testImplementation("org.springframework.boot:spring-boot-starter-test")
 testImplementation("org.springframework.security:spring-security-test")
}
tasks.test { useJUnitPlatform() }
