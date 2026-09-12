# PsicologiaUV 🧠

Aplicación Android para estudiantes de la carrera de **Psicología** de la **Universidad del Valle (UV)**, desarrollada de forma nativa con **Kotlin** y **Jetpack Compose**.

> ✨ **Proyecto independiente**: esta aplicación fue creada por una sola persona con mucho amor para democratizar el acceso a la información académica, brindando a cada estudiante una herramienta clara, accesible y transparente para gestionar y planificar su trayectoria universitaria.

---

## ✨ Características principales

- 📚 **Malla curricular y Pensum interactivo**: visualización ordenada por semestres, seguimiento de materias (aprobadas, pendientes, matriculadas), créditos académicos y verificación de prerrequisitos.
- 📈 **Seguimiento de avance**: cálculo en tiempo real del progreso de la carrera, créditos cursados vs. totales, gráficos de anillo y avance detallado por áreas curriculares.
- 💼 **Gestión de Prácticas Profesionales**: control del cumplimiento de requisitos previos (Ética, Diagnóstico, Fundamentaciones) y avance por niveles (I, II, III).
- 🎨 **Personalización por línea de profundización**: diseños visuales temáticos adaptados a cada línea profesional (**Clínica**, **Educativa**, **Organizacional**, **Social**, **Neuroclínica**), con soporte completo para modo claro y modo oscuro.
- 🎓 **Candidatura a grado**: monitoreo automatizado de los requisitos obligatorios para saber cuándo se está listo para el proceso de graduación.
- 📥 **Integración con SIRA y reportes PDF**: importación de calificaciones oficiales directamente desde el PDF del SIRA y exportación de informes académicos en PDF.
- ✊ **Memoria e historia estudiantil**: cronología interactiva de hitos históricos del movimiento estudiantil en Cali y Latinoamérica.
- 🔗 **Accesos rápidos**: consulta directa de normativas universitarias (Resolución 091, Acuerdo 009, malla oficial y enlaces institucionales).

---

## 🛠️ Stack tecnológico

| Tecnología | Rol en el proyecto |
|---|---|
| **Kotlin** | Lenguaje de desarrollo principal |
| **Jetpack Compose & Material 3** | Interfaz de usuario moderna y reactiva |
| **Kotlinx Serialization** | Persistencia y serialización de datos académicos |
| **PdfBox Android** | Procesamiento y generación de documentos PDF |
| **Android min SDK** | 24 (Android 7.0 Nougat) |
| **Target SDK** | 37 |

---

## 🚀 Cómo ejecutar el proyecto

### Requisitos previos

- [Android Studio](https://developer.android.com/studio) Hedgehog o superior
- JDK 17+
- Android SDK instalado

### Pasos

1. Clona el repositorio:
   ```bash
   git clone https://github.com/tu-usuario/PsicologiaUV.git
   ```

2. Abre el proyecto en Android Studio.

3. Sincroniza las dependencias con Gradle.

4. Ejecuta la aplicación en un dispositivo físico o emulador (Android 7.0+).

---

## 📁 Estructura del proyecto

```
app/
├── src/
│   └── main/
│       ├── java/com/renea/psicologiauv/
│       │   ├── data/       # Manejo de almacenamiento, persistencia y serialización
│       │   ├── model/      # Modelos de datos (Estudiante, Asignatura, Programa)
│       │   ├── gui/        # Pantallas y componentes en Jetpack Compose
│       │   └── ui/theme/   # Temas, paletas de colores y personalización por línea
│       ├── assets/
│       │   └── data/       # Datos curriculares y pensum base
│       └── res/            # Recursos visuales e íconos vectoriales
```

---

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Si tienes sugerencias de mejora o encuentras algún error en la información del pensum o las normativas, siéntete libre de abrir un *issue* o enviar un *pull request*.

---

## 📄 Licencia

Este proyecto está bajo la licencia **GNU General Public License v3.0 (GPL-3.0)** — consulta el archivo [LICENSE](LICENSE) para más detalles.

> Al distribuir o modificar este proyecto, cualquier trabajo derivado debe publicarse también bajo la licencia GPL-3.0 libre y abierta.
