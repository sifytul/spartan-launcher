import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}
val hasReleaseKey = keystorePropertiesFile.exists()

val versionFile = rootProject.file("version.properties")

fun readVersionProps(): Properties = Properties().apply {
    if (versionFile.exists()) {
        versionFile.inputStream().use { load(it) }
    }
    if (!containsKey("versionMajor")) setProperty("versionMajor", "1")
    if (!containsKey("versionMinor")) setProperty("versionMinor", "0")
    if (!containsKey("versionPatch")) setProperty("versionPatch", "0")
}

fun bumpAndWrite(part: String) {
    val props = readVersionProps()
    var major = props.getProperty("versionMajor").toInt()
    var minor = props.getProperty("versionMinor").toInt()
    var patch = props.getProperty("versionPatch").toInt()
    when (part) {
        "major" -> {
            major++
            minor = 0
            patch = 0
        }
        "minor" -> {
            minor++
            patch = 0
        }
        else -> patch++
    }
    props.setProperty("versionMajor", major.toString())
    props.setProperty("versionMinor", minor.toString())
    props.setProperty("versionPatch", patch.toString())
    versionFile.outputStream().use { props.store(it, "Spartan Launcher version (semver). Bump: ./gradlew bumpVersion [-PversionPart=major|minor|patch]") }
}

val versionProps = readVersionProps()
val appVersionMajor = versionProps.getProperty("versionMajor").toInt()
val appVersionMinor = versionProps.getProperty("versionMinor").toInt()
val appVersionPatch = versionProps.getProperty("versionPatch").toInt()
val appVersionName = "$appVersionMajor.$appVersionMinor.$appVersionPatch"
// monotonic integer Play uses to detect updates (e.g. 1.2.3 -> 10203)
val appVersionCode = appVersionMajor * 10000 + appVersionMinor * 100 + appVersionPatch

android {
    namespace = "com.spartan.launcer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.spartan.launcer"
        minSdk = 26
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName
    }

    signingConfigs {
        if (hasReleaseKey) {
            create("release") {
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = if (hasReleaseKey) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.navigation.compose)
    implementation(libs.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.org.json)
    debugImplementation(libs.compose.ui.tooling)
}

tasks.register("printVersion") {
    group = "versioning"
    description = "Print the current version (versionName) and exit."
    doLast { println(appVersionName) }
}

tasks.register("printVersionCode") {
    group = "versioning"
    description = "Print the current versionCode."
    doLast { println(appVersionCode) }
}

tasks.register("bumpVersion") {
    group = "versioning"
    description = "Bump the version in version.properties (default: patch; override with -PversionPart=major|minor|patch)."
    doLast {
        val part = providers.gradleProperty("versionPart").getOrNull() ?: "patch"
        require(part in listOf("major", "minor", "patch")) { "versionPart must be major, minor, or patch" }
        bumpAndWrite(part)
        val props = readVersionProps()
        val bumped = "${props.getProperty("versionMajor")}.${props.getProperty("versionMinor")}.${props.getProperty("versionPatch")}"
        println("Bumped to $bumped")
    }
}