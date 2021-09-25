plugins {
    application
    id("org.springframework.boot")
    id("org.openjfx.javafxplugin")
}

application {
    mainModule.set("jp.gr.java_conf.stardiopside.sound.checker")
    mainClass.set("jp.gr.java_conf.stardiopside.sound.SoundChecker")
}

springBoot {
    mainClass.set("jp.gr.java_conf.stardiopside.sound.SoundChecker")
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
