plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("com.github.spotbugs:com.github.spotbugs.gradle.plugin:6.5.13")
    implementation("io.spring.dependency-management:io.spring.dependency-management.gradle.plugin:1.1.7")
    implementation("org.javamodularity.moduleplugin:org.javamodularity.moduleplugin.gradle.plugin:2.1.0")
    implementation("org.openjfx.javafxplugin:org.openjfx.javafxplugin.gradle.plugin:0.1.0")
    implementation("org.springframework.boot:org.springframework.boot.gradle.plugin:4.1.2")
}
