import org.jetbrains.kotlin.ir.backend.js.compile

plugins {
    kotlin("jvm") version "2.0.0"

}

group = "org.dotnamekorea"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.aayushatharva.brotli4j:brotli4j:1.7.1")
    implementation("com.github.luben:zstd-jni:1.5.0-1")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.22")
    implementation("io.github.classgraph:classgraph:4.8.127")
    implementation("org.json:json:20240303")
    implementation("com.datastax.oss:java-driver-core:4.17.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

tasks.register("copyJar", Copy::class) {
    from(tasks.getByName("jar"))
    into(file("C:\\Users\\kcomw\\OneDrive\\바탕 화면\\개인\\Melon Framework Kotlin"))
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "MainKt"
        )
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.compileClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    archiveFileName.set("MelonFramework-v1.0.0-alpha.jar")
}

tasks.named("build") {
    dependsOn("copyJar")
}