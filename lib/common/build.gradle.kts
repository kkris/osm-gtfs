plugins {
    id("org.jetbrains.kotlin.jvm")
}

val googlePolylineCodecVersion: String by project
val kotestVersion: String by project
val kotlinLoggingVersion: String by project
val moshiVersion: String by project

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    api("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")

    implementation("io.leonard:google-polyline-codec:$googlePolylineCodecVersion")
    implementation("com.squareup.moshi:moshi-kotlin:$moshiVersion")

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