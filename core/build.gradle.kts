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

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
    
    testCompileOnly("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")
    
    // Bouncy Castle Crypto API
    implementation("org.bouncycastle:bcprov-jdk18on:1.77")
}

configurations.all {
    resolutionStrategy {
        force("org.projectlombok:lombok:1.18.30")
    }
}