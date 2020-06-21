plugins {
    kotlin("jvm")
}

val jakartaXmlBindApiVersion: String by project
val jaxbApiVersion: String by project
val jaxbRuntimeVersion: String by project
val jollydayVersion: String by project
val kotestVersion: String by project

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    api(project(":lib:common"))
    api(project(":lib:gtfs"))

    implementation("de.jollyday:jollyday:$jollydayVersion")
    // JAX-B dependencies for JDK 9+
    implementation("javax.xml.bind:jaxb-api:$jaxbApiVersion")
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:$jakartaXmlBindApiVersion")
    implementation("org.glassfish.jaxb:jaxb-runtime:$jaxbRuntimeVersion")

    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-property-jvm:$kotestVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()
}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}