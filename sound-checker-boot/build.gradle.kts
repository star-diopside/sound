plugins {
    id("org.springframework.boot")
}

springBoot {
    mainClass.set("jp.gr.java_conf.stardiopside.sound.SoundChecker")
}

dependencies {
    runtimeOnly(project(":sound-checker"))
}
