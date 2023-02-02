import org.jetbrains.compose.desktop.application.dsl.TargetFormat

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
        val material3_version = "1.2.2"
        val rxVersion = "3.1.3"
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("io.reactivex.rxjava3:rxjava:$rxVersion")
                implementation("org.glassfish:javax.json:1.1.4")
                implementation("javax.json:javax.json-api:1.1.4")
                implementation("org.jetbrains.compose.material3:material3-desktop:$material3_version")
            }
            val jvmTest by getting
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Frac2lz_compose"
            packageVersion = "1.0.0"
        }
    }
}
