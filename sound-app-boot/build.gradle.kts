plugins {
    id("org.springframework.boot")
}

springBoot {
    mainClass.set("jp.gr.java_conf.stardiopside.sound.App")
}

dependencies {
    runtimeOnly(project(":sound-app"))
}
