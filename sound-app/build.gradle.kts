plugins {
    application
    id("org.openjfx.javafxplugin")
}

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
