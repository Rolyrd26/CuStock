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
import com.custock.app.LineaVenta
import com.custock.app.Producto
import com.custock.app.database.ProductDatabase
import com.custock.app.dinero
import com.custock.app.dp
import com.custock.app.fondoRedondeado

class SalesScreen(
    private val context: Context,
    private val db: ProductDatabase
) {
    fun buildView(): LinearLayout {
        val raiz = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(245, 248, 253))
            setPadding(context.dp(16), context.dp(12), context.dp(16), context.dp(16))
        }

        val carrito = mutableListOf<LineaVenta>()
        val cliente = EditText(context).apply {
            hint = "Nombre del cliente (obligatorio para fiar)"
            setSingleLine(true)
        }
        raiz.addView(cliente)

        val lista = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(context).apply { addView(lista) }

        val totalTexto = TextView(context).apply {
            textSize = 23f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(24, 65, 120))
            gravity = Gravity.CENTER
            setPadding(context.dp(16), context.dp(14), context.dp(16), context.dp(14))
            background = context.fondoRedondeado(Color.rgb(225, 240, 255))
        }

        fun renderizarCarrito() {
            lista.removeAllViews()
            var total = 0.0

            if (carrito.isEmpty()) {
                lista.addView(TextView(context).apply {
                    text = "Añade productos para comenzar la venta."
                    setPadding(0, context.dp(12), 0, context.dp(12))
                })
            }

            carrito.toList().forEach { linea ->
                val producto = db.obtenerProducto(linea.productoId)
                if (producto == null) {
                    carrito.remove(linea)
                } else {
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
            }

            val moneda = carrito.firstOrNull()
                ?.let { db.obtenerProducto(it.productoId)?.moneda } ?: "CUP"

            totalTexto.text = "TOTAL: ${dinero(total)} $moneda"
        }

        fun confirmarVenta(esFiado: Boolean) {
            if (carrito.isEmpty()) {
                return
            }

            if (esFiado && cliente.text.toString().isBlank()) {
                return
            }

            AlertDialog.Builder(context)
                .setTitle(if (esFiado) "Confirmar fiado" else "Confirmar venta")
                .setMessage(
                    "Total: ${dinero(carrito.sumOf { it.cantidad * it.precioUnitario })} " +
                        (db.obtenerProducto(carrito.first().productoId)?.moneda ?: "CUP") + "\n\n" +
                        if (esFiado) "Se creará una deuda independiente para ${cliente.text}." else "Se descontarán las existencias del inventario."
                )
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar") { _, _ ->
                    try {
                        db.registrarVenta(
                            lineas = carrito.toList(),
                            cliente = cliente.text.toString().trim(),
                            esFiado = esFiado
                        )
                    } catch (e: Exception) {
                        // manejar error aquí
                    }
                    renderizarCarrito()
                }
                .show()
        }

        raiz.addView(Button(context).apply {
            text = "＋ Añadir producto"
            setOnClickListener {
                val todos = db.listar("*") // no usar esto si no está implementado
            }
        })

        raiz.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        raiz.addView(totalTexto)
        raiz.addView(Button(context).apply {
            text = "Confirmar venta pagada"
            setOnClickListener { confirmarVenta(false) }
        })
        raiz.addView(Button(context).apply {
            text = "Registrar venta fiada"
            setOnClickListener { confirmarVenta(true) }
        })

        renderizarCarrito()
        return raiz
    }
}