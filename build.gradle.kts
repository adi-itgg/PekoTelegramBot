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

    implementation("com.google.code.gson:gson:2.10")

    // modified in gradle cache
    implementation("eu.vendeli:telegram-bot:2.2.2")
    // modified but intellij idea have bug index external jar
    //implementation(fileTree("libs") { include("*.jar") })
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
        load(FileInputStream(rootProject.file("configuration\\mycommand.properties")))

        val sb = StringBuilder("\n")
        var currLength = 0
        forEach { (k, v) ->
            if (k !is String || v !is String) return@forEach
            if (sb.length > 2) {
                sb.append(", ")
                currLength += 2
            }
            if (currLength > 48) {
                sb.append("\n")
                currLength = 0
            }
            val value = "\"$k\" to \"$v\""
            sb.append(value)
            currLength += value.length
        }
        sb.append("\n")
        buildConfigField("me.phantomx.pekonime.bot.data.Commands", "COMMANDS", "Commands($sb)")
    }

    loadProperties("settings.properties")
    loadProperties("message.properties")
    loadProperties("buttons.properties")
}

fun BuildConfigExtension.loadProperties(filename: String) {
    val settings = Properties()
    settings.load(FileInputStream(rootProject.file("configuration\\$filename")))
    settings.forEach { (k, v) ->
        if (k !is String || v !is String) return@forEach

        val sb = StringBuilder()
        var currentLength = 0
        if (v.length > 50)
            v.forEach {
                if (currentLength > 50) {
                    sb.append("\" +\n\"")
                    currentLength = 0
                }

                currentLength += 1
                sb.append(it)
            }
        else sb.append(v)

        val isLong = v.toLongOrNull()?.run { true } ?: false

        buildConfigField(
            type = if (isLong) "kotlin.Long" else "String",
            name = k.replace(".", "_").toUpperCase(),
            value = if (isLong) "\n$sb" else "\n\"$sb\""
        )
    }
}

val generateBuildConfig by tasks

task("generateResourcesConstants") {
    val buildResources = buildConfig.forClass("BuildResources")

    doFirst {
        sourceSets["main"].resources.asFileTree.visit {
            if (isDirectory) return@visit
            val name = path.toUpperCase().replace("\\W".toRegex(), "_")

            buildResources.buildConfigField("me.phantomx.pekonime.bot.utils.FileChecker", name, "\nFileChecker(\"configuration/$path\")")
        }
    }

    generateBuildConfig.dependsOn(this)
}


tasks.create("buildJar", Jar::class) {
    group = "application" // OR, for example, "build"
    description = "Creates a self-contained fat JAR of the application that can be run."
    manifest.attributes["Main-Class"] = "me.phantomx.pekonime.bot.MainKt"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    val dependencies = configurations
        .runtimeClasspath
        .get()
        .map(::zipTree)
    from(dependencies)
    with(tasks.jar.get())
}