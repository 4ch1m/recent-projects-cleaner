import org.jetbrains.changelog.Changelog

fun properties(key: String) = project.findProperty(key).toString()

version = properties("pluginVersion")
description = properties("pluginDescription")

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij") version "1.17.1"
    id("org.jetbrains.changelog") version "2.2.0"
    id("com.github.ben-manes.versions") version "0.51.0"
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

intellij {
    version.set(properties("platformVersion"))
    pluginName.set(properties("pluginName"))
    updateSinceUntilBuild.set(false)
}

changelog {
    version.set(properties("pluginVersion"))
}

tasks {
    properties("javaVersion").let {
        withType<JavaCompile> {
            sourceCompatibility = it
            targetCompatibility = it
        }
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = properties("kotlinJvmTarget")
    }

    dependencyUpdates {
        rejectVersionIf {
            (
                listOf("RELEASE", "FINAL", "GA").any { candidate.version.uppercase().contains(it) }
                ||
                "^[0-9,.v-]+(-r)?$".toRegex().matches(candidate.version)
            ).not()
        }
    }

    patchPluginXml {
        pluginDescription.set(properties("pluginDescription"))
        version.set(properties("pluginVersion"))
        sinceBuild.set(properties("pluginSinceBuild"))
        changeNotes.set(provider {
            changelog.renderItem(
                changelog.getLatest(),
                Changelog.OutputType.HTML
            )
        })
    }

    signPlugin {
        if (listOf(
                "JB_PLUGIN_SIGN_CERTIFICATE_CHAIN",
                "JB_PLUGIN_SIGN_PRIVATE_KEY",
                "JB_PLUGIN_SIGN_PRIVATE_KEY_PASSWORD").all { System.getenv(it) != null }) {
            certificateChainFile.set(file(System.getenv("JB_PLUGIN_SIGN_CERTIFICATE_CHAIN")))
            privateKeyFile.set(file(System.getenv("JB_PLUGIN_SIGN_PRIVATE_KEY")))
            password.set(File(System.getenv("JB_PLUGIN_SIGN_PRIVATE_KEY_PASSWORD")).readText())
        }
    }

    publishPlugin {
        if (project.hasProperty("JB_PLUGIN_PUBLISH_TOKEN")) {
            token.set(project.property("JB_PLUGIN_PUBLISH_TOKEN").toString())
        }
    }
}
