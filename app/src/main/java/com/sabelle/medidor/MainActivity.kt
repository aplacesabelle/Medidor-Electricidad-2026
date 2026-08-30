package com.sabelle.medidor
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================================================
// CONFIGURACIÓN DE COLORES DE LA INTERFAZ
// Se definen según la paleta corporativa/energética en tonos verdes del diseño.
// ============================================================================
val VerdePrincipal = Color(0xFF1E8E3E)
val VerdeOscuroHeader = Color(0xFF137333)
val FondoPantalla = Color(0xFFF8F9FA)

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
                    CalculadoraElectricaScreen()
                }
            }
        }
    }
}

@Composable
fun CalculadoraElectricaScreen() {
    // ============================================================================
    // ESTADOS (VARIABLES DINÁMICAS)
    // Se usa 'remember' y 'mutableStateOf' para que Compose detecte cambios en los
    // datos y redibuje automáticamente la pantalla cuando el usuario interactúe.
    // ============================================================================
    var lecturaAnterior by remember { mutableStateOf("8590") }
    var lecturaActual by remember { mutableStateOf("8650") }

    var consumoKwh by remember { mutableStateOf(60.0) }
    var costoEnergia by remember { mutableStateOf(9600.0) }

    // Parámetros de tarifa fija para el cálculo aproximado
    val cargoFijo = 2000.0
    val tarifaPorKwh = 160.0 // Tarifa base por kWh
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