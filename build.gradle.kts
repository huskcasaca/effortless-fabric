import io.github.themrmilchmann.gradle.publish.curseforge.*
import java.util.Properties
import java.io.*

plugins {
    id("fabric-loom") version "0.13.20"
    id("io.github.themrmilchmann.curseforge-publish") version "0.1.0"
}
version = "1.2.1"
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
    modImplementation(libs.cloth.config) { isTransitive = false }
    implementation("com.google.code.findbugs:jsr305:3.0.2")

//    minecraft("com.mojang:minecraft:$minecraftVersion")
//    mappings(loom.officialMojangMappings())
//
//    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
//    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")
//    modApi("com.terraformersmc:modmenu:$modmenuVersion") { isTransitive = false }
//    modApi("me.shedaniel.cloth:cloth-config-fabric:$clothConfigVersion") { isTransitive = false }
//
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(JavaVersion.VERSION_17.toString()))
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
}

loom {
    accessWidenerPath.value {
        file("src/main/resources/effortless.accesswidener")
    }
}

val curseForgeId: String by project

publishing {
    repositories {
        curseForge {
            apiKey.set(getLocalProperty("curseforge.apikey"))
        }
    }
    publications.create<CurseForgePublication>("curseForge") {
        projectID.set(getLocalProperty("curseforge.id").toInt()) // The CurseForge project ID (required)
        // Specify which game and version the mod/plugin targets (required)
        includeGameVersions { type, version -> type == "modloader" || version == "fabric" }
        includeGameVersions { type, version -> type == "minecraft-1-19" || version == "minecraft-1-19-2" || version == "minecraft-1-19-1" }

        artifact {
            changelog = Changelog("Changelog...", ChangelogType.TEXT) // The changelog (required)
            releaseType = ReleaseType.RELEASE // The release type (required)
            displayName = "effortless-fabric-$version-${libs.versions.minecraft.version}.jar"
        }
    }
}

tasks {

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
        if (JavaVersion.current().isJava9Compatible) {
            options.release.set(JavaVersion.VERSION_17.toString().toInt())
        }
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from("src/main/resources")
        filesMatching("fabric.mod.json") {
            expand(project.properties)
        }
    }

}

fun getLocalProperty(key: String): String {
    val properties = Properties()
    val localProperties = File("local.properties")
    if (localProperties.isFile) {
        InputStreamReader(FileInputStream(localProperties), Charsets.UTF_8).use { reader ->
            properties.load(reader)
        }
    } else error(localProperties.name + " is not found")

    return properties.getProperty(key)
}
