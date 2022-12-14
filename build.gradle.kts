import java.util.function.BiConsumer
import java.util.UUID

buildscript {
    repositories {
        mavenCentral()
        google()
    }
}
plugins {
    id("com.android.application")
    checkstyle
    id("com.github.sherter.google-java-format") version "0.9"
    id("com.github.cs125-illinois.gradlegrader") version "2021.2.1"
}
android {
    compileSdkVersion(29)
    buildToolsVersion("29.0.3")
    lintOptions {
        disable("OldTargetApi", "GradleDependency")
    }
    defaultConfig {
        applicationId = "edu.illinois.cs.cs125.spring2021.courseable"
        minSdkVersion(24)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures {
        dataBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}
dependencies {
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("com.github.wrdlbrnft:sorted-list-adapter:0.3.0.27")
    implementation("androidx.recyclerview:recyclerview:1.1.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.squareup.okhttp3:mockwebserver:4.9.1")
    implementation("com.android.volley:volley:1.1.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.3.0")
    implementation("androidx.test.espresso:espresso-idling-resource:3.3.0")
    implementation("com.android.support:support-annotations:28.0.0")
    implementation("androidx.preference:preference:1.1.1")

    testImplementation("com.github.cs125-illinois:gradlegrader:2021.2.1")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.5.1")
    testImplementation("org.robolectric:shadows-httpclient:4.5.1")
    testImplementation("androidx.test:core:1.3.0")
    testImplementation("androidx.test.ext:junit:1.1.2")
    testImplementation("androidx.test.ext:truth:1.3.0")
    testImplementation("androidx.test.espresso:espresso-core:3.3.0")
    testImplementation("androidx.test.espresso:espresso-intents:3.3.0")
    testImplementation("androidx.test.espresso:espresso-contrib:3.3.0")
    testImplementation("com.puppycrawl.tools:checkstyle:8.41")
}
googleJavaFormat {
    toolVersion = "1.7"
}
checkstyle {
    configFile = rootProject.file("config/checkstyle/checkstyle.xml")
    toolVersion = "8.41"
}
tasks.register("checkstyle", Checkstyle::class) {
    source("src/main/java")
    include("**/*.java")
    classpath = files()
}
gradlegrader {
    assignment = "Spring2021.MP"
    checkpoint {
        yamlFile = rootProject.file("grade.yaml")
        configureTests(BiConsumer { MP, test ->
            require(MP in setOf("0", "1", "2", "3")) { "Cannot grade unknown checkpoint MP$MP" }
            test.setTestNameIncludePatterns(listOf("MP${MP}Test"))
            test.filter.isFailOnNoMatchingTests = false
        })
    }
    checkstyle {
        points = 10
        configFile = rootProject.file("config/checkstyle/checkstyle.xml")
        version = "8.41"
    }
    forceClean = false
    identification {
        txtFile = rootProject.file("ID.txt")
        validate = Spec {
            try {
                UUID.fromString(it.trim())
                true
            } catch (e: java.lang.IllegalArgumentException) {
                false
            }
        }
    }
    reporting {
        post {
            endpoint = "https://cs125-cloud.cs.illinois.edu/gradlegrader"
        }
        printPretty {
            title = "Grade Summary"
            notes = "On checkpoints with an early deadline, the maximum local score is 90/100. " +
                    "10 points will be provided during official grading if you submit code " +
                    "that earns at least 40 points before the early deadline."
        }
    }
    vcs {
        git = true
        requireCommit = true
    }
}
