plugins {
    java
}

group = "io.github.teilabs"
base.archivesName = "meshnet-core"

repositories {
    mavenCentral()
}

dependencies {
    // JUnit Jupiter
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
    
    // Bouncy Castle Crypto API
    implementation("org.bouncycastle:bcprov-jdk18on:1.77")
}

configurations.all {
    resolutionStrategy {
    }
}