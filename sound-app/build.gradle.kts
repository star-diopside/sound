plugins {
    application
    id("org.springframework.boot")
    id("org.openjfx.javafxplugin")
}

application {
    mainModule.set("jp.gr.java_conf.stardiopside.sound.app")
    mainClass.set("jp.gr.java_conf.stardiopside.sound.App")
}

springBoot {
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
    runtimeOnly("io.github.jseproject:jse-spi-flac")
    runtimeOnly("io.github.jseproject:jse-spi-opus")
    runtimeOnly("io.github.jseproject:jse-spi-vorbis")
    runtimeOnly("io.github.jseproject:jse-spi-speex")
    runtimeOnly("io.github.jseproject:jse-spi-mp3")
    runtimeOnly("io.github.jseproject:jse-spi-aac")
}
