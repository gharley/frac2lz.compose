import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.Properties

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "com.resmass"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }
    sourceSets {
        val material3Version = "1.3.0"
        val rxVersion = "3.1.3"
        val jsonVersion = "1.1.4"
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("io.reactivex.rxjava3:rxjava:$rxVersion")
                implementation("org.glassfish:javax.json:$jsonVersion")
                implementation("javax.json:javax.json-api:$jsonVersion")
                implementation("org.jetbrains.compose.material3:material3-desktop:$material3Version")
                implementation("commons-io:commons-io:2.11.0")
            }
            val jvmTest by getting
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            val major: String
            val minor: String
            val buildVersion: String

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

            modules("java.instrument", "jdk.unsupported")
            targetFormats(TargetFormat.Dmg, TargetFormat.Exe, TargetFormat.Msi, TargetFormat.Deb)
            windows {
                console = true
                msiPackageVersion = version
                exePackageVersion = version
                upgradeUuid = "e1b4694e-cab1-4208-8987-f2e361662b47"
                menuGroup = "Frac2lz"
            }
            packageName = "Frac2lz"
            packageVersion = version
        }
    }
}
