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

class ReportsScreen(
    private val context: Context,
    private val db: ProductDatabase
) {
    fun buildView(): LinearLayout {
        val raiz = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(context.dp(16), context.dp(12), context.dp(16), context.dp(16))
        }

        val scroll = ScrollView(context)
        val contenido = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val desde = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        val hasta = System.currentTimeMillis()

        val totalVentas = db.totalVentas(desde, hasta)
        val ganancia = db.gananciaRegistrada(desde, hasta)
        val porMoneda = db.totalVentasPorMoneda(desde, hasta)

        contenido.addView(TextView(context).apply {
            text = "Informes y ganancias"
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(24, 65, 120))
        })

        contenido.addView(TextView(context).apply {
            text = "Ventas: ${dinero(totalVentas)} CUP"
            textSize = 16f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.START
            setPadding(0, context.dp(10), 0, context.dp(10))
        })

        contenido.addView(TextView(context).apply {
            text = "Ganancia: ${dinero(ganancia)} CUP"
            textSize = 16f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.START
            setPadding(0, context.dp(10), 0, context.dp(10))
        })

        val monedaText = TextView(context).apply {
            text = "Resumen por moneda:\n" + porMoneda.entries.joinToString(separator = "\n") {
                "${it.key}: ${dinero(it.value)}"
            }
            textSize = 15f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.START
            setPadding(0, context.dp(10), 0, context.dp(10))
        }

        contenido.addView(monedaText)
        scroll.addView(contenido)
        raiz.addView(scroll)

        return raiz
    }
}