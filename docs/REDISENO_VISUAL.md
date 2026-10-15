# Rediseño visual por línea profesional — PsicoRuta UV

## Cambios aplicados en esta fase

- Se añadió una capa de fondo temática global al contenedor principal de navegación.
- El fondo usa `obtenerTemaLinea(estudiante.linea)` y `IlustracionFondoLinea`, por lo que la ilustración y el color base cambian según la línea profesional del estudiante.
- La ilustración se dibuja detrás del paginador, no como parte de una tarjeta/hero.
- Se conservó el flujo de navegación y las llamadas a las pantallas existentes. No se modificó la lógica de datos, importación, cálculos, PDF ni almacenamiento.
- La selección de modo claro/oscuro usa el ajuste de tema del sistema.

## Pendiente de la siguiente pasada visual

- Revisar pantalla por pantalla y retirar las tarjetas que funcionen exclusivamente como hero/cabecera, conservando superficies cuando sean necesarias para formularios, diálogos, lectura de datos o accesibilidad.
- Unificar botones, iconos, estados activos, chips y navegación para usar el color de acento de la línea seleccionada sin reducir contraste ni estados de foco.
- Verificar especialmente el contraste de texto sobre las ilustraciones en modo claro y oscuro y dispositivos con tamaño de fuente ampliado.
- Ejecutar compilación y pruebas instrumentadas en un entorno con acceso a la distribución de Gradle/Android SDK. En este entorno la compilación no pudo iniciarse porque Gradle Wrapper no pudo descargar Gradle por falta de conexión DNS (`services.gradle.org`).

## Seguridad y mantenibilidad

El cambio es de presentación y no añade permisos, servicios, conexiones de red ni almacenamiento. Se mantiene la separación entre interfaz, modelo y controlador. No se debe eliminar el contraste de superficies en elementos que requieran distinción clara, como confirmaciones destructivas o información sensible.
