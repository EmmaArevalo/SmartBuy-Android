package com.example.smartbuysaludo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

private data class Producto(
    val nombre: String,
    val categoria: String,
    val descripcion: String,
    val precio: Int
)

private val productosDeEjemplo = listOf(
    Producto("Bolso tejido", "Accesorios", "Bolso de fibras naturales con asa corta", 68000),
    Producto("Taza de cerámica", "Hogar", "Pieza esmaltada de 300 ml", 32000),
    Producto("Libreta artesanal", "Papelería", "Cubierta de cartón y 80 hojas", 24000)
)

private fun pesos(valor: Long): String =
    "$" + NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CO")).format(valor)

@Composable
internal fun TiendaSmartBuy(onCerrarSesion: () -> Unit) {
    var cantidades by rememberSaveable { mutableStateOf(IntArray(productosDeEjemplo.size)) }
    var verCarrito by rememberSaveable { mutableStateOf(false) }
    val total = productosDeEjemplo.indices.sumOf { i ->
        productosDeEjemplo[i].precio.toLong() * cantidades[i]
    }

    fun cambiarCantidad(indice: Int, cambio: Int) {
        cantidades = cantidades.copyOf().apply {
            this[indice] = (this[indice] + cambio).coerceAtLeast(0)
        }
    }

    BackHandler(enabled = verCarrito) { verCarrito = false }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Smart Buy",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onCerrarSesion) { Text("Cerrar sesión") }
                }
            }
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
                Column(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp)
                ) {
                    if (verCarrito && total > 0) {
                        Text("Subtotal · ${pesos(total)} COP", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                    }
                    Button(
                        onClick = { verCarrito = !verCarrito },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (verCarrito) "Volver al catálogo" else "Ver carrito (${cantidades.sum()})")
                    }
                }
            }
        }
    ) { espacios ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(espacios),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = if (verCarrito) "Tu carrito" else "Catálogo",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                if (!verCarrito) {
                    Text("Productos de ejemplo", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else if (total == 0L) {
                    Text("Tu carrito está vacío. Agrega un producto desde el catálogo.")
                }
            }
            items(productosDeEjemplo.indices.filter { !verCarrito || cantidades[it] > 0 }) { indice ->
                val producto = productosDeEjemplo[indice]
                val cantidad = cantidades[indice]
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(producto.categoria, color = MaterialTheme.colorScheme.primary)
                        Text(producto.nombre, style = MaterialTheme.typography.titleLarge)
                        if (!verCarrito) Text(producto.descripcion)
                        Text(
                            text = if (verCarrito) {
                                "${pesos(producto.precio.toLong() * cantidad)} COP"
                            } else {
                                "${pesos(producto.precio.toLong())} COP"
                            },
                            fontWeight = FontWeight.Bold
                        )
                        if (verCarrito) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(onClick = { cambiarCantidad(indice, -1) }) {
                                    Text("Quitar 1")
                                }
                                Text(cantidad.toString())
                                OutlinedButton(onClick = { cambiarCantidad(indice, 1) }) {
                                    Text("Añadir 1")
                                }
                            }
                        } else {
                            Button(onClick = { cambiarCantidad(indice, 1) }) {
                                Text(if (cantidad == 0) "Agregar al carrito" else "Agregar otro · ${cantidad} en carrito")
                            }
                        }
                    }
                }
            }
        }
    }
}
