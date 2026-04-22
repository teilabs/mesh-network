val androidClientProjectPath = ":client-android"

tasks.register("processDebugResources") {
    group = "build"
    description = "Delegates resource processing to the Android client module."
    dependsOn("$androidClientProjectPath:mergeDebugResources")
}
