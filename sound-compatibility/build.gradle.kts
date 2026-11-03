plugins {
    id("java-common-conventions")
}

tasks.jar {
    manifest {
        attributes("Automatic-Module-Name" to "jp.gr.java_conf.stardiopside.sound.compatibility")
    }
}

dependencies {
    implementation("org.jspecify:jspecify")
    implementation("org:jaudiotagger")
}
