package com.sabelle.medidor
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.widget.Toast
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
    val id: Int = 0,
    val nombreMedidor: String,
    val fecha: String,
    val consumoKwh: Int,
    val costoTotal: Int,
    val lecturaActual: Int
)

// las 3 pantallas de la app, para saber cual mostrar
enum class Pantalla {
    INICIO, HISTORIAL, AJUSTES
}

// ============================================================================
// BASE DE DATOS (SQLite directo, con SQLiteOpenHelper)
// Esta semana cambie el guardado de SharedPreferences a una base de datos
// real, porque en el foro vimos que las apps deberian usar SQL para guardar
// datos que van creciendo (como el historial). Uso SQLite directo en vez de
// Room porque tuve varios problemas de compatibilidad con el plugin kapt
// que necesita Room en mi version de Android Studio.
// ============================================================================
class DBHelper(context: Context) : SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        private const val NOMBRE_BD = "medidor.db"
        private const val VERSION_BD = 1

        // nombre de las tablas y columnas, los pongo en constantes para no
        // andar escribiendo el texto a mano cada vez y equivocarme
        private const val TABLA_AJUSTES = "ajustes"
        private const val TABLA_HISTORIAL = "historial"

        private const val COL_ID = "id"
        private const val COL_CARGO_FIJO = "cargo_fijo"
        private const val COL_TARIFA = "tarifa_kwh"

        private const val COL_NOMBRE = "nombre_medidor"
        private const val COL_FECHA = "fecha"
        private const val COL_CONSUMO = "consumo_kwh"
        private const val COL_COSTO = "costo_total"
        private const val COL_LECTURA_ACTUAL = "lectura_actual"
    }

    // esto se ejecuta solo una vez, la primera vez que se crea la base de
    // datos en el telefono. Aca defino la estructura de las tablas con SQL.
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLA_AJUSTES (
                $COL_ID INTEGER PRIMARY KEY,
                $COL_CARGO_FIJO REAL,
                $COL_TARIFA REAL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLA_HISTORIAL (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NOMBRE TEXT,
                $COL_FECHA TEXT,
                $COL_CONSUMO INTEGER,
                $COL_COSTO INTEGER,
                $COL_LECTURA_ACTUAL INTEGER
            )
            """.trimIndent()
        )

        // dejo una fila inicial en ajustes con los valores por defecto,
        // para no tener que andar preguntando si existe o no cada vez
        val valoresIniciales = ContentValues().apply {
            put(COL_ID, 1)
            put(COL_CARGO_FIJO, 2000.0)
            put(COL_TARIFA, 160.0)
        }
        db.insert(TABLA_AJUSTES, null, valoresIniciales)
    }

    // esto se llama si en el futuro cambio la version de la base de datos
    // (por ejemplo si agrego una columna nueva). Por ahora, como es la
    // primera version, solo borro y creo de nuevo.
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLA_AJUSTES")
        db.execSQL("DROP TABLE IF EXISTS $TABLA_HISTORIAL")
        onCreate(db)
    }

    fun guardarTarifas(cargoFijo: Double, tarifaPorKwh: Double) {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put(COL_CARGO_FIJO, cargoFijo)
            put(COL_TARIFA, tarifaPorKwh)
        }
        // actualizo la fila con id = 1, que es la unica que uso para guardar
        // los ajustes generales de la app
        db.update(TABLA_AJUSTES, valores, "$COL_ID = ?", arrayOf("1"))
    }

    fun leerCargoFijo(): Double {
        val db = readableDatabase
        val cursor = db.query(TABLA_AJUSTES, arrayOf(COL_CARGO_FIJO), "$COL_ID = ?", arrayOf("1"), null, null, null)
        var valor = 2000.0
        if (cursor.moveToFirst()) {
            valor = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_CARGO_FIJO))
        }
        cursor.close()
        return valor
    }

    fun leerTarifa(): Double {
        val db = readableDatabase
        val cursor = db.query(TABLA_AJUSTES, arrayOf(COL_TARIFA), "$COL_ID = ?", arrayOf("1"), null, null, null)
        var valor = 160.0
        if (cursor.moveToFirst()) {
            valor = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TARIFA))
        }
        cursor.close()
        return valor
    }

    // guarda un calculo nuevo en la tabla historial (un INSERT normal de SQL)
    fun guardarCalculo(calculo: CalculoGuardado) {
        val db = writableDatabase
        val valores = ContentValues().apply {
            put(COL_NOMBRE, calculo.nombreMedidor)
            put(COL_FECHA, calculo.fecha)
            put(COL_CONSUMO, calculo.consumoKwh)
            put(COL_COSTO, calculo.costoTotal)
            put(COL_LECTURA_ACTUAL, calculo.lecturaActual)
        }
        db.insert(TABLA_HISTORIAL, null, valores)
    }

    // trae todos los calculos guardados, ordenados del mas nuevo al mas viejo
    fun leerHistorial(): List<CalculoGuardado> {
        val lista = mutableListOf<CalculoGuardado>()
        val db = readableDatabase
        val cursor = db.query(TABLA_HISTORIAL, null, null, null, null, null, "$COL_ID DESC")
        while (cursor.moveToNext()) {
            lista.add(
                CalculoGuardado(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    nombreMedidor = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOMBRE)),
                    fecha = cursor.getString(cursor.getColumnIndexOrThrow(COL_FECHA)),
                    consumoKwh = cursor.getInt(cursor.getColumnIndexOrThrow(COL_CONSUMO)),
                    costoTotal = cursor.getInt(cursor.getColumnIndexOrThrow(COL_COSTO)),
                    lecturaActual = cursor.getInt(cursor.getColumnIndexOrThrow(COL_LECTURA_ACTUAL))
                )
            )
        }
        cursor.close()
        return lista
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
    // creo el helper de la base de datos una sola vez, con remember, para
    // no estar abriendo la base de datos de nuevo en cada recomposicion
    val dbHelper = remember { DBHelper(context) }

    var pantallaActual by remember { mutableStateOf(Pantalla.INICIO) }

    var cargoFijo by remember { mutableStateOf(dbHelper.leerCargoFijo()) }
    var tarifaPorKwh by remember { mutableStateOf(dbHelper.leerTarifa()) }
    val historial = remember {
        mutableStateListOf<CalculoGuardado>().apply { addAll(dbHelper.leerHistorial()) }
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
                    historial = historial,
                    onGuardarCalculo = { calculo ->
                        dbHelper.guardarCalculo(calculo)
                        // vuelvo a leer todo el historial desde la base de datos,
                        // asi me aseguro que el id autoincremental quede bien
                        historial.clear()
                        historial.addAll(dbHelper.leerHistorial())
                    }
                )
                Pantalla.AJUSTES -> AjustesScreen(
                    cargoFijo = cargoFijo,
                    tarifaPorKwh = tarifaPorKwh,
                    onGuardarAjustes = { nuevoCargo, nuevaTarifa ->
                        cargoFijo = nuevoCargo
                        tarifaPorKwh = nuevaTarifa
                        dbHelper.guardarTarifas(nuevoCargo, nuevaTarifa)
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
    historial: List<CalculoGuardado>,
    onGuardarCalculo: (CalculoGuardado) -> Unit
) {
    val context = LocalContext.current

    var nombreMedidor by remember { mutableStateOf("") }
    var lecturaAnterior by remember { mutableStateOf("") }
    var lecturaActual by remember { mutableStateOf("") }
    var lecturaAnteriorBloqueada by remember { mutableStateOf(false) }
    var mostrarListaMedidores by remember { mutableStateOf(false) }

    var consumoKwh by remember { mutableStateOf(0.0) }
    var costoEnergia by remember { mutableStateOf(0.0) }

    val tasaImpuesto = 0.081 // Porcentaje estimado de impuestos

    val costoImpuestos = (costoEnergia + cargoFijo) * tasaImpuesto
    val costoTotal = cargoFijo + costoEnergia + costoImpuestos

    // saco la lista de nombres de medidores que ya se han usado antes,
    // sin repetidos, para mostrarlos en el boton de "elegir medidor guardado"
    val nombresGuardados = remember(historial) {
        historial.map { it.nombreMedidor }.distinct()
    }

    // busca cual fue la ultima lectura actual guardada para un medidor
    fun buscarUltimaLectura(nombre: String): Int {
        return historial.firstOrNull { it.nombreMedidor == nombre }?.lecturaActual ?: 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
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

            Column {
                OutlinedTextField(
                    value = nombreMedidor,
                    onValueChange = {
                        nombreMedidor = it
                        lecturaAnteriorBloqueada = false
                    },
                    label = { Text("Nombre del medidor") },
                    placeholder = { Text("Ej: Casa, Oficina") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VerdePrincipal)
                )

                if (nombresGuardados.isNotEmpty()) {
                    Box {
                        TextButton(onClick = { mostrarListaMedidores = true }) {
                            Text("Elegir medidor guardado", color = VerdePrincipal)
                        }
                        DropdownMenu(
                            expanded = mostrarListaMedidores,
                            onDismissRequest = { mostrarListaMedidores = false }
                        ) {
                            nombresGuardados.forEach { nombre ->
                                DropdownMenuItem(
                                    text = { Text(nombre) },
                                    onClick = {
                                        nombreMedidor = nombre
                                        lecturaAnterior = buscarUltimaLectura(nombre).toString()
                                        lecturaAnteriorBloqueada = true
                                        mostrarListaMedidores = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = lecturaAnterior,
                onValueChange = { nuevoValor ->
                    lecturaAnterior = nuevoValor.filter { it.isDigit() }
                },
                label = { Text("Lectura Anterior") },
                suffix = { Text("kWh") },
                readOnly = lecturaAnteriorBloqueada,
                supportingText = {
                    if (lecturaAnteriorBloqueada) {
                        Text("Se completa sola con tu última lectura de este medidor")
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VerdePrincipal)
            )

            OutlinedTextField(
                value = lecturaActual,
                onValueChange = { nuevoValor ->
                    lecturaActual = nuevoValor.filter { it.isDigit() }
                },
                label = { Text("Lectura Actual") },
                suffix = { Text("kWh") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VerdePrincipal)
            )

            Button(
                onClick = {
                    if (nombreMedidor.isBlank()) {
                        Toast.makeText(context, "Debes ingresar el nombre del medidor", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val ant = lecturaAnterior.toDoubleOrNull() ?: 0.0
                    val act = lecturaActual.toDoubleOrNull() ?: 0.0

                    if (act < ant) {
                        Toast.makeText(context, "La lectura actual no puede ser menor a la anterior", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    consumoKwh = act - ant
                    costoEnergia = consumoKwh * tarifaPorKwh

                    val impuestos = (costoEnergia + cargoFijo) * tasaImpuesto
                    val total = cargoFijo + costoEnergia + impuestos

                    val fechaActual = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL")).format(Date())

                    onGuardarCalculo(
                        CalculoGuardado(
                            nombreMedidor = nombreMedidor,
                            fecha = fechaActual,
                            consumoKwh = consumoKwh.toInt(),
                            costoTotal = total.toInt(),
                            lecturaActual = act.toInt()
                        )
                    )

                    Toast.makeText(context, "Lectura guardada exitosamente", Toast.LENGTH_SHORT).show()
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

                CardResultadoVisual(
                    titulo = "Costo Aproximado",
                    valor = "$${String.format(Locale("es", "CL"), "%,d", costoTotal.toInt()).replace(',', '.')} CLP",
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

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilaDetalle(etiqueta = "Cargo Fijo", valor = "$${String.format(Locale("es", "CL"), "%,d", cargoFijo.toInt()).replace(',', '.')} CLP")
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                    FilaDetalle(etiqueta = "Energía Consumida", valor = "$${String.format(Locale("es", "CL"), "%,d", costoEnergia.toInt()).replace(',', '.')} CLP")
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                    FilaDetalle(etiqueta = "Impuestos", valor = "$${String.format(Locale("es", "CL"), "%,d", costoImpuestos.toInt()).replace(',', '.')} CLP")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
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
                                    text = "$${String.format(Locale("es", "CL"), "%,d", calculo.costoTotal).replace(',', '.')} CLP",
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