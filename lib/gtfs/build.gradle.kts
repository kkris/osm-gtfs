plugins {
    id("org.jetbrains.kotlin.jvm")
}

val onebusawayGtfsVersion: String by project

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation(project(":lib:common"))

    implementation("org.onebusaway:onebusaway-gtfs:$onebusawayGtfsVersion")
}