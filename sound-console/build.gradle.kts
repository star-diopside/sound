plugins {
    application
    id("org.springframework.boot")
}

application {
    mainModule.set("jp.gr.java_conf.stardiopside.sound.console")
    mainClass.set("jp.gr.java_conf.stardiopside.sound.Console")
}

springBoot {
    mainClass.set("jp.gr.java_conf.stardiopside.sound.Console")
}

dependencies {
    implementation(project(":sound-service"))
    runtimeOnly("io.github.jseproject:jse-spi-flac")
    runtimeOnly("io.github.jseproject:jse-spi-opus")
    runtimeOnly("io.github.jseproject:jse-spi-vorbis")
    runtimeOnly("io.github.jseproject:jse-spi-speex")
    runtimeOnly("io.github.jseproject:jse-spi-mp3")
    runtimeOnly("io.github.jseproject:jse-spi-aac")
}
