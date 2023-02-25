plugins {
    java
}

repositories {
    maven(url = "https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }
    mavenCentral()
}

dependencies {
    implementation("com.velocitypowered:velocity-api:3.2.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.2.0-SNAPSHOT")
    implementation("net.kyori:adventure-text-minimessage:4.11.0")
    implementation("com.electronwill.night-config:toml:3.6.0")
    implementation("net.luckperms:api:5.4")
}

group = "com.matthewcash.network"
version = "1.0.0"
description = "Proxy Core"
java.sourceCompatibility = JavaVersion.VERSION_17
