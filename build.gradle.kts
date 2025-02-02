plugins {
    `java-library`
    checkstyle
    id("com.github.spotbugs") version "6.0.27"
    id("org.springframework.boot") version "3.4.1" apply false
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

    val javafxVersion by extra { "21.0.5" }

    dependencyManagement {
        imports {
            mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        }
        dependencies {
            dependency("com.google.guava:guava:33.4.0-jre")
            dependency("org.controlsfx:controlsfx:11.2.1")
            dependency("org:jaudiotagger:2.0.3")
            dependency("io.github.jseproject:jse-spi-flac:1.0.1")
            dependency("io.github.jseproject:jse-spi-opus:1.0.1")
            dependency("io.github.jseproject:jse-spi-vorbis:1.0.1")
            dependency("io.github.jseproject:jse-spi-speex:1.0.1")
            dependency("io.github.jseproject:jse-spi-mp3:1.0.2")
            dependency("io.github.jseproject:jse-spi-aac:1.0.2")
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
