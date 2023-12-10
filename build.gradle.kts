plugins {
    `java-library`
    checkstyle
    id("com.github.spotbugs") version "6.0.2"
    id("org.springframework.boot") version "3.2.0" apply false
    id("io.spring.dependency-management") version "1.1.4"
    id("org.openjfx.javafxplugin") version "0.1.0" apply false
    id("org.javamodularity.moduleplugin") version "1.8.12" apply false
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "checkstyle")
    apply(plugin = "com.github.spotbugs")

    repositories {
        mavenCentral()
    }

    val javafxVersion by extra { "21.0.1" }

    dependencyManagement {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        }
        dependencies {
            dependency("org.controlsfx:controlsfx:11.1.2")
            dependency("org:jaudiotagger:2.0.3")
            dependency("com.googlecode.soundlibs:mp3spi:1.9.5.4")
            dependency("com.google.guava:guava:32.1.3-jre")
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
        ignoreFailures = true
    }

    tasks.compileJava {
        options.encoding = "UTF-8"
    }

    tasks.compileTestJava {
        options.encoding = "UTF-8"
    }

    tasks.javadoc {
        options.encoding = "UTF-8"
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
