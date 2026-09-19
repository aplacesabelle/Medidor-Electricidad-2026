# Medidor Eléctrico

Aplicación Android que calcula el consumo y costo aproximado de electricidad a partir de las lecturas del medidor del hogar.

## Descripción del proyecto

La app permite al usuario ingresar la lectura anterior y la lectura actual de su medidor eléctrico, calcular el consumo en kWh y obtener una estimación del costo en pesos chilenos (CLP), incluyendo el desglose de cargo fijo, energía consumida e impuestos.

Nace como respuesta a un problema real: en Chile, los usuarios generalmente solo conocen su consumo eléctrico al recibir la boleta mensual, lo que dificulta planificar el gasto y tomar decisiones informadas sobre el uso de energía en el hogar.

## Estado actual del proyecto

En el módulo 1 solo tenía la idea planteada como propuesta. Para el módulo 2 avancé bastante más de lo pedido porque me interesó el proyecto y quise dejarlo funcionando de verdad, no solo en papel. En el módulo 3 seguí avanzando, agregando las pantallas que había planteado en el wireframe de la semana anterior.

Lo que hice hasta ahora:

- Instalé y configuré Android Studio, incluyendo el emulador (tuve varios problemas para que cargara bien, pero ya quedó funcionando con un Pixel 7).
- Armé la pantalla principal con los campos para ingresar la lectura anterior y la lectura actual del medidor, y agregué un campo para el nombre del medidor (para diferenciar, por ejemplo, entre "Casa" y "Oficina").
- Programé el cálculo del consumo en kWh y del costo aproximado, mostrando el desglose de cargo fijo, energía consumida e impuestos.
- Le di formato a la pantalla y elegí un color verde para la parte superior, pensando en la temática de energía.
- Agregué una pantalla de Ajustes, donde se puede configurar el cargo fijo y el precio por kWh, en vez de tenerlos fijos en el código.
- Agregué una pantalla de Historial, que muestra los cálculos que se han hecho anteriormente (medidor, fecha, consumo y costo).
- Armé una barra de navegación inferior con íconos para moverse entre las tres pantallas (Inicio, Historial, Ajustes).
- Hice que los datos de tarifas y el historial se guarden en el teléfono usando `SharedPreferences`, para que no se pierdan al cerrar la app.
- Esta semana agregué varias mejoras pensando en que la app se use de verdad, no solo una vez para probar:
  - Al abrir la app, los campos de lectura y nombre del medidor ahora parten vacíos (antes tenían números de ejemplo fijos).
  - Se puede elegir un medidor que ya se usó antes desde una lista, y la app rellena sola la "Lectura Anterior" con la última lectura que se guardó para ese medidor, para no tener que comparar siempre contra el mismo número.
  - Si la lectura actual es menor que la anterior, la app avisa con un mensaje y no deja calcular.
  - Si no se ingresa el nombre del medidor, tampoco deja calcular.
  - Al guardar un cálculo, aparece un mensaje breve confirmando que se guardó.
  - Los campos de lectura ahora solo aceptan números, filtrando letras y otros caracteres.

## Herramientas utilizadas

# Medidor Eléctrico

Aplicación Android que calcula el consumo y costo aproximado de electricidad a partir de las lecturas del medidor del hogar.

## Descripción del proyecto

La app permite al usuario ingresar la lectura anterior y la lectura actual de su medidor eléctrico, calcular el consumo en kWh y obtener una estimación del costo en pesos chilenos (CLP), incluyendo el desglose de cargo fijo, energía consumida e impuestos.

Nace como respuesta a un problema real: en Chile, los usuarios generalmente solo conocen su consumo eléctrico al recibir la boleta mensual, lo que dificulta planificar el gasto y tomar decisiones informadas sobre el uso de energía en el hogar.

## Estado actual del proyecto

En el módulo 1 solo tenía la idea planteada como propuesta. Para el módulo 2 avancé bastante más de lo pedido porque me interesó el proyecto y quise dejarlo funcionando de verdad, no solo en papel. En el módulo 3 seguí avanzando, agregando las pantallas que había planteado en el wireframe de la semana anterior, y esta semana le agregué mejoras pensando en un uso más real de la app.

Lo que hice hasta ahora:

- Instalé y configuré Android Studio, incluyendo el emulador (tuve varios problemas para que cargara bien, pero ya quedó funcionando con un Pixel 7).
- Armé la pantalla principal con los campos para ingresar la lectura anterior y la lectura actual del medidor, y agregué un campo para el nombre del medidor (para diferenciar, por ejemplo, entre "Casa" y "Oficina").
- Programé el cálculo del consumo en kWh y del costo aproximado, mostrando el desglose de cargo fijo, energía consumida e impuestos.
- Le di formato a la pantalla y elegí un color verde para la parte superior, pensando en la temática de energía.
- Agregué una pantalla de **Ajustes**, donde se puede configurar el cargo fijo y el precio por kWh, en vez de tenerlos fijos en el código.
- Agregué una pantalla de **Historial**, que muestra los cálculos que se han hecho anteriormente (medidor, fecha, consumo y costo).
- Armé una barra de navegación inferior con íconos para moverse entre las tres pantallas (Inicio, Historial, Ajustes).
- Hice que los datos de tarifas y el historial se guarden en el teléfono usando `SharedPreferences`, para que no se pierdan al cerrar la app.
- Esta semana agregué varias mejoras pensando en que la app se use de verdad, no solo una vez para probar:
  - Al abrir la app, los campos de lectura y nombre del medidor ahora parten vacíos (antes tenían números de ejemplo fijos).
  - Se puede elegir un medidor que ya se usó antes desde una lista, y la app rellena sola la "Lectura Anterior" con la última lectura que se guardó para ese medidor, para no tener que comparar siempre contra el mismo número.
  - Si la lectura actual es menor que la anterior, la app avisa con un mensaje y no deja calcular.
  - Si no se ingresa el nombre del medidor, tampoco deja calcular.
  - Al guardar un cálculo, aparece un mensaje breve confirmando que se guardó.
  - Los campos de lectura ahora solo aceptan números, filtrando letras y otros caracteres.

Todavía me falta ordenar mejor el código a medida que crece, y más adelante quiero evaluar pasar el historial a una base de datos local (Room) si sigo agregando funciones.

## Herramientas utilizadas

Trabajé todo el proyecto en Android Studio, usando Kotlin y Jetpack Compose para la interfaz. Para las pruebas usé el emulador de Android configurado con un Pixel 7.

Un desafío que tuve que resolver la primera semana fue que mi computador (en ese momento con 8 GB de RAM) no siempre daba abasto para correr Android Studio y el emulador al mismo tiempo, lo que hacía que el emulador se colgara o tardara mucho en cargar. Tuve que investigar cómo liberar memoria y reiniciar procesos que quedaban corriendo en segundo plano para poder seguir probando la app con normalidad. Ya actualicé mi equipo a 16 GB de RAM, así que ese problema ya no lo he tenido.

En el módulo 3 el desafío fue distinto: al agregar los íconos de la barra de navegación tuve un error de compilación porque me faltaba agregar una dependencia (`material-icons-extended`) en el `build.gradle.kts`, y aprendí a diferenciar entre el archivo de configuración del proyecto completo y el del módulo `app`, que son fáciles de confundir.

Esta semana el desafío principal fue pensar cómo simular el concepto de "cerrar el mes" sin tener que armar algo muy complejo: terminé usando el mismo historial que ya tenía guardado para buscar la última lectura de cada medidor, en vez de crear una tabla o lógica aparte solo para eso.

## Próxima semana

Para la próxima semana quiero seguir aplicando lo que veamos en el curso para ordenar mejor el código, y evaluar si conviene mover el guardado de datos a una base de datos local (Room) en vez de `SharedPreferences`, sobre todo porque el historial cada vez guarda más información por cada registro.

## Cómo ejecutar el proyecto
1. Clonar este repositorio.
2. Abrir el proyecto en Android Studio.
3. Sincronizar Gradle.
4. Ejecutar en un emulador o dispositivo físico con el botón ▶️ Run.

## Referencias
- Ministerio de Energía de Chile. (2023). *Estudio revela que el 73% del consumo energético de los hogares se destina a calefacción, climatización y agua caliente.* https://energia.gob.cl/noticias/nacional/estudio-revela-que-el-73-del-consumo-energetico-de-los-hogares-se-destina-calefaccionclimatizacion-y-agua-caliente
- Android Developers. (2019). *Enfoque de prioridad de Kotlin en Android.* Google. https://developer.android.com/kotlin/first?hl=es-419
## Referencias
- Ministerio de Energía de Chile. (2023). *Estudio revela que el 73% del consumo energético de los hogares se destina a calefacción, climatización y agua caliente.* https://energia.gob.cl/noticias/nacional/estudio-revela-que-el-73-del-consumo-energetico-de-los-hogares-se-destina-calefaccionclimatizacion-y-agua-caliente
- Android Developers. (2019). *Enfoque de prioridad de Kotlin en Android.* Google. https://developer.android.com/kotlin/first?hl=es-419
