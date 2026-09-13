package com.sabelle.medidor
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ============================================================================
// CONFIGURACIÓN DE COLORES DE LA INTERFAZ
// Se definen según la paleta corporativa/energética en tonos verdes del diseño.
// ============================================================================
val VerdePrincipal = Color(0xFF1E8E3E)
val VerdeOscuroHeader = Color(0xFF137333)
val FondoPantalla = Color(0xFFF8F9FA)

// clase simple para guardar un cálculo en el historial
data class CalculoGuardado(
    val nombreMedidor: String,
    val fecha: String,
    val consumoKwh: Int,
    val costoTotal: Int
)

// las 3 pantallas de la app, para saber cual mostrar
enum class Pantalla {
    INICIO, HISTORIAL, AJUSTES
}

// ============================================================================
// ALMACENAMIENTO LOCAL (SharedPreferences)
// Guarda las tarifas y el historial en el telefono, para que no se pierdan
// cuando se cierra la app. No use base de datos porque para lo que necesito
// (unos pocos valores y una lista simple) esto es suficiente.
// ============================================================================
object Almacenamiento {
    private const val NOMBRE_PREFS = "medidor_prefs"
    private const val CLAVE_CARGO_FIJO = "cargo_fijo"
    private const val CLAVE_TARIFA = "tarifa_kwh"
    private const val CLAVE_HISTORIAL = "historial"

    fun guardarTarifas(context: Context, cargoFijo: Double, tarifaPorKwh: Double) {
        val prefs = context.getSharedPreferences(NOMBRE_PREFS, Context.MODE_PRIVATE)
        prefs.edit()
            .putFloat(CLAVE_CARGO_FIJO, cargoFijo.toFloat())
            .putFloat(CLAVE_TARIFA, tarifaPorKwh.toFloat())
            .apply()
    }

    fun leerCargoFijo(context: Context): Double {
        val prefs = context.getSharedPreferences(NOMBRE_PREFS, Context.MODE_PRIVATE)
        return prefs.getFloat(CLAVE_CARGO_FIJO, 2000f).toDouble()
    }

    fun leerTarifa(context: Context): Double {
        val prefs = context.getSharedPreferences(NOMBRE_PREFS, Context.MODE_PRIVATE)
        return prefs.getFloat(CLAVE_TARIFA, 160f).toDouble()
    }

    // guardo el historial como texto: cada calculo separado por ";;" y
    // dentro de cada calculo, sus datos separados por "|"
    fun guardarHistorial(context: Context, historial: List<CalculoGuardado>) {
        val prefs = context.getSharedPreferences(NOMBRE_PREFS, Context.MODE_PRIVATE)
        val texto = historial.joinToString(";;") { c ->
            "${c.nombreMedidor}|${c.fecha}|${c.consumoKwh}|${c.costoTotal}"
        }
        prefs.edit().putString(CLAVE_HISTORIAL, texto).apply()
    }

    fun leerHistorial(context: Context): List<CalculoGuardado> {
        val prefs = context.getSharedPreferences(NOMBRE_PREFS, Context.MODE_PRIVATE)
        val texto = prefs.getString(CLAVE_HISTORIAL, "") ?: ""
        if (texto.isBlank()) return emptyList()
        return texto.split(";;").mapNotNull { linea ->
            val partes = linea.split("|")
            if (partes.size == 4) {
                CalculoGuardado(
                    nombreMedidor = partes[0],
                    fecha = partes[1],
                    consumoKwh = partes[2].toIntOrNull() ?: 0,
                    costoTotal = partes[3].toIntOrNull() ?: 0
                )
            } else null
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = FondoPantalla
                ) {
                    AppMedidorElectrico()
                }
            }
        }
    }
}

// composable "padre" que decide que pantalla mostrar segun lo que
// el usuario toque en la barra de abajo
@Composable
fun AppMedidorElectrico() {
    val context = LocalContext.current
    var pantallaActual by remember { mutableStateOf(Pantalla.INICIO) }

    // al arrancar la app, cargo lo que haya guardado antes (o los valores
    // por defecto si es la primera vez que se abre)
    var cargoFijo by remember { mutableStateOf(Almacenamiento.leerCargoFijo(context)) }
    var tarifaPorKwh by remember { mutableStateOf(Almacenamiento.leerTarifa(context)) }
    val historial = remember {
        mutableStateListOf<CalculoGuardado>().apply { addAll(Almacenamiento.leerHistorial(context)) }
    }

    Scaffold(
        bottomBar = {
            BarraNavegacionInferior(
                pantallaActual = pantallaActual,
                onSeleccionar = { pantallaActual = it }
            )
        }
    ) { paddingInterno ->
        Box(modifier = Modifier.padding(paddingInterno)) {
            when (pantallaActual) {
                Pantalla.INICIO -> CalculadoraElectricaScreen(
                    cargoFijo = cargoFijo,
                    tarifaPorKwh = tarifaPorKwh,
                    onGuardarCalculo = { calculo ->
                        historial.add(0, calculo)
                        // guardo el historial actualizado apenas se agrega un calculo nuevo
                        Almacenamiento.guardarHistorial(context, historial)
                    }
                )
                Pantalla.AJUSTES -> AjustesScreen(
                    cargoFijo = cargoFijo,
                    tarifaPorKwh = tarifaPorKwh,
                    onGuardarAjustes = { nuevoCargo, nuevaTarifa ->
                        cargoFijo = nuevoCargo
                        tarifaPorKwh = nuevaTarifa
                        Almacenamiento.guardarTarifas(context, nuevoCargo, nuevaTarifa)
                    }
                )
                Pantalla.HISTORIAL -> HistorialScreen(historial = historial)
            }
        }
    }
}

// barra de abajo con los 3 botones (Inicio, Historial, Ajustes)
@Composable
fun BarraNavegacionInferior(pantallaActual: Pantalla, onSeleccionar: (Pantalla) -> Unit) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            selected = pantallaActual == Pantalla.INICIO,
            onClick = { onSeleccionar(Pantalla.INICIO) },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            colors = NavigationBarItemDefaults.colors(
                selectedTextColor = VerdePrincipal,
                selectedIconColor = VerdePrincipal
            )
        )
        NavigationBarItem(
            selected = pantallaActual == Pantalla.HISTORIAL,
            onClick = { onSeleccionar(Pantalla.HISTORIAL) },
            icon = { Icon(Icons.Filled.List, contentDescription = "Historial") },
            label = { Text("Historial") },
            colors = NavigationBarItemDefaults.colors(
                selectedTextColor = VerdePrincipal,
                selectedIconColor = VerdePrincipal
            )
        )
        NavigationBarItem(
            selected = pantallaActual == Pantalla.AJUSTES,
            onClick = { onSeleccionar(Pantalla.AJUSTES) },
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Ajustes") },
            label = { Text("Ajustes") },
            colors = NavigationBarItemDefaults.colors(
                selectedTextColor = VerdePrincipal,
                selectedIconColor = VerdePrincipal
            )
        )
    }
}

// ============================================================================
// PANTALLA 1: CALCULADORA (Inicio)
// ============================================================================
@Composable
fun CalculadoraElectricaScreen(
    cargoFijo: Double,
    tarifaPorKwh: Double,
    onGuardarCalculo: (CalculoGuardado) -> Unit
) {
    var nombreMedidor by remember { mutableStateOf("") }
    var lecturaAnterior by remember { mutableStateOf("8590") }
    var lecturaActual by remember { mutableStateOf("8650") }

    var consumoKwh by remember { mutableStateOf(60.0) }
    var costoEnergia by remember { mutableStateOf(9600.0) }

    // Parámetros de tarifa fija para el cálculo aproximado
    val tasaImpuesto = 0.081 // Porcentaje estimado de impuestos

    // Cálculos derivados
    val costoImpuestos = (costoEnergia + cargoFijo) * tasaImpuesto
    val costoTotal = cargoFijo + costoEnergia + costoImpuestos

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Permite desplazamiento en pantallas pequeñas
    ) {
        // ------------------------------------------------------------------------
        // 1. BARRA SUPERIOR (HEADER)
        // ------------------------------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(VerdeOscuroHeader)
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Consumo Eléctrico",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --------------------------------------------------------------------
            // 2. ENCABEZADO DE INSTRUCCIÓN
            // --------------------------------------------------------------------
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Ingresa tus lecturas del medidor",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A73E8),
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Introduce la lectura anterior y la lectura actual",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            // campo nuevo para poner el nombre del medidor (casa, oficina, etc)
            OutlinedTextField(
                value = nombreMedidor,
                onValueChange = { nombreMedidor = it },
                label = { Text("Nombre del medidor") },
                placeholder = { Text("Ej: Casa, Oficina") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VerdePrincipal)
            )

            // --------------------------------------------------------------------
            // 3. CAMPOS DE ENTRADA DE DATOS (LECTURAS)
            // --------------------------------------------------------------------
            OutlinedTextField(
                value = lecturaAnterior,
                onValueChange = { lecturaAnterior = it },
                label = { Text("Lectura Anterior") },
                suffix = { Text("kWh") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VerdePrincipal)
            )

            OutlinedTextField(
                value = lecturaActual,
                onValueChange = { lecturaActual = it },
                label = { Text("Lectura Actual") },
                suffix = { Text("kWh") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VerdePrincipal)
            )

            // --------------------------------------------------------------------
            // 4. LÓGICA DE NEGOCIO EN EL BOTÓN "CALCULAR"
            // Se convierte el texto a número con 'toDoubleOrNull()' para evitar
            // errores si el usuario ingresa caracteres inválidos.
            // --------------------------------------------------------------------
            Button(
                onClick = {
                    val ant = lecturaAnterior.toDoubleOrNull() ?: 0.0
                    val act = lecturaActual.toDoubleOrNull() ?: 0.0
                    // Regla de validación: la lectura actual debe ser mayor o igual
                    if (act >= ant) {
                        consumoKwh = act - ant
                        costoEnergia = consumoKwh * tarifaPorKwh

                        val impuestos = (costoEnergia + cargoFijo) * tasaImpuesto
                        val total = cargoFijo + costoEnergia + impuestos

                        // aca guardo el calculo en el historial cada vez que
                        // se presiona calcular
                        val fechaActual = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL")).format(Date())
                        val nombreParaGuardar = if (nombreMedidor.isBlank()) "Sin nombre" else nombreMedidor

                        onGuardarCalculo(
                            CalculoGuardado(
                                nombreMedidor = nombreParaGuardar,
                                fecha = fechaActual,
                                consumoKwh = consumoKwh.toInt(),
                                costoTotal = total.toInt()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Calcular", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "Resultado",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.DarkGray
            )

            // --------------------------------------------------------------------
            // 5. TARJETAS DE RESULTADO (MODULARIZADAS CON CardResultadoVisual)
            // --------------------------------------------------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CardResultadoVisual(
                    titulo = "Consumo",
                    valor = "${consumoKwh.toInt()} kWh",
                    subtitulo = "Consumo del periodo",
                    modifier = Modifier.weight(1f)
                )

                // Se aplica formato de miles con punto (ej: $12.450 CLP)
                CardResultadoVisual(
                    titulo = "Costo Aproximado",
                    valor = "$${String.format("%,d", costoTotal.toInt()).replace(',', '.')} CLP",
                    subtitulo = "Costo estimado",
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "Detalle del Costo",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.DarkGray
            )

            // --------------------------------------------------------------------
            // 6. DESGLOSE DETALLADO DEL COSTO
            // --------------------------------------------------------------------
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilaDetalle(etiqueta = "Cargo Fijo", valor = "$${String.format("%,d", cargoFijo.toInt()).replace(',', '.')} CLP")
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                    FilaDetalle(etiqueta = "Energía Consumida", valor = "$${String.format("%,d", costoEnergia.toInt()).replace(',', '.')} CLP")
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                    FilaDetalle(etiqueta = "Impuestos", valor = "$${String.format("%,d", costoImpuestos.toInt()).replace(',', '.')} CLP")
                }
            }

            Spacer(modifier = Modifier.height(8.dp)) // para que la barra de abajo no tape nada
        }
    }
}

// pantalla de ajustes, aca se cambia el cargo fijo y el precio del kwh
@Composable
fun AjustesScreen(
    cargoFijo: Double,
    tarifaPorKwh: Double,
    onGuardarAjustes: (Double, Double) -> Unit
) {
    // uso variables de texto aparte para que no se aplique el cambio
    // hasta que el usuario presione guardar
    var textoCargoFijo by remember { mutableStateOf(cargoFijo.toInt().toString()) }
    var textoTarifa by remember { mutableStateOf(tarifaPorKwh.toInt().toString()) }
    var mensajeGuardado by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(VerdeOscuroHeader)
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Ajustes", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = textoCargoFijo,
                onValueChange = { textoCargoFijo = it; mensajeGuardado = false },
                label = { Text("Cargo Fijo") },
                prefix = { Text("$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VerdePrincipal)
            )

            OutlinedTextField(
                value = textoTarifa,
                onValueChange = { textoTarifa = it; mensajeGuardado = false },
                label = { Text("Precio kWh") },
                prefix = { Text("$") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VerdePrincipal)
            )

            Button(
                onClick = {
                    // si el usuario escribio algo raro que no es numero, dejo el valor anterior
                    val nuevoCargo = textoCargoFijo.toDoubleOrNull() ?: cargoFijo
                    val nuevaTarifa = textoTarifa.toDoubleOrNull() ?: tarifaPorKwh
                    onGuardarAjustes(nuevoCargo, nuevaTarifa)
                    mensajeGuardado = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Guardar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            if (mensajeGuardado) {
                Text(
                    text = "Cambios guardados ✓",
                    color = VerdePrincipal,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// pantalla de historial, muestra los calculos que se han hecho hasta ahora
@Composable
fun HistorialScreen(historial: List<CalculoGuardado>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(VerdeOscuroHeader)
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Historial", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        // si todavia no hay nada calculado, muestro un mensaje en vez de una lista vacia
        if (historial.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = "Aún no hay cálculos guardados.\nVe a Inicio y presiona Calcular.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // una tarjeta por cada calculo guardado
                historial.forEach { calculo ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = calculo.nombreMedidor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(text = calculo.fecha, color = Color.Gray, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Consumo: ${calculo.consumoKwh} kWh", fontSize = 14.sp)
                                Text(
                                    text = "$${String.format("%,d", calculo.costoTotal).replace(',', '.')} CLP",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF0D6EFD)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// COMPONENTE REUTILIZABLE: TARJETA DE RESULTADOS
// Permite reutilizar el diseño visual para el Consumo y el Costo Aproximado.
// ============================================================================
@Composable
fun CardResultadoVisual(titulo: String, valor: String, subtitulo: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VerdePrincipal)
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = titulo, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = valor, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                Text(text = subtitulo, fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

// ============================================================================
// COMPONENTE REUTILIZABLE: FILA DE DETALLE DE COSTO
// ============================================================================
@Composable
fun FilaDetalle(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = etiqueta, fontSize = 15.sp, color = Color.DarkGray)
        Text(text = valor, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF0D6EFD))
    }
}