// composeApp/build.gradle.kts

import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import com.github.jk1.license.filter.DependencyFilter
import com.github.jk1.license.filter.ExcludeTransitiveDependenciesFilter
import com.github.jk1.license.render.JsonReportRenderer
import org.gradle.internal.extensions.stdlib.capitalized
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val APP_NAME = "Kreate"

plugins {
  // Multiplatform
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.jetbrains.compose)

  // Android
  alias(libs.plugins.android.application)
  alias(libs.plugins.room)

  alias(libs.plugins.kotlin.ksp)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.license.report)
}

repositories {
  google()
  mavenCentral()
  maven {
    url = uri("https://jitpack.io")
  }
}

kotlin {
  // Ensure Android target is registered for KMP
  androidTarget {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_21)
      freeCompilerArgs.add("-Xcontext-parameters")
    }
  }

  // JVM target for Desktop Compose
  jvm()

  sourceSets {
    all {
      languageSettings {
        optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
      }
    }

    // Shared UI/business logic
    val commonMain by getting {
      dependencies {
        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.material3)
        implementation(compose.ui)
        implementation(compose.components.resources)
        implementation(compose.components.uiToolingPreview)

        // Keep commonMain free of JVM-only modules; use platform source sets for provider modules
        implementation(projects.oldtube)
        implementation(projects.kugou)
        implementation(projects.lrclib)

        implementation(libs.kizzy.rpc)

        // Room KMP
        implementation(libs.room.runtime)
        implementation(libs.room.sqlite.bundled)

        implementation(libs.navigation.kmp)

        // coil3 MP
        implementation(libs.coil3.compose.core)
        implementation(libs.coil3.network.ktor)

        implementation(libs.translator)

        implementation(libs.bundles.compose.kmp)
        implementation(libs.hypnoticcanvas)
        implementation(libs.hypnoticcanvas.shaders)

        implementation(libs.kotlin.csv)
        implementation(libs.bundles.ktor)

        implementation(libs.math3)
      }
    }

    // Android-specific wiring (providers and Android libs)
    val androidMain by getting {
      dependencies {
        implementation(libs.media3.session)
        implementation(libs.kotlinx.coroutines.guava)
        implementation(libs.newpipe.extractor)
        implementation(libs.nanojson)
        implementation(libs.androidx.webkit)

        implementation(libs.androidx.glance.widgets)
        implementation(libs.androidx.constraintlayout)

        implementation(libs.androidx.appcompat)
        implementation(libs.androidx.appcompat.resources)
        implementation(libs.androidx.palette)

        implementation(libs.monetcompat)
        implementation(libs.androidmaterial)

        implementation(libs.ktor.okhttp)
        implementation(libs.okhttp3.logging.interceptor)

        // Deprecating
        implementation(libs.androidx.crypto)

        // Player implementations
        implementation(libs.media3.exoplayer)
        implementation(libs.androidyoutubeplayer)

        implementation(libs.timber)
        implementation(libs.toasty)

        // Provider modules (JVM on Android)
        implementation(projects.innertube)
        implementation(projects.saavn)
      }
    }

    // Desktop JVM wiring (providers and desktop compose)
    val jvmMain by getting {
      dependencies {
        implementation(compose.components.resources)
        implementation(compose.desktop.currentOs)

        implementation(libs.material.icon.desktop)
        implementation(libs.vlcj)

        // Provider modules for desktop runtime
        implementation(projects.innertube)
        implementation(projects.saavn)
      }
    }
  }
}

android {
  compileSdk = 36

  defaultConfig {
    applicationId = "me.knighthat.kreate"
    minSdk = 21
    targetSdk = 36
    versionCode = 117
    versionName = "1.7.2"

    // Universal variables
    buildConfigField("String", "APP_NAME", "\"$APP_NAME\"")
  }

  buildFeatures {
    buildConfig = true
    compose = true
  }

  dependenciesInfo {
    includeInApk = false
    includeInBundle = false
  }

  splits {
    abi {
      reset()
      isUniversalApk = true
    }
  }

  namespace = "app.kreate.android"

  buildTypes {
    debug {
      applicationIdSuffix = ".debug"
      manifestPlaceholders["appName"] = "$APP_NAME-debug"
    }
    // To test compatibility after minification process
    create("debugR8") {
      initWith(maybeCreate("debug"))
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(
        getDefaultProguardFile("proguard-defaults.txt"),
        "debug-proguard-rules.pro"
      )
    }
    release {
      isDefault = true
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
    create("uncompressed") {
      versionNameSuffix = "-f"
    }
    // Apply appName placeholder across all variants
    forEach {
      it.manifestPlaceholders.putIfAbsent("appName", APP_NAME)
    }
  }

  flavorDimensions += listOf("prod")
  productFlavors {
    create("github") {
      dimension = "prod"
      isDefault = true
    }
    create("fdroid") {
      dimension = "prod"
      versionNameSuffix = "-fdroid"
    }
    create("izzy") {
      dimension = "prod"
      versionNameSuffix = "-izzy"
    }
  }

  applicationVariants.all {
    outputs.map {
      it as BaseVariantOutputImpl
    }.forEach {
      val suffix = if (flavorName == "izzy") "izzy" else buildType.name
      it.outputFileName = "$APP_NAME-${suffix}.apk"
    }

    if (buildType.name != "debug") {
      val capitalizedFlavorName = "${flavorName.capitalized()}${buildType.name.capitalized()}"
      tasks.register<Copy>("copyReleaseNoteTo${capitalizedFlavorName}Res") {
        from("$rootDir/fastlane/metadata/android/en-US/changelogs")
        val fileName = "${android.defaultConfig.versionCode!!}.txt"
        setIncludes(listOf(fileName))
        into("$rootDir/composeApp/src/android$capitalizedFlavorName/res/raw")
        rename {
          if (it == fileName) "release_notes.txt" else it
        }
      }
      preBuildProvider.get().dependsOn("copyReleaseNoteTo${capitalizedFlavorName}Res")
    }
  }

  compileOptions {
    isCoreLibraryDesugaringEnabled = true
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
  }

  signingConfigs {
    create("release") {
      val path = System.getenv("ANDROID_KEYSTORE_PATH") ?: "keystore.jks"
      storeFile = rootProject.file(path)
      storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
      keyAlias = System.getenv("ANDROID_KEY_ALIAS")
      keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
      storeType = System.getenv("ANDROID_KEYSTORE_TYPE") ?: "PKCS12"
    }
  }

  buildTypes {
    // Attach signing config to release
    getByName("release") {
      signingConfig = signingConfigs.getByName("release")
    }
  }
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}

compose.desktop {
  application {
    mainClass = "MainKt"

    // conveyor
    version = "0.0.1"
    group = "rimusic"

    // jpackage
    nativeDistributions {
      vendor = "RiMusic.DesktopApp"
      description = "RiMusic Desktop Music Player"
      targetFormats(TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)
      packageName = "RiMusic.DesktopApp"
      packageVersion = "0.0.1"
    }
  }
}

// Updated Compose resources DSL: no ResourcesType symbol
compose.resources {
  publicResClass = true
  // Optional: force generation if needed by your workflow
  // generateResClass.set(org.jetbrains.compose.resources.GenerateResClass.Always)
}

room {
  schemaDirectory("$projectDir/schemas")
}

dependencies {
  add("kspAndroid", libs.room.compiler)
  coreLibraryDesugaring(libs.desugaring.nio)
}

// Use `./gradlew dependencies` to get report in composeApp/build/reports/dependency-license
licenseReport {
  projects = arrayOf(project)
  configurations = arrayOf("githubUncompressedRuntimeClasspath")
  excludeOwnGroup = true
  excludeBoms = true
  renderers = arrayOf(JsonReportRenderer())
  filters = arrayOf<DependencyFilter>(ExcludeTransitiveDependenciesFilter())
}
