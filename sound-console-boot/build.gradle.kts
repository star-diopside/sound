plugins {
    id("org.springframework.boot")
}

springBoot {
    mainClass.set("jp.gr.java_conf.stardiopside.sound.Console")
}

dependencies {
    runtimeOnly(project(":sound-console"))
}
