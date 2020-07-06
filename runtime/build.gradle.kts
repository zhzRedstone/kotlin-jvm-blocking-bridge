import upload.Bintray
import java.util.*

plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("java")
    `maven-publish`
    id("com.jfrog.bintray")
}

version = Versions.project
description =
    "Kotlin compiler plugin that can generate a blocking bridge for calling suspend functions from Java with minimal effort"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType(JavaCompile::class.java) {
    options.encoding = "UTF8"
}

kotlin {
    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }
        getByName("test") {
            languageSettings.apply {
                languageVersion = "1.4"
            }
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))

    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.2.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.2.0")
}

tasks {
    "test"(Test::class) {
        useJUnitPlatform()
    }
}

// region PUBLISHING

tasks.register("ensureBintrayAvailable") {
    doLast {
        if (!Bintray.isBintrayAvailable(project)) {
            error("bintray isn't available. ")
        }
    }
}

if (Bintray.isBintrayAvailable(project)) {
    bintray {
        val keyProps = Properties()
        val keyFile = file("../keys.properties")
        if (keyFile.exists()) keyFile.inputStream().use { keyProps.load(it) }
        if (keyFile.exists()) keyFile.inputStream().use { keyProps.load(it) }

        user = Bintray.getUser(project)
        key = Bintray.getKey(project)
        setPublications("mavenJava")
        setConfigurations("archives")

        pkg.apply {
            userOrg = "mamoe"
            repo = "kotlin-jvm-blocking-bridge"
            name = "kotlin-jvm-blocking-bridge"
            setLicenses("Apache-2.0")
            publicDownloadNumbers = true
            vcsUrl = "https://github.com/mamoe/kotlin-jvm-blocking-bridge"
        }
    }

    @Suppress("DEPRECATION")
    val sourcesJar by tasks.registering(Jar::class) {
        classifier = "sources"
        from(sourceSets.main.get().allSource)
    }

    publishing {
        /*
        repositories {
            maven {
                // change to point to your repo, e.g. http://my.org/repo
                url = uri("$buildDir/repo")
            }
        }*/
        publications {
            register("mavenJava", MavenPublication::class) {
                from(components["java"])

                groupId = rootProject.group.toString()
                artifactId = "kotlin-jvm-blocking-bridge"
                version = version

                pom.withXml {
                    val root = asNode()
                    root.appendNode("description", description)
                    root.appendNode("name", project.name)
                    root.appendNode("url", "https://github.com/mamoe/kotlin-jvm-blocking-bridge")
                    root.children().last()
                }

                artifact(sourcesJar.get())
            }
        }
    }
} else println("bintray isn't available. NO PUBLICATIONS WILL BE SET")

// endregion