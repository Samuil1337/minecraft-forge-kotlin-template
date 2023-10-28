import net.minecraftforge.gradle.userdev.UserDevExtension
import org.gradle.jvm.tasks.Jar

// gradle.properties
val modGroup = "io.github.proudust"
val modVersion = "1.12.2-1.0.0-beta"
val modBaseName = "minecraft-forge-kotlin-template"
val forgeVersion = "1.12.2-14.23.5.2860"
val customMappingChannel = "snapshot"
val customMappingVersion = "20171003-1.12"

buildscript {
    repositories {
        mavenCentral()
        maven(url = "https://maven.minecraftforge.net/")
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    }
    dependencies {
        classpath("net.minecraftforge.gradle:ForgeGradle:4.+") {
            isChanging = true
        }
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.10")
    }
}

plugins {
    java
    scala
}

apply {
    plugin("net.minecraftforge.gradle")
    plugin("kotlin")
}

version = modVersion
group = modGroup

configure<UserDevExtension> {
    // the mappings can be changed at any time, and must be in the following format.
    // snapshot_YYYYMMDD   snapshot are built nightly.
    // stable_#            stables are built at the discretion of the MCP team.
    // Use non-default mappings at your own risk. they may not always work.
    // simply re-run your setup task after changing the mappings to update your workspace.
    mappings(customMappingChannel, customMappingVersion)
    //mappings = mappingVersion
    // makeObfSourceJar = false // a Srg named sources jar is made by default. uncomment this to disable.

    accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

    // Default run configurations.
    // These can be tweaked, removed, or duplicated as needed.
    runs {
        create("client") {
            workingDirectory(project.file("run"))

            // Recommended logging data for a userdev environment
            property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")

            // Recommended logging level for the console
            property("forge.logging.console.level", "debug")
        }

        create("server") {
            workingDirectory(project.file("run"))

            // Recommended logging data for a userdev environment
            property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")

            // Recommended logging level for the console
            property("forge.logging.console.level", "debug")
        }
    }
}

repositories {
    jcenter()
    mavenCentral()
    maven(url = "https://maven.shadowfacts.net/")
}

dependencies {
    "minecraft"("net.minecraftforge:forge:$forgeVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.10")
    implementation("net.shadowfacts:Forgelin:1.8.4")
}

// processResources
val Project.minecraft: UserDevExtension
    get() = extensions.getByName<UserDevExtension>("minecraft")
tasks.withType<Jar> {
    // this will ensure that this task is redone when the versions change.
    inputs.property("version", modVersion)

    archiveBaseName.set(modBaseName)

    // replace stuff in mcmod.info, nothing else
    filesMatching("/mcmod.info") {
        expand(mapOf(
            "version" to modVersion,
            "mcversion" to "1.12.2"
        ))
    }
}

// workaround for userdev bug
tasks.create("copyResourceToClasses", Copy::class) {
    tasks.classes.get().dependsOn(this)
    dependsOn(tasks.processResources.get())
    onlyIf { gradle.taskGraph.hasTask(tasks.getByName("prepareRuns")) }
    into("$buildDir/classes/kotlin/main")
    from(tasks.processResources.get().destinationDir)
}
