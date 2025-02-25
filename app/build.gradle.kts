plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "cat.dam.andy.firebase_compose"
    compileSdk = 35

    defaultConfig {
        applicationId = "cat.dam.andy.firebase_compose"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    //per executar tasques en segon pla com ara detectar errades connexió
    implementation(libs.androidx.work.runtime)
    // per ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    // Navegació amb Compose
    implementation(libs.androidx.navigation.compose)

    // Dependències per autentificar,
    implementation(libs.credentials) // Credential Manager
    implementation(libs.credentials.play.services.auth) // Suport per a Google Sign-In
    implementation(libs.googleid) // Google Identity
    implementation(libs.firebase.auth.ktx) // Firebase Authentication
    implementation(libs.play.services.auth) // per versions anteriors SDK<34


    //Firestore i Storage
    implementation(libs.firebase.firestore.ktx) // Firestore
    implementation(libs.firebase.storage.ktx) // Firebase Storage

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}