plugins {
    application
}

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
