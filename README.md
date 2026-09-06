# Medidor Eléctrico

Aplicación Android que calcula el consumo y costo aproximado de electricidad a partir de las lecturas del medidor del hogar.

## Descripción del proyecto

La app permite al usuario ingresar la lectura anterior y la lectura actual de su medidor eléctrico, calcular el consumo en kWh y obtener una estimación del costo en pesos chilenos (CLP), incluyendo el desglose de cargo fijo, energía consumida e impuestos.

Nace como respuesta a un problema real: en Chile, los usuarios generalmente solo conocen su consumo eléctrico al recibir la boleta mensual, lo que dificulta planificar el gasto y tomar decisiones informadas sobre el uso de energía en el hogar.

## Estado actual del proyecto

En el módulo 1 solo tenía la idea planteada como propuesta. Para el módulo 2 avancé bastante más de lo pedido porque me interesó el proyecto y quise dejarlo funcionando de verdad, no solo en papel.

Lo que hice hasta ahora:

- Instalé y configuré Android Studio, incluyendo el emulador (tuve varios problemas para que cargara bien, pero ya quedó funcionando con un Pixel 7).
- Armé la pantalla principal con los campos para ingresar la lectura anterior y la lectura actual del medidor.
- Programé el cálculo del consumo en kWh y del costo aproximado, mostrando el desglose de cargo fijo, energía consumida e impuestos.
- Le di formato a la pantalla y elegí un color verde para la parte superior, pensando en la temática de energía.

Todavía no he trabajado en guardar datos entre sesiones ni en dejar la tarifa configurable, eso queda pendiente para los próximos módulos.

## Herramientas utilizadas

Trabajé todo el proyecto en Android Studio, usando Kotlin y Jetpack Compose para la interfaz. Para las pruebas usé el emulador de Android configurado con un Pixel 7.

Un desafío que tuve que resolver esta semana fue que mi computador (8 GB de RAM) no siempre daba abasto para correr Android Studio y el emulador al mismo tiempo, lo que hacía que el emulador se colgara o tardara mucho en cargar. Tuve que investigar cómo liberar memoria y reiniciar procesos que quedaban corriendo en segundo plano para poder seguir probando la app con normalidad.

## Próxima semana

Para la próxima semana quiero avanzar en guardar el historial de lecturas para que no se pierda al cerrar la app, y empezar a revisar cómo estructurar mejor el código siguiendo lo que vayamos viendo en el curso.

## Cómo ejecutar el proyecto
1. Clonar este repositorio.
2. Abrir el proyecto en Android Studio.
3. Sincronizar Gradle.
4. Ejecutar en un emulador o dispositivo físico con el botón ▶️ Run.

## Referencias
- Ministerio de Energía de Chile. (2023). *Estudio revela que el 73% del consumo energético de los hogares se destina a calefacción, climatización y agua caliente.* https://energia.gob.cl/noticias/nacional/estudio-revela-que-el-73-del-consumo-energetico-de-los-hogares-se-destina-calefaccionclimatizacion-y-agua-caliente
- Android Developers. (2019). *Enfoque de prioridad de Kotlin en Android.* Google. https://developer.android.com/kotlin/first?hl=es-419