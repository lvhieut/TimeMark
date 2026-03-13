import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import java.text.SimpleDateFormat
import java.util.Locale

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.timemarkbase"
    compileSdk = 35

    flavorDimensions += "mode"

    defaultConfig {
        applicationId = "com.example.timemarkbase"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "API_KEY",
            "\"AIzaSyDLkb82VOZNFs8QD5Kc1Bcnk5541Llnh60\""
        )
    }

    applicationVariants.all {
        val outputFileName = "TimeMark_Pro" +
                "_${name}" +
                "_ver${versionName}.apk"
        outputs.all {
            val output = this as? BaseVariantOutputImpl
            output?.outputFileName = outputFileName
        }
    }

    productFlavors {
        create("dev") {
            dimension = "mode"
            applicationId = "com.example.timemarkbase"
        }
        create("prod") {
            dimension = "mode"
            applicationId = "com.example.timemarkbase"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.location)
    implementation(libs.firebase.config.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // request permission
    implementation(libs.permissionx)
    implementation(libs.play.services.location)
    implementation(libs.glide)

    implementation (libs.androidx.camera.core)
    implementation (libs.androidx.camera.camera2)
    implementation (libs.androidx.camera.lifecycle)
    implementation (libs.androidx.camera.view)


    // ViewModel + LiveData
    implementation (libs.androidx.lifecycle.viewmodel.ktx)
    implementation (libs.androidx.lifecycle.livedata.ktx)

    // Nếu muốn Lifecycle + ViewModel cho Fragment / Activity
    implementation (libs.androidx.fragment.ktx)
    implementation (libs.androidx.activity.ktx)

    //firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config)

    implementation (libs.osmdroid.android)
    implementation (libs.osmdroid.wms)
    implementation (libs.osmdroid.mapsforge)
    implementation (libs.play.services.maps)
}