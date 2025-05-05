plugins {
    application
    id("org.springframework.boot")
    id("org.openjfx.javafxplugin")
}

application {
    mainModule = "jp.gr.java_conf.stardiopside.sound.checker"
    mainClass = "jp.gr.java_conf.stardiopside.sound.SoundChecker"
    applicationDefaultJvmArgs = listOf("--enable-native-access=javafx.graphics")
}

springBoot {
    mainClass = "jp.gr.java_conf.stardiopside.sound.SoundChecker"
}

tasks.bootStartScripts {
    defaultJvmOpts = listOf()
}

tasks.bootJar {
    manifest {
        attributes("Enable-Native-Access" to "ALL-UNNAMED")
    }
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
