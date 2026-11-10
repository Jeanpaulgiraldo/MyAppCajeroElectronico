package com.example.myapplication

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.text.NumberFormat
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CajeroApp()
            }
        }
    }
}

private const val PREFS_NAME = "CajeroPrefs"
private const val KEY_SALDO = "saldo_actual"

private fun obtenerSaldoGuardado(context: Context): Double {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getFloat(KEY_SALDO, 0.0f).toDouble()
}

private fun guardarSaldo(context: Context, nuevoSaldo: Double) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit {
        putFloat(KEY_SALDO, nuevoSaldo.toFloat())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CajeroApp() {
    val context = LocalContext.current
    var saldo by remember { mutableDoubleStateOf(obtenerSaldoGuardado(context)) }
    var montoConsignar by remember { mutableStateOf("") }
    var montoRetirar by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf("") }
    var esError by remember { mutableStateOf(false) }

    val formatoMoneda = remember {
        NumberFormat.getCurrencyInstance(
            Locale.Builder().setLanguage("es").setRegion("CO").build()
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Cajero Electrónico",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Card para mostrar el Saldo Actual
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Saldo Disponible",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatoMoneda.format(saldo),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Vista Dividida (Split View): Consignar a la izquierda, Retirar a la derecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Panel Izquierdo: Consignar
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Consignar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = montoConsignar,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                    montoConsignar = input
                                }
                            },
                            label = { Text("Monto ($)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val monto = montoConsignar.toDoubleOrNull()
                                if (monto == null || monto <= 0) {
                                    mensaje = "Ingrese un monto válido para consignar."
                                    esError = true
                                } else {
                                    saldo += monto
                                    guardarSaldo(context, saldo)
                                    mensaje = "Consignación exitosa: +${formatoMoneda.format(monto)}"
                                    esError = false
                                    montoConsignar = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Consignar", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Panel Derecho: Retirar
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Retirar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = montoRetirar,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                    montoRetirar = input
                                }
                            },
                            label = { Text("Monto ($)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val monto = montoRetirar.toDoubleOrNull()
                                if (monto == null || monto <= 0) {
                                    mensaje = "Ingrese un monto válido para retirar."
                                    esError = true
                                } else if (monto > saldo) {
                                    // Validación: Retiro mayor al saldo existente -> Transacción Inválida
                                    mensaje = "Transacción inválida: Saldo insuficiente para retirar ${formatoMoneda.format(monto)}."
                                    esError = true
                                } else {
                                    saldo -= monto
                                    guardarSaldo(context, saldo)
                                    mensaje = "Retiro exitoso: -${formatoMoneda.format(monto)}"
                                    esError = false
                                    montoRetirar = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text("Retirar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Card para mostrar el resultado o mensaje de error/éxito
            if (mensaje.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (esError) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = mensaje,
                        modifier = Modifier.padding(16.dp),
                        color = if (esError) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
fun CajeroPreview() {
    MyApplicationTheme {
        CajeroApp()
    }
}