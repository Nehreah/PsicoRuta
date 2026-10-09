// Archivo de construcción raíz del proyecto (Top-level build file).
// Declara los plugins una sola vez para todos los módulos; cada módulo
// (por ejemplo :app) los aplica en su propio build.gradle.kts.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    kotlin("plugin.serialization") version "2.2.10" apply false
}
