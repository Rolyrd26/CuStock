package com.custock.app.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.custock.app.database.ProductDatabase
import com.custock.app.dinero
import com.custock.app.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportsScreen(
    private val context: Context,
    private val db: ProductDatabase,
    private val onBack: () -> Unit
) {
    fun buildView(): LinearLayout {
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
            text = "📊 Informes"
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        })

        textos.addView(TextView(context).apply {
            text = "Ventas y ganancias"
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

        val scroll = ScrollView(context)
        val cuerpo = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val desde = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        val hasta = System.currentTimeMillis()

                val ventasPorMoneda = db.totalVentasPorMoneda(desde, hasta)
        val gananciasPorMoneda = db.gananciaPorMoneda(desde, hasta)

        cuerpo.addView(TextView(context).apply {
            text = "Resumen del último mes"
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(24, 65, 120))
        })

        cuerpo.addView(TextView(context).apply {
            text = "Ventas por moneda"
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(24, 65, 120))
            setPadding(0, context.dp(12), 0, context.dp(4))
        })

        val resumenVentas = if (ventasPorMoneda.isEmpty()) {
            "No hay ventas registradas."
        } else {
            ventasPorMoneda.entries
                .sortedBy { it.key }
                .joinToString(separator = "\n") {
                    "${it.key}: ${dinero(it.value)} ${it.key}"
                }
        }

        cuerpo.addView(TextView(context).apply {
            text = resumenVentas
            textSize = 16f
            setTextColor(Color.DKGRAY)
            setPadding(0, context.dp(4), 0, context.dp(10))
        })

        cuerpo.addView(TextView(context).apply {
            text = "Ganancias por moneda"
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(24, 65, 120))
            setPadding(0, context.dp(8), 0, context.dp(4))
        })

        val resumenGanancias = if (gananciasPorMoneda.isEmpty()) {
            "No hay ganancias registradas."
        } else {
            gananciasPorMoneda.entries
                .sortedBy { it.key }
                .joinToString(separator = "\n") {
                    "${it.key}: ${dinero(it.value)} ${it.key}"
                }
        }

        cuerpo.addView(TextView(context).apply {
            text = resumenGanancias
            textSize = 16f
            setTextColor(Color.DKGRAY)
            setPadding(0, context.dp(4), 0, context.dp(10))
        })

        cuerpo.addView(TextView(context).apply {
            text = "Detalle de productos vendidos"
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(24, 65, 120))
            setPadding(0, context.dp(20), 0, context.dp(10))
        })

        val formatoFecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val ventasDetalle = db.ventasDelPeriodo(desde, hasta)

        if (ventasDetalle.isEmpty()) {
            cuerpo.addView(TextView(context).apply {
                text = "No hay ventas registradas en los últimos 30 días."
                textSize = 15f
                setTextColor(Color.DKGRAY)
                setPadding(0, context.dp(8), 0, context.dp(12))
            })
        } else {
            ventasDetalle.forEach { venta ->
                val detalleVenta = buildString {
                    append("Venta #${venta.id}\n")
                    append("Fecha: ${formatoFecha.format(Date(venta.fecha))}\n")
                    append("Cliente: ${venta.cliente.ifBlank { "Cliente general" }}\n")
                    append("Tipo: ${if (venta.esFiado) "Fiada" else "Pagada"}\n\n")

                    venta.lineas.forEach { linea ->
                        append("• ${linea.nombre}\n")
                        append("  Cantidad: ${linea.cantidad}\n")
                        append("  Precio unitario: ${dinero(linea.precioUnitario)} ${linea.moneda}\n")
                        append("  Subtotal: ${dinero(linea.subtotal)} ${linea.moneda}\n\n")
                    }

                    append("TOTAL: ${dinero(venta.total)}")
                }

                cuerpo.addView(TextView(context).apply {
                    text = detalleVenta
                    textSize = 15f
                    setTextColor(Color.rgb(45, 55, 70))
                    setPadding(
                        context.dp(12),
                        context.dp(12),
                        context.dp(12),
                        context.dp(12)
                    )
                    background = android.graphics.drawable.GradientDrawable().apply {
                        cornerRadius = context.dp(12).toFloat()
                        setColor(Color.rgb(235, 242, 250))
                    }
                })

                cuerpo.addView(TextView(context).apply {
                    height = context.dp(10)
                })
            }
        }

        scroll.addView(cuerpo)
        contenido.addView(scroll)
        raiz.addView(contenido)

        return raiz
    }
}
