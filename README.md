# PsicologiaUV 

Aplicación Android para estudiantes de la carrera de **Psicología** de la Universidad del Valle (UV), desarrollada con **Kotlin y Jetpack Compose**.

## Características

- 📚 Consulta de materias y malla curricular
- 👤 Perfil de estudiante con información académica
- 🏛️ Líneas de énfasis (Clínica, Educativa, Organizacional, Social, Neuroclínica)
- 📄 Visualización de documentos y archivos PDF
- 📖 Historias clínicas y casos de práctica
- 📧 Integración de contacto y correo institucional

## Stack tecnológico

| Tecnología | Versión |
|---|---|
| Kotlin | Latest |
| Jetpack Compose | BOM |
| Material 3 | ✅ |
| Room (base de datos) | ✅ |
| Kotlinx Serialization | 1.11.0 |
| PdfBox Android | ✅ |
| Android min SDK | 24 (Android 7.0) |
| Target SDK | 37 |

## Cómo ejecutar el proyecto

### Requisitos previos

- [Android Studio](https://developer.android.com/studio) Hedgehog o superior
- JDK 11+
- Android SDK 37

### Pasos

1. Clona el repositorio:
   ```bash
   git clone https://github.com/tu-usuario/PsicologiaUV.git
   ```

2. Abre el proyecto en Android Studio.

3. Sincroniza las dependencias de Gradle.

4. Ejecuta la app en un emulador o dispositivo físico (Android 7.0+).

## Estructura del proyecto

```
app/
├── src/
│   └── main/
│       ├── java/com/renea/psicologiauv/
│       │   ├── data/       # Fuentes de datos y repositorios
│       │   ├── model/      # Modelos de dominio
│       │   ├── gui/        # Componentes de UI (Compose)
│       │   └── ui/         # Pantallas y navegación
│       ├── assets/
│       │   └── data/       # Datos JSON y recursos empaquetados
│       └── res/            # Recursos de Android (drawables, mipmaps, etc.)
├── Asignatura/             # Recursos académicos adicionales
└── build.gradle.kts
```

## Contribuciones

Las contribuciones son bienvenidas. Por favor, abre un *issue* para discutir cambios importantes antes de hacer un *pull request*.

## 📄 Licencia

Este proyecto está bajo la licencia **GNU General Public License v3.0 (GPL-3.0)** — consulta el archivo [LICENSE](LICENSE) para más detalles.

> Al distribuir o modificar este proyecto, cualquier trabajo derivado debe publicarse también bajo GPL-3.0.
