package com.custock.app.ui

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.custock.app.LineaVenta
import com.custock.app.database.ProductDatabase
import com.custock.app.dinero
import com.custock.app.dp
import com.custock.app.fondoRedondeado

class SalesScreen(
    private val context: Context,
    private val db: ProductDatabase,
    private val categorias: List<String>,
    private val onBack: () -> Unit
) {
    fun buildView(): LinearLayout {
        val carrito = mutableListOf<LineaVenta>()
        val raiz = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(245, 248, 253))
        }

        val cabecera = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(context.dp(24), context.dp(34), context.dp(16), context.dp(28))
            background = android.graphics.drawable.GradientDrawable(
                android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(24, 65, 120), Color.rgb(45, 115, 190))
            )
        }

        val fila = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val textos = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }

        textos.addView(TextView(context).apply {
            text = "🛒 Nueva venta"
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        })

        textos.addView(TextView(context).apply {
            text = "Carrito y registro de ventas"
            textSize = 14f
            setTextColor(Color.rgb(230, 238, 250))
            setPadding(0, context.dp(5), 0, 0)
        })

        fila.addView(textos, LinearLayout.LayoutParams(0, -2, 1f))

        fila.addView(TextView(context).apply {
            text = "←"
            textSize = 30f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(context.dp(12), context.dp(8), context.dp(8), context.dp(8))
            setOnClickListener { onBack() }
        })

        cabecera.addView(fila)
        raiz.addView(cabecera)

        val contenido = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(context.dp(16), context.dp(16), context.dp(16), context.dp(20))
        }

        val cliente = EditText(context).apply {
            hint = "Nombre del cliente (obligatorio para fiar)"
            setSingleLine(true)
            setPadding(context.dp(12), context.dp(10), context.dp(12), context.dp(10))
            background = context.fondoRedondeado(Color.WHITE)
        }

        contenido.addView(cliente)

        val lista = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val scroll = ScrollView(context).apply {
            addView(lista)
        }

        contenido.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val totalTexto = TextView(context).apply {
            text = "TOTAL: 0.00 CUP"
            textSize = 23f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(24, 65, 120))
            gravity = Gravity.CENTER
            setPadding(context.dp(16), context.dp(14), context.dp(16), context.dp(14))
            background = context.fondoRedondeado(Color.rgb(225, 240, 255))
        }
        contenido.addView(totalTexto)

        fun renderizarCarrito() {
            lista.removeAllViews()
            var total = 0.0

            if (carrito.isEmpty()) {
                lista.addView(TextView(context).apply {
                    text = "Añade productos para comenzar la venta."
                    setPadding(0, context.dp(12), 0, context.dp(12))
                })
            }

            carrito.forEach { linea ->
                val producto = db.obtenerProducto(linea.productoId)
                if (producto == null) {
                    carrito.remove(linea)
                    return@forEach
                }

                val subtotal = linea.cantidad * linea.precioUnitario
                total += subtotal

                val tarjeta = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(context.dp(14), context.dp(12), context.dp(14), context.dp(12))
                    background = context.fondoRedondeado(Color.WHITE)
                }

                tarjeta.addView(TextView(context).apply {
                    text = producto.nombre
                    textSize = 17f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(Color.rgb(24, 65, 120))
                })

                tarjeta.addView(TextView(context).apply {
                    text = "${linea.cantidad} × ${dinero(linea.precioUnitario)} ${producto.moneda}\n" +
                        "Subtotal: ${dinero(subtotal)} ${producto.moneda}"
                    textSize = 14f
                    setTextColor(Color.DKGRAY)
                    setPadding(0, context.dp(6), 0, context.dp(8))
                })

                tarjeta.addView(Button(context).apply {
                    text = "Quitar del carrito"
                    isAllCaps = false
                    textSize = 14f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(Color.rgb(190, 45, 55))
                    background = context.fondoRedondeado(Color.rgb(255, 238, 239))
                    setPadding(context.dp(12), context.dp(8), context.dp(12), context.dp(8))
                    setOnClickListener {
                        carrito.removeAll { it.productoId == linea.productoId }
                        renderizarCarrito()
                    }
                })

                lista.addView(tarjeta)
                lista.addView(TextView(context).apply { height = context.dp(8) })
            }

            val moneda = carrito.firstOrNull()
                ?.let { db.obtenerProducto(it.productoId)?.moneda } ?: "CUP"

            totalTexto.text = "TOTAL: ${dinero(total)} $moneda"
        }

        fun registrarVenta(esFiado: Boolean) {
            if (carrito.isEmpty()) {
                AlertDialog.Builder(context)
                    .setTitle("Venta vacía")
                    .setMessage("Añade al menos un producto antes de confirmar.")
                    .setPositiveButton("Aceptar", null)
                    .show()
                return
            }

            if (esFiado && cliente.text.toString().isBlank()) {
                AlertDialog.Builder(context)
                    .setTitle("Cliente requerido")
                    .setMessage("Necesitas escribir el nombre del cliente para registrar un fiado.")
                    .setPositiveButton("Aceptar", null)
                    .show()
                return
            }

            val total = carrito.sumOf { it.cantidad * it.precioUnitario }
            val monedaVenta = db.obtenerProducto(carrito.first().productoId)?.moneda ?: "CUP"

            AlertDialog.Builder(context)
                .setTitle(if (esFiado) "Confirmar fiado" else "Confirmar venta")
                .setMessage(
                    "Total: ${dinero(total)} $monedaVenta\n\n" +
                        if (esFiado) "Se creará una deuda independiente para ${cliente.text.toString().trim()}."
                        else "Se descontarán las existencias del inventario."
                )
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar") { _, _ ->
                    try {
                        db.registrarVenta(
                            lineas = carrito.toList(),
                            cliente = cliente.text.toString().trim(),
                            esFiado = esFiado
                        )
                        AlertDialog.Builder(context)
                            .setTitle("Venta registrada")
                            .setMessage("La venta se registró correctamente.")
                            .setPositiveButton("Aceptar", null)
                            .show()
                        carrito.clear()
                        renderizarCarrito()
                    } catch (e: Exception) {
                        AlertDialog.Builder(context)
                            .setTitle("Error")
                            .setMessage(e.message ?: "No se pudo registrar la venta.")
                            .setPositiveButton("Aceptar", null)
                            .show()
                    }
                }
                .show()
        }

        fun abrirSelectorProductos() {
            val categoriasDisponibles = categorias.filter { categoria ->
                db.listar(categoria).any { it.cantidad > 0 }
            }

            if (categoriasDisponibles.isEmpty()) {
                Toast.makeText(context, "No hay productos con stock disponible.", Toast.LENGTH_LONG).show()
                return
            }

            val opcionesCategorias = categoriasDisponibles.toTypedArray()

            AlertDialog.Builder(context)
                .setTitle("Selecciona una categoría")
                .setItems(opcionesCategorias) { _, indiceCategoria ->
                    val categoriaSeleccionada = opcionesCategorias[indiceCategoria]
                    val productos = db.listar(categoriaSeleccionada).filter { it.cantidad > 0 }

                    if (productos.isEmpty()) {
                        Toast.makeText(context, "No hay productos disponibles en esa categoría.", Toast.LENGTH_LONG).show()
                        return@setItems
                    }

                    val opcionesProductos = productos.map {
                        "${it.nombre} — ${dinero(it.venta)} ${it.moneda} (stock: ${it.cantidad})"
                    }.toTypedArray()

                    AlertDialog.Builder(context)
                        .setTitle("Selecciona un producto")
                        .setItems(opcionesProductos) { _, indiceProducto ->
                            val producto = productos[indiceProducto]

                            if (carrito.isNotEmpty()) {
                                val monedaActual = db.obtenerProducto(carrito.first().productoId)?.moneda
                                if (monedaActual != null && monedaActual != producto.moneda) {
                                    AlertDialog.Builder(context)
                                        .setTitle("Moneda distinta")
                                        .setMessage("No puedes mezclar monedas en la misma venta.")
                                        .setPositiveButton("Aceptar", null)
                                        .show()
                                    return@setItems
                                }
                            }

                            val yaEnCarrito = carrito.filter { it.productoId == producto.id }.sumOf { it.cantidad }
                            val restante = producto.cantidad - yaEnCarrito

                            if (restante <= 0) {
                                AlertDialog.Builder(context)
                                    .setTitle("Stock agotado")
                                    .setMessage("Ya añadiste todas las existencias disponibles de ${producto.nombre}.")
                                    .setPositiveButton("Aceptar", null)
                                    .show()
                                return@setItems
                            }

                            val campoCantidad = EditText(context).apply {
                                hint = "Cantidad (máximo $restante)"
                                inputType = InputType.TYPE_CLASS_NUMBER
                                setText("1")
                                selectAll()
                            }

                            val dialogCantidad = AlertDialog.Builder(context)
                                .setTitle(producto.nombre)
                                .setMessage("Disponibles: $restante")
                                .setView(campoCantidad)
                                .setNegativeButton("Cancelar", null)
                                .setPositiveButton("Añadir", null)
                                .create()

                            dialogCantidad.setOnShowListener {
                                dialogCantidad.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                                    val cantidad = campoCantidad.text.toString().toIntOrNull()
                                    if (cantidad == null || cantidad <= 0 || cantidad > restante) {
                                        AlertDialog.Builder(context)
                                            .setTitle("Cantidad inválida")
                                            .setMessage("Introduce una cantidad entre 1 y $restante.")
                                            .setPositiveButton("Aceptar", null)
                                            .show()
                                    } else {
                                        val idx = carrito.indexOfFirst { it.productoId == producto.id }
                                        if (idx >= 0) {
                                            val actual = carrito[idx]
                                            carrito[idx] = actual.copy(cantidad = actual.cantidad + cantidad)
                                        } else {
                                            carrito.add(LineaVenta(producto.id, cantidad, producto.venta))
                                        }
                                        dialogCantidad.dismiss()
                                        renderizarCarrito()
                                    }
                                }
                            }

                            dialogCantidad.show()
                        }
                        .show()
                    }
                }
                .show()
        }

        val btnAgregarProducto = Button(context).apply {
            text = "＋ Añadir producto"
            typeface = Typeface.DEFAULT_BOLD
            setOnClickListener { abrirSelectorProductos() }
        }

        val btnConfirmarPagada = Button(context).apply {
            text = "Confirmar venta pagada"
            typeface = Typeface.DEFAULT_BOLD
            setOnClickListener { registrarVenta(false) }
        }

        val btnConfirmarFiada = Button(context).apply {
            text = "Registrar venta fiada"
            typeface = Typeface.DEFAULT_BOLD
            setOnClickListener { registrarVenta(true) }
        }

        contenido.addView(btnAgregarProducto)
        contenido.addView(btnConfirmarPagada)
        contenido.addView(btnConfirmarFiada)
        raiz.addView(contenido)

        renderizarCarrito()
        return raiz
    }
}