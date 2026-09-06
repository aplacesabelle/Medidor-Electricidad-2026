# Medidor Eléctrico

Aplicación Android que calcula el consumo y costo aproximado de electricidad a partir de las lecturas del medidor del hogar.

## Descripción del proyecto

La app permite al usuario ingresar la lectura anterior y la lectura actual de su medidor eléctrico, calcular el consumo en kWh y obtener una estimación del costo en pesos chilenos (CLP), incluyendo el desglose de cargo fijo, energía consumida e impuestos.

Nace como respuesta a un problema real: en Chile, los usuarios generalmente solo conocen su consumo eléctrico al recibir la boleta mensual, lo que dificulta planificar el gasto y tomar decisiones informadas sobre el uso de energía en el hogar.

## Estado actual del proyecto

Este proyecto comenzó como una propuesta (módulo 1) y actualmente cuenta con una primera versión funcional (módulo 2), con la pantalla principal de cálculo completamente operativa.

### Funcionalidades implementadas
- Ingreso de lectura anterior y lectura actual del medidor.
- Cálculo automático del consumo en kWh (lectura actual − lectura anterior).
- Cálculo del costo aproximado, desglosado en:
    - Cargo fijo
    - Energía consumida
    - Impuestos
- Interfaz visual con paleta de colores verde, pensada para transmitir la temática de energía/eficiencia.

## Tecnologías utilizadas
- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose
- **Entorno:** Android Studio
- **Pruebas:** Emulador Android (Pixel 7, configurado dentro de Android Studio)

## Cómo ejecutar el proyecto
1. Clonar este repositorio.
2. Abrir el proyecto en Android Studio.
3. Sincronizar Gradle.
4. Ejecutar en un emulador o dispositivo físico con el botón ▶️ Run.

## Próximos pasos
- Guardar historial de lecturas entre sesiones.
- Permitir configurar la tarifa por kWh.
- Mejorar la validación de datos ingresados por el usuario.

## Referencias
- Ministerio de Energía de Chile. (2023). *Estudio revela que el 73% del consumo energético de los hogares se destina a calefacción, climatización y agua caliente.* https://energia.gob.cl/noticias/nacional/estudio-revela-que-el-73-del-consumo-energetico-de-los-hogares-se-destina-calefaccionclimatizacion-y-agua-caliente
- Android Developers. (2019). *Enfoque de prioridad de Kotlin en Android.* Google. https://developer.android.com/kotlin/first?hl=es-419