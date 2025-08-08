import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

var major: String
var minor: String
var buildVersion: String
var versionPropsFile = file("version.properties")

if (versionPropsFile.isFile) {
    val versionProps = Properties()
    val inStream = versionPropsFile.inputStream()

    versionProps.load(inStream)
    inStream.close()

    major = versionProps["major"].toString()
    minor = versionProps["minor"].toString()
    buildVersion = (versionProps["build"].toString().toInt() + 1).toString()

    val outStream = versionPropsFile.outputStream()
    versionProps["build"] = buildVersion
    versionProps.store(outStream, null)
} else {
    throw GradleException("Could not read version.properties!")
}

val version = "$major.$minor.$buildVersion"

kotlin {
    jvm("desktop")

    val generateVersionInfo by tasks.registering(Copy::class) {
        from(project.projectDir.resolve("src/desktopMain/templates"))
        into(project.projectDir.resolve("src/desktopMain/kotlin"))
        filter { line ->
            line.replace("\$projectVersion", version)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.components.resources)
        }

        val rxVersion = "3.1.3"
        val jsonVersion = "1.1.4"
        val collectionVersion = "1.4.5"
        val desktopMain by getting {
            kotlin.srcDir(generateVersionInfo)
            dependencies {
                implementation(compose.material3)
                implementation(compose.desktop.windows_x64)
                implementation(compose.runtime)
                implementation(compose.foundation)
// Explicitly include this is required to fix Proguard warnings coming from Kotlinx.DateTime
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.7.0")
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(libs.kotlinx.coroutines.swing)
                implementation("io.reactivex.rxjava3:rxjava:$rxVersion")
                implementation("org.glassfish:javax.json:$jsonVersion")
                implementation("javax.json:javax.json-api:$jsonVersion")
                implementation("commons-io:commons-io:2.11.0")
                implementation("androidx.collection:collection:$collectionVersion")
            }
            val desktopTest by getting
        }
    }
}


compose.desktop {
    application {
        buildTypes.release.proguard {
            version.set("7.5.0")
            configurationFiles.from("proguard.pro")
        }
        mainClass = "MainKt"

        nativeDistributions {
            modules("java.instrument" , "jdk.unsupported", "java.lang")
//            includeAllModules = true
            targetFormats(TargetFormat.Msi)
            windows {
                console = true
                msiPackageVersion = version
//                exePackageVersion = version
                upgradeUuid = "e1b4694e-cab1-4208-8987-f2e361662b47"
                menuGroup = "Frac2lz"
                includeAllModules = true
            }
            packageName = "Frac2lz"
            packageVersion = version
        }
    }
}
