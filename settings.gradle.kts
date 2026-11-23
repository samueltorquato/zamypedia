// Arquivo: settings.gradle.kts
// Diz ao Gradle onde buscar plugins e bibliotecas, e quais módulos fazem parte do projeto.
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Zamypedia"
include(":app")
