plugins {
    application
    id("org.springframework.boot")
}

application {
    mainModule = "jp.gr.java_conf.stardiopside.sound.console"
    mainClass = "jp.gr.java_conf.stardiopside.sound.Console"
}

springBoot {
    mainClass = "jp.gr.java_conf.stardiopside.sound.Console"
}

dependencies {
    implementation(project(":sound-service"))
    runtimeOnly(files("${rootDir}/libs/jaad-0.8.4.jar"))
    runtimeOnly("com.googlecode.soundlibs:mp3spi")
}
