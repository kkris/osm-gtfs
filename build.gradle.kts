plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.71"
}

repositories {
    jcenter()
}

subprojects {
    repositories {
        jcenter()
        maven(url = "https://mvn.topobyte.de")
        maven(url = "http://mvn.slimjars.com")
    }
}

tasks.named("clean") {
    doLast {
        delete("generated")
    }
}