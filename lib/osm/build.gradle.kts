plugins {
    id("org.jetbrains.kotlin.jvm")
}

val osm4jPbfVersion: String by project

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    api(project(":lib:common"))

    implementation("de.topobyte:osm4j-pbf:$osm4jPbfVersion")
}