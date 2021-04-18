import org.springframework.boot.gradle.plugin.SpringBootPlugin
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    `java-library`
    application
    checkstyle
    id("com.github.spotbugs") version "4.7.0"
    id("org.springframework.boot") version "2.4.5" apply false
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
    id("org.openjfx.javafxplugin") version "0.0.9"
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "checkstyle")
    apply(plugin = "com.github.spotbugs")

    repositories {
        mavenCentral()
    }

    val javafxVersion by extra { "16" }

    dependencyManagement {
        imports {
            mavenBom(SpringBootPlugin.BOM_COORDINATES)
        }
        dependencies {
            dependency("org.controlsfx:controlsfx:11.1.0")
            dependency("org:jaudiotagger:2.0.3")
            dependency("com.googlecode.soundlibs:mp3spi:1.9.5.4")
        }
    }

    dependencies {
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")
        testCompileOnly("org.projectlombok:lombok")
        testAnnotationProcessor("org.projectlombok:lombok")
    }

    checkstyle {
        toolVersion = "8.41.1"
        configFile = file("${rootDir}/config/checkstyle/checkstyle.xml")
        isIgnoreFailures = true
    }

    spotbugs {
        toolVersion.set("4.2.2")
        ignoreFailures.set(true)
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
            create("html") {
                enabled = true
            }
        }
    }
}

project(":sound-service") {
    dependencies {
        api("org.springframework.boot:spring-boot-starter")
        implementation(project(":sound-util"))
        implementation(project(":sound-compatibility"))
    }
}

project(":sound-compatibility") {
    tasks.jar {
        manifest {
            attributes("Automatic-Module-Name" to "jp.gr.java_conf.stardiopside.sound.compatibility")
        }
    }

    dependencies {
        implementation("org:jaudiotagger")
    }
}

project(":sound-console") {
    apply(plugin = "application")

    application {
        mainModule.set("jp.gr.java_conf.stardiopside.sound.console")
        mainClass.set("jp.gr.java_conf.stardiopside.sound.Console")
    }

    dependencies {
        implementation(project(":sound-service"))
        implementation(project(":sound-util"))
        runtimeOnly(files("${rootDir}/libs/jaad-0.8.4.jar"))
        runtimeOnly("com.googlecode.soundlibs:mp3spi")
    }
}

project(":sound-console-boot") {
    apply(plugin = "org.springframework.boot")

    tasks.getByName<BootJar>("bootJar") {
        mainClass.set("jp.gr.java_conf.stardiopside.sound.Console")
    }

    dependencies {
        runtimeOnly(project(":sound-console"))
    }
}

project(":sound-app") {
    apply(plugin = "application")
    apply(plugin = "org.openjfx.javafxplugin")

    application {
        mainModule.set("jp.gr.java_conf.stardiopside.sound.app")
        mainClass.set("jp.gr.java_conf.stardiopside.sound.App")
    }

    val javafxVersion: String by extra

    javafx {
        version = javafxVersion
        modules("javafx.controls", "javafx.fxml")
    }

    dependencies {
        implementation(project(":sound-service"))
        implementation("org.controlsfx:controlsfx")
        runtimeOnly(files("${rootDir}/libs/jaad-0.8.4.jar"))
        runtimeOnly("com.googlecode.soundlibs:mp3spi")
    }
}

project(":sound-app-boot") {
    apply(plugin = "org.springframework.boot")

    tasks.getByName<BootJar>("bootJar") {
        mainClass.set("jp.gr.java_conf.stardiopside.sound.App")
    }

    dependencies {
        runtimeOnly(project(":sound-app"))
    }
}

project(":sound-checker") {
    apply(plugin = "application")
    apply(plugin = "org.openjfx.javafxplugin")

    application {
        mainModule.set("jp.gr.java_conf.stardiopside.sound.checker")
        mainClass.set("jp.gr.java_conf.stardiopside.sound.SoundChecker")
    }

    val javafxVersion: String by extra

    javafx {
        version = javafxVersion
        modules("javafx.controls", "javafx.fxml")
    }

    dependencies {
        implementation(project(":sound-service"))
        implementation(project(":sound-util"))
        implementation("org.controlsfx:controlsfx")
        runtimeOnly(files("${rootDir}/libs/jaad-0.8.4.jar"))
        runtimeOnly("com.googlecode.soundlibs:mp3spi")
    }
}

project(":sound-checker-boot") {
    apply(plugin = "org.springframework.boot")

    tasks.getByName<BootJar>("bootJar") {
        mainClass.set("jp.gr.java_conf.stardiopside.sound.SoundChecker")
    }

    dependencies {
        runtimeOnly(project(":sound-checker"))
    }
}
