plugins {
    idea
    java
    id("gg.essential.loom") version "1.10.+"
    id("com.gradleup.shadow") version "9.2.+"
}

val modID: String by project
val modName: String by project
val mavenGroup: String by project
val modVersion: String by project
val mcVersion: String by project
val occlusionCullingVersion: String by project

group = mavenGroup
version = modVersion
base.archivesName.set("EntityCulling-Forge-mc$mcVersion")

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

loom {
    runConfigs {
        getByName("client") {
            property("mixin.debug.verbose", "true")
            property("mixin.debug.export", "true")
            programArgs("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker", "--mixin", "$modID.mixins.json")
        }
        remove(getByName("server"))
    }

    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
        //accessTransformer(rootProject.file("src/main/resources/${modID}_at.cfg"))
        mixinConfig("$modID.mixins.json")
    }

    mixin {
        defaultRefmapName.set("$modID.mixins.refmap.json")
    }

    // For some reason loom defaults to tab indentation
    decompilers {
        named("vineflower") {
            options.put("indent-string", "    ")
        }
    }
}

repositories {
    mavenCentral()
    maven("https://repo.essential.gg/repository/maven-public")
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-public/")
}

val shade: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    shade("com.logisticscraft:occlusionculling:$occlusionCullingVersion")
    shade("org.spongepowered:mixin:0.7.11-SNAPSHOT") {
        isTransitive = false
    }
}

sourceSets.main {
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    processResources {
        inputs.property("modID", modID)
        inputs.property("modName", modName)
        inputs.property("version", version)
        inputs.property("mcVersion", mcVersion)

        filesMatching(listOf("mcmod.info", "$modID.mixins.json")) {
            expand(inputs.properties) {
                escapeBackslash = true
            }
        }

        //rename("(.+_at.cfg)", "META-INF/$1")
    }

    shadowJar {
        archiveClassifier.set("dev")
        configurations = listOf(shade)
        exclude("META-INF/maven/**")
    }

    jar {
        dependsOn(shadowJar)
        duplicatesStrategy = DuplicatesStrategy.FAIL

        manifest.attributes(mapOf(
            "ModSide" to "CLIENT",
            //"FMLAT" to "${modID}_at.cfg",
            "FMLCorePluginContainsFMLMod" to true,
            "ForceLoadAsMod" to true,
            "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
            "MixinConfigs" to "$modID.mixins.json",
        ))
    }

    remapJar {
        inputFile.set(shadowJar.get().archiveFile)
        archiveClassifier.set("")
    }
}
