import com.matthewprenger.cursegradle.*
import net.fabricmc.loom.task.RemapJarTask

plugins {
  kotlin("jvm") version Versions.kotlin
  id("fabric-loom") version Versions.loom
  id("com.modrinth.minotaur") version Versions.minotaur
  id("com.matthewprenger.cursegradle") version Versions.cursegradle
}

java {
  withSourcesJar()
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

base {
  archivesName.set(ModData.id)
}

group = ModData.group
version = ModData.version

repositories {
  mavenCentral()
  maven (url = "https://jitpack.io")
}

dependencies {
  minecraft("com.mojang:minecraft:${Versions.minecraft}")
  mappings("net.fabricmc:yarn:${Versions.yarn}:v2")
  modImplementation("net.fabricmc:fabric-loader:${Versions.fabricLoader}")
  modImplementation("net.fabricmc.fabric-api:fabric-api:${Versions.fabricApi}")
}

@Suppress("UnstableApiUsage")
tasks {
  withType<ProcessResources> {
    inputs.property("version", ModData.version)
    filesMatching("fabric.mod.json") {
      expand("version" to ModData.version)
    }
  }
  withType<JavaCompile> {
    configureEach {
      options.release.set(17)
    }
  }
}

// Publishing

val secretsFile = rootProject.file("publishing.properties")
val secrets = Secrets(secretsFile)
val versionName = "${ModData.id}-${Versions.minecraft[0]}-${ModData.version}"
val remapJar = tasks.getByName("remapJar") as RemapJarTask

if (secrets.isModrinthReady()) {
  println("Setting up Minotaur")
  modrinth {
    token.set(secrets.modrinthToken)
    projectId.set(secrets.modrinthId)
    uploadFile.set(remapJar)
    versionName.set(versionName)
    versionType.set(ModData.versionType)
    changelog.set(ModData.changelog)
    syncBodyFrom.set(rootProject.file("README.md").readText())
    gameVersions.set(ModData.mcVersions)
    loaders.set(listOf("fabric"))
    dependencies {
      ModData.dependencies.forEach(required::project)
    }
  }
}

if (secrets.isCurseforgeReady()) {
  println("Setting up Cursegradle")
  curseforge {
    apiKey = secrets.curseforgeToken
    project(closureOf<CurseProject> {
      id = secrets.curseforgeId
      releaseType = ModData.versionType
      ModData.mcVersions.forEach(::addGameVersion)
      changelog = ModData.changelog
      changelogType = "markdown"
      relations(closureOf<CurseRelation> {
        ModData.dependencies.forEach(::requiredDependency)
      })
      mainArtifact(remapJar, closureOf<CurseArtifact> {
        displayName = versionName
      })
    })
    options(closureOf<Options> {
      forgeGradleIntegration = false
    })
  }
  project.afterEvaluate {
    tasks.getByName<CurseUploadTask>("curseforge${secrets.curseforgeId}") {
      dependsOn(remapJar)
    }
  }
}