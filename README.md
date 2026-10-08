# Medidor Eléctrico

Aplicación Android que calcula el consumo y costo aproximado de electricidad a partir de las lecturas del medidor del hogar.

## Descripción del proyecto

La app permite al usuario ingresar la lectura anterior y la lectura actual de su medidor eléctrico, calcular el consumo en kWh y obtener una estimación del costo en pesos chilenos (CLP), incluyendo el desglose de cargo fijo, energía consumida e impuestos.

Nace como respuesta a un problema real: en Chile, los usuarios generalmente solo conocen su consumo eléctrico al recibir la boleta mensual, lo que dificulta planificar el gasto y tomar decisiones informadas sobre el uso de energía en el hogar.

## Registro de cambios (Changelog)

### Módulo 1
- Propuesta inicial del proyecto: descripción de la idea, sin código todavía.

### Módulo 2
- Instalación y configuración de Android Studio y el emulador (Pixel 7).
- Pantalla principal con lectura anterior, lectura actual, y cálculo de consumo y costo aproximado.

### Módulo 3
- Wireframes de las 3 pantallas (Inicio, Ajustes, Historial), dibujados a mano y digitalizados en Figma.
- Implementación de las pantallas de Ajustes e Historial.
- Navegación entre pantallas con barra inferior e íconos.
- Persistencia de datos (tarifas e historial) usando `SharedPreferences`, para que no se pierdan al cerrar la app.
- Campo para el nombre del medidor.

### Módulo 4
- Los campos de lectura y nombre del medidor ahora parten vacíos al abrir la app, en vez de tener datos de ejemplo.
- Se puede elegir un medidor ya usado desde una lista; la app autocompleta la "Lectura Anterior" con la última lectura guardada de ese medidor.
- Validación: no se puede calcular sin ingresar el nombre del medidor.
- Validación: la lectura actual no puede ser menor que la anterior, con un mensaje breve en pantalla si pasa.
- Mensaje de confirmación al guardar un cálculo exitosamente.
- Los campos de lectura ahora solo aceptan números (se filtran letras y otros caracteres).
- Se agrega esta sección de changelog al README, para llevar un registro claro de los avances de cada módulo.

### Módulo 5 (actual)
- Migré el guardado de datos de `SharedPreferences` a una base de datos real. Intenté primero con **Room**, pero tuve varios problemas de compatibilidad con el plugin `kapt` en mi versión de Android Studio y no logré dejarlo funcionando.
- Terminé implementando **SQLite directo** con `SQLiteOpenHelper` (viene incluido en Android, sin necesitar plugins adicionales), creando las tablas `ajustes` e `historial` con SQL y usando `ContentValues` y `Cursor` para guardar y leer los datos.
- Con esto, tanto los ajustes de tarifa como el historial de cálculos ahora se guardan en una base de datos SQL local en vez de `SharedPreferences`.

### Próximos pasos (planeado)
- Evaluar si más adelante conviene volver a intentar Room (por ejemplo si cambio de versión de Android Studio), aunque por ahora SQLite directo cumple bien la función.
- Seguir ordenando el código en archivos separados si el proyecto sigue creciendo.

## Herramientas utilizadas

Trabajé todo el proyecto en Android Studio, usando Kotlin y Jetpack Compose para la interfaz. Para las pruebas usé el emulador de Android configurado con un Pixel 7.

Un desafío que tuve que resolver la primera semana fue que mi computador (en ese momento con 8 GB de RAM) no siempre daba abasto para correr Android Studio y el emulador al mismo tiempo, lo que hacía que el emulador se colgara o tardara mucho en cargar. Tuve que investigar cómo liberar memoria y reiniciar procesos que quedaban corriendo en segundo plano para poder seguir probando la app con normalidad. Ya actualicé mi equipo a 16 GB de RAM, así que ese problema ya no lo he tenido.

En el módulo 3 el desafío fue distinto: al agregar los íconos de la barra de navegación tuve un error de compilación porque me faltaba agregar una dependencia (`material-icons-extended`) en el `build.gradle.kts`, y aprendí a diferenciar entre el archivo de configuración del proyecto completo y el del módulo `app`, que son fáciles de confundir.

En el módulo 4 el desafío principal fue pensar cómo simular el concepto de "cerrar el mes" sin tener que armar algo muy complejo: terminé usando el mismo historial que ya tenía guardado para buscar la última lectura de cada medidor, en vez de crear una tabla o lógica aparte solo para eso.

En el módulo 5 el desafío fue justamente el guardado de datos: después de varios intentos fallidos configurando Room (problemas con el plugin `kapt` y la versión de Android Studio), opté por implementar SQLite directo con `SQLiteOpenHelper`, que viene incluido en Android y no requiere plugins adicionales. Aprendí a definir tablas con sentencias `CREATE TABLE`, y a usar `ContentValues` y `Cursor` para insertar y leer datos.

## Cómo ejecutar el proyecto
1. Clonar este repositorio.
2. Abrir el proyecto en Android Studio.
3. Sincronizar Gradle.
4. Ejecutar en un emulador o dispositivo físico con el botón ▶️ Run.

## Referencias
- Ministerio de Energía de Chile. (2023). *Estudio revela que el 73% del consumo energético de los hogares se destina a calefacción, climatización y agua caliente.* https://energia.gob.cl/noticias/nacional/estudio-revela-que-el-73-del-consumo-energetico-de-los-hogares-se-destina-calefaccionclimatizacion-y-agua-caliente
- Android Developers. (2019). *Enfoque de prioridad de Kotlin en Android.* Google. https://developer.android.com/kotlin/first?hl=es-419
