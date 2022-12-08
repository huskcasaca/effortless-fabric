import io.github.huskcasaca.gradlecurseforgeplugin.*
import java.util.*

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.loom)
    id("io.github.huskcasaca.gradle-curseforge-plugin") version "1.0.0-alpha"
}

version = "1.4.0"
group = "dev.huskcasaca"

base {
    archivesName.set("effortless-fabric")
}

repositories {
    maven("https://maven.shedaniel.me/") { name = "shedaniel" }
    maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
}

dependencies {
    minecraft(libs.minecraft)

    mappings(loom.officialMojangMappings())

    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    modImplementation(libs.modmenu) { isTransitive = false }
    modImplementation(libs.cloth.config)

    implementation(libs.findbugs)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    withSourcesJar()
}

loom {
    accessWidenerPath.value {
        file("src/main/resources/effortless.accesswidener")
    }
}

tasks {
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from("src/main/resources")
        filesMatching("fabric.mod.json") {
            expand(project.properties)
        }
    }
    remapJar {
        archiveClassifier.set(libs.versions.minecraft.get())
    }
    remapSourcesJar {
        archiveClassifier.set(libs.versions.minecraft.get() + "-source")
    }
}

publishing {
    val properties = Properties().apply {
        file("local.properties").apply {
            if (isFile) {
                inputStream().use { reader -> load(reader) }
            } else {
                println("$name is not found")
                return@publishing
            }
        }
    }
    repositories {
        curseForge {
            token.set(properties.getProperty("curseforge.apikey"))
        }
    }
    publications {
        create<CurseForgePublication>("Effortless") {

            id.set(properties.getProperty("curseforge.id").toInt())

            artifact(tasks.remapJar) {
                releaseType = ReleaseType.ALPHA // The release type (required)
                changelog = Changelog("Changelog...", ChangelogType.TEXT) // The changelog (required)
                loader = LoaderType.FABRIC
                gameVersion = MinecraftVersion.VERSION_1_19_2
                javaVersion = JavaVersion.VERSION_17
            }
        }
    }
}