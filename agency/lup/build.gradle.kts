plugins {
    id("org.jetbrains.kotlin.jvm")

    application
}

val commonsTextVersion: String by project
val cliktVersion: String by project
val logbackVersion: String by project
val kotlinLoggingVersion: String by project
val tabulaVersion: String by project

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation(project(":lib:agency"))
    implementation(project(":lib:common"))
    implementation(project(":lib:gtfs"))
    implementation(project(":lib:osm"))

    implementation("technology.tabula:tabula:$tabulaVersion")
    implementation("org.apache.commons:commons-text:$commonsTextVersion")
    implementation("com.github.ajalt:clikt:$cliktVersion")

    api("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
}

application {
    mainClassName = "org.kkris.osmgtfs.lup.MainKt"
}