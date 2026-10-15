# PsicoRuta UV — normalización visual, fase 2

## Cambios aplicados

- El fondo temático global por línea profesional sigue en el contenedor del paginador, detrás de las pantallas.
- La pantalla de Perfil (`PantallaEditar`) ya no pinta una superficie opaca de fondo que tape la ilustración global.
- La pantalla de Pensum deja de pintar un fondo opaco propio.
- La superficie raíz de Avance ahora es transparente para que se vea el fondo temático compartido.
- Se aplanaron los paneles decorativos principales de cabecera/progreso en Prácticas, Pensum y Avance: sin elevación, sin borde y con superficie transparente.
- La pantalla de Grado ya no presenta las tarjetas principales como bloques elevados opacos.
- No se modificaron los modelos, el serializador, la importación, los cálculos de avance, ni las acciones de navegación.

## Limitaciones pendientes

- Hay tarjetas y superficies interiores que sí cumplen una función (formularios, selectores, diálogos, grupos de datos y elementos interactivos). No se eliminaron indiscriminadamente porque hacerlo podría reducir la legibilidad, afectar los controles o cambiar la interacción.
- Falta una pasada visual pantalla por pantalla para convertir el resto de tarjetas meramente decorativas en contenido sin caja y para unificar todos los botones/estados con la paleta de cada línea profesional.
- La compilación no pudo verificarse en este entorno: el Gradle Wrapper intentó descargar Gradle 9.5.0 y la conexión a `services.gradle.org` falló por `UnknownHostException`. Ejecutar `./gradlew :app:compileDebugKotlin` en un entorno con conectividad para confirmar compilación.

## Principio de implementación

Se priorizó la transparencia de los contenedores raíz para que el fondo temático compartido sea visible, y se aplanaron solo los paneles principales identificados como decorativos. Los contenedores que comunican estados, agrupan formularios o contienen acciones se deben revisar visualmente antes de eliminarlos.
