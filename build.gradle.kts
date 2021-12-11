plugins {
    kotlin("jvm") version "1.4.32"
    id("maven-publish")
    //id("org.jlleitschuh.gradle.ktlint")
}

group = "com.pjcampi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("com.ximpleware", "vtd-xml", "2.13.4")

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
    testImplementation(platform("org.junit:junit-bom:5.8.1"))
    testImplementation("org.junit.jupiter", "junit-jupiter")
    testImplementation("org.junit.jupiter", "junit-jupiter-params")
    testImplementation("com.willowtreeapps.assertk", "assertk-jvm", "0.23.1")

    implementation("org.apache.logging.log4j", "log4j-api-kotlin", "1.0.0")
    implementation(platform("org.apache.logging.log4j:log4j-bom:2.11.0"))
    implementation("org.apache.logging.log4j", "log4j-api")
    testImplementation("org.apache.logging.log4j", "log4j-core")
    testRuntimeOnly("org.apache.logging.log4j", "log4j-jul")
}