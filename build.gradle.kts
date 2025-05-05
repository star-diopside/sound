plugins {
    `java-library`
    checkstyle
    id("com.github.spotbugs") version "6.1.9"
    id("org.springframework.boot") version "3.4.5" apply false
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openjfx.javafxplugin") version "0.1.0" apply false
    id("org.javamodularity.moduleplugin") version "1.8.15" apply false
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "checkstyle")
    apply(plugin = "com.github.spotbugs")

    repositories {
        mavenCentral()
    }

    val javafxVersion by extra { "24.0.1" }

    dependencyManagement {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        }
        dependencies {
            dependency("org.controlsfx:controlsfx:11.2.2")
            dependency("org:jaudiotagger:2.0.3")
            dependency("com.googlecode.soundlibs:mp3spi:1.9.5.4")
            dependency("com.google.guava:guava:33.4.8-jre")
        }
    }

    dependencies {
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")
        testCompileOnly("org.projectlombok:lombok")
        testAnnotationProcessor("org.projectlombok:lombok")
    }

    checkstyle {
        configFile = file("${rootDir}/config/checkstyle/checkstyle.xml")
        isIgnoreFailures = true
    }

    spotbugs {
        toolVersion = "4.9.3"
        ignoreFailures = true
    }

    tasks.checkstyleMain {
        exclude("**/module-info.java")
    }

    tasks.spotbugsMain {
        reports {
            create("html")
        }
    }
}
