plugins {
    `java-library`
}

group = "io.github.teilabs"
version = "0.1.0-SNAPSHOT"
base.archivesName = "meshnet-core"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation("org.bouncycastle:bcprov-jdk18on:1.77")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
configurations.all {
    resolutionStrategy {
    }
}