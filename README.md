# Medidor Eléctrico

Aplicación Android que calcula el consumo y costo aproximado de electricidad a partir de las lecturas del medidor del hogar.

## Descripción del proyecto

La app permite al usuario ingresar la lectura anterior y la lectura actual de su medidor eléctrico, calcular el consumo en kWh y obtener una estimación del costo en pesos chilenos (CLP), incluyendo el desglose de cargo fijo, energía consumida e impuestos.

Nace como respuesta a un problema real: en Chile, los usuarios generalmente solo conocen su consumo eléctrico al recibir la boleta mensual, lo que dificulta planificar el gasto y tomar decisiones informadas sobre el uso de energía en el hogar.

## Estado actual del proyecto

En el módulo 1 solo tenía la idea planteada como propuesta. Para el módulo 2 avancé bastante más de lo pedido porque me interesó el proyecto y quise dejarlo funcionando de verdad, no solo en papel. En el módulo 3 seguí avanzando, agregando las pantallas que había planteado en el wireframe de la semana anterior.

Lo que hice hasta ahora:

- Instalé y configuré Android Studio, incluyendo el emulador (tuve varios problemas para que cargara bien, pero ya quedó funcionando con un Pixel 7).
- Armé la pantalla principal con los campos para ingresar la lectura anterior y la lectura actual del medidor, y agregué un campo nuevo para el nombre del medidor (para poder diferenciar, por ejemplo, entre "Casa" y "Oficina").
- Programé el cálculo del consumo en kWh y del costo aproximado, mostrando el desglose de cargo fijo, energía consumida e impuestos.
- Le di formato a la pantalla y elegí un color verde para la parte superior, pensando en la temática de energía.
- Agregué una pantalla de **Ajustes**, donde se puede configurar el cargo fijo y el precio por kWh, en vez de tenerlos fijos en el código.
- Agregué una pantalla de **Historial**, que muestra los cálculos que se han hecho anteriormente (medidor, fecha, consumo y costo).
- Armé una barra de navegación inferior con íconos para moverse entre las tres pantallas (Inicio, Historial, Ajustes).
- Hice que los datos de tarifas y el historial se guarden en el teléfono usando `SharedPreferences`, para que no se pierdan al cerrar la app.

Todavía me falta ordenar mejor el código a medida que crece, y más adelante quiero evaluar pasar el historial a una base de datos local (Room) si sigo agregando funciones.


## Herramientas utilizadas

Trabajé todo el proyecto en Android Studio, usando Kotlin y Jetpack Compose para la interfaz. Para las pruebas usé el emulador de Android configurado con un Pixel 7.

Un desafío que tuve que resolver la primera semana fue que mi computador (en ese momento con 8 GB de RAM) no siempre daba abasto para correr Android Studio y el emulador al mismo tiempo, lo que hacía que el emulador se colgara o tardara mucho en cargar. Tuve que investigar cómo liberar memoria y reiniciar procesos que quedaban corriendo en segundo plano para poder seguir probando la app con normalidad. Ya actualicé mi equipo a 16 GB de RAM, así que ese problema ya no lo he tenido.

Esta semana el desafío fue distinto: al agregar los íconos de la barra de navegación tuve un error de compilación porque me faltaba agregar una dependencia (`material-icons-extended`) en el `build.gradle.kts`, y aprendí a diferenciar entre el archivo de configuración del proyecto completo y el del módulo `app`, que son fáciles de confundir.

## Próxima semana

Para la próxima semana quiero seguir mejorando la app: agregar una validación para que no se pueda guardar un cálculo si el usuario no ingresó el nombre del medidor (por ahora si lo deja vacío se guarda como "Sin nombre", pero prefiero que se lo pida antes de calcular). También quiero revisar si conviene mover el guardado de datos a una base de datos local (Room) en vez de `SharedPreferences`, y seguir aplicando lo que veamos en el curso para ordenar mejor el código.


## Cómo ejecutar el proyecto
1. Clonar este repositorio.
2. Abrir el proyecto en Android Studio.
3. Sincronizar Gradle.
4. Ejecutar en un emulador o dispositivo físico con el botón ▶️ Run.

## Referencias
- Ministerio de Energía de Chile. (2023). *Estudio revela que el 73% del consumo energético de los hogares se destina a calefacción, climatización y agua caliente.* https://energia.gob.cl/noticias/nacional/estudio-revela-que-el-73-del-consumo-energetico-de-los-hogares-se-destina-calefaccionclimatizacion-y-agua-caliente
- Android Developers. (2019). *Enfoque de prioridad de Kotlin en Android.* Google. https://developer.android.com/kotlin/first?hl=es-419
