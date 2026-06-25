pluginManagement {
    repositories {
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/google-maven/") }
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/google-maven/") }
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
        google()
        mavenCentral()
    }
}

rootProject.name = "AiChatKMP"
include(":shared")
include(":androidApp")
include(":desktopApp")
// WasmJs 暂不支持 Room/DataStore，后续版本启用
// include(":webApp")
