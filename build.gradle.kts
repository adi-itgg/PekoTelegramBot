import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties
import java.io.FileInputStream
import com.github.gmazzo.gradle.plugins.BuildConfigExtension

plugins {
    kotlin("jvm") version "1.7.20"
    application
    id("com.github.gmazzo.buildconfig") version "3.1.0"
}

group = "me.phantomx.pekonime.bot"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("io.insert-koin:koin-core:3.2.2")
    implementation("com.google.code.gson:gson:2.10")

    implementation("eu.vendeli:telegram-bot:2.2.2")
    // modified
    implementation(fileTree("dir" to "G:\\Github\\telegram-bot\\telegram-bot\\build\\libs", "include" to "*.jar"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}

buildConfig {
    buildConfigField("String", "TELEGRAM_ME", "\"https://t.me/\"")



    Properties().apply {
        load(FileInputStream(rootProject.file("mycommand.properties")))

        val sb = StringBuilder()
        forEach { (k, v) ->
            if (k !is String || v !is String) return@forEach
            if (sb.isNotEmpty()) sb.append(", ")
            sb.append("\"$k\" to \"$v\"")
        }
        buildConfigField("me.phantomx.pekonime.bot.data.Commands", "COMMANDS", "Commands($sb)")
    }

    loadProperties("settings.properties")
    loadProperties("message.properties")
    loadProperties("buttons.properties")
}

fun BuildConfigExtension.loadProperties(filename: String) {
    val settings = Properties()
    settings.load(FileInputStream(rootProject.file(filename)))
    settings.forEach { (k, v) ->
        if (k !is String || v !is String) return@forEach
        buildConfigField(
            type = "String",
            name = k.replace(".", "_").toUpperCase(),
            value = "\"${v.replace("\n", "\\n").replace(" ", "~nl~")}\""
        )
    }
}

val generateBuildConfig by tasks

task("generateResourcesConstants") {
    val buildResources = buildConfig.forClass("BuildResources")

    doFirst {
        sourceSets["main"].resources.asFileTree.visit(Action<FileVisitDetails> {
            val name = path.toUpperCase().replace("\\W".toRegex(), "_")

            buildResources.buildConfigField("java.io.File", name, "File(\"$path\")")
        })
    }

    generateBuildConfig.dependsOn(this)
}