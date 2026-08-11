plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

configurations.all {
    // Evita o conflito clássico do CameraX: várias libs (AdMob, Room, etc.) trazem
    // o stub "com.google.guava:listenablefuture" enquanto o CameraX espera a classe
    // real do Guava. Forçamos uma única implementação de ListenableFuture no classpath.
    exclude(group = "com.google.guava", module = "listenablefuture")
}

// Se as variáveis de ambiente da keystore estiverem presentes (ex: no GitHub Actions),
// o release já sai assinado automaticamente. Localmente, sem essas variáveis, o release
// fica sem assinatura no Gradle - use o assistente "Generate Signed Bundle" do Android
// Studio nesse caso, que assina por fora do Gradle e não é afetado por isto aqui.
val releaseKeystorePath = System.getenv("ANDROID_KEYSTORE_PATH")

android {
    namespace = "com.qrscangera.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.qrscangera.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 8
        versionName = "1.9.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (releaseKeystorePath != null) {
            create("release") {
                storeFile = file(releaseKeystorePath)
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("ANDROID_KEY_ALIAS")
                keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
            }
        }
    }

    // Separação segura entre "estou testando" e "isto é produção", garantida em tempo de
    // compilação (não é uma variável que dá pra mudar depois, é literalmente um build
    // diferente). A variante "closedTesting" SÓ deve ser enviada à faixa de teste fechado
    // do Play Console; "production" é a que vai pra faixa de Produção.
    flavorDimensions += "environment"
    productFlavors {
        create("production") {
            dimension = "environment"
            buildConfigField("boolean", "IS_CLOSED_TESTING", "false")
        }
        create("closedTesting") {
            dimension = "environment"
            buildConfigField("boolean", "IS_CLOSED_TESTING", "true")
            versionNameSuffix = "-teste-fechado"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (releaseKeystorePath != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            isMinifyEnabled = false
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
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core / Compose
    implementation("androidx.core:core-ktx:1.13.1")
    // Padrão Arka Cortex: temas base sempre em Theme.AppCompat.DayNight.NoActionBar
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Fontes do Google Fonts (Poppins) carregadas sob demanda
    implementation("androidx.compose.ui:ui-text-google-fonts")

    // Guava real (não o stub listenablefuture) - necessário pelo CameraX (ProcessCameraProvider
    // retorna ListenableFuture) quando outras libs do projeto também dependem de guava/listenablefuture
    implementation("com.google.guava:guava:32.1.3-android")

    // Câmera
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")

    // Leitura de QR/Barcode - ML Kit
    implementation("com.google.mlkit:barcode-scanning:17.3.0")

    // Geração de QR Code - ZXing (core apenas, renderização é manual)
    implementation("com.google.zxing:core:3.5.3")

    // Persistência - Room (histórico)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // AdMob
    implementation("com.google.android.gms:play-services-ads:23.2.0")

    // Google Play Billing - compra única "Pro" (desbloqueia tudo + remove anúncios)
    implementation("com.android.billingclient:billing-ktx:6.2.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Acompanhamento de permissões simplificado
    implementation("androidx.activity:activity-ktx:1.9.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
