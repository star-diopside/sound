import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone

plugins {
    `java-library`
    checkstyle
    id("com.github.spotbugs")
    id("io.spring.dependency-management")
    id("net.ltgt.errorprone")
}

repositories {
    mavenCentral()
}

extra["javafxVersion"] = "27.0.1"

dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
    dependencies {
        dependency("org.controlsfx:controlsfx:11.2.6")
        dependency("org:jaudiotagger:2.0.3")
        dependency("com.googlecode.soundlibs:mp3spi:1.9.5.4")
        dependency("com.google.guava:guava:33.7.2-jre")
    }
}

dependencies {
    errorprone("com.google.errorprone:error_prone_core:2.52.0")
    errorprone("com.uber.nullaway:nullaway:0.14.3")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}

checkstyle {
    toolVersion = "14.3.0"
    isIgnoreFailures = true
}

spotbugs {
    toolVersion = "4.10.5"
    ignoreFailures = true
}

tasks.withType<JavaCompile>().configureEach {
    options.errorprone {
        check("NullAway", CheckSeverity.ERROR)
        option("NullAway:CustomContractAnnotations", "org.springframework.lang.Contract")
        option("NullAway:ExcludedFieldAnnotations", "javafx.fxml.FXML")
        option("NullAway:JSpecifyMode", "true")
        option("NullAway:OnlyNullMarked", "true")
    }
}

tasks.spotbugsMain {
    reports {
        create("html")
    }
}
