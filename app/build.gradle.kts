plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    kotlin("kapt")
    id("com.apollographql.apollo3")
    kotlin("plugin.serialization") version "1.9.0"
}

// val mondayApiToken: String = project.findProperty("MONDAY_API_TOKEN") as? String
//     ?: error("MONDAY_API_TOKEN is missing from local.properties")

android {
    namespace = "dev.bokov.mondaydotcom"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.bokov.mondaydotcom"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // buildConfigField("String", "MONDAY_API_TOKEN", "\"$mondayApiToken\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        compose = true
        buildConfig = true
    }
}

apollo {
    service("monday") {
        packageName.set("dev.bokov.mondaydotcom.graphql")
        schemaFile.set(file("src/main/graphql/dev/bokov/mondaydotcom/schema.graphqls"))
        // introspection {
        //     endpointUrl.set("https://api.monday.com/v2/get_schema")
        // }
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
    implementation(libs.androidx.material3.extended)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.animation)
    implementation(libs.androidx.foundation)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.coil.compose)

    implementation(libs.paging.runtime)
    implementation(libs.paging.compose)

    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    kapt(libs.hilt.compiler)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    kapt(libs.room.compiler)

    implementation(libs.apollo.runtime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.material1)

    testImplementation(kotlin("test"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.engine)
    testImplementation(libs.apollo.testing.support)
    testImplementation(libs.apollo.mockserver)
}
