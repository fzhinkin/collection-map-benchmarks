group = "com.jetbrains"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.9.0-dev-5083"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.7"
}

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/") }
}

kotlin {
    jvmToolchain(8)

    sourceSets {
        main {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.7")
            }
        }
    }
}

benchmark {
    targets {
        register("main")
    }
}
