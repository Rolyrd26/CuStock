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

        val totalVentas = db.totalVentas(desde, hasta)
        val ganancia = db.gananciaRegistrada(desde, hasta)
        val porMoneda = db.totalVentasPorMoneda(desde, hasta)

        cuerpo.addView(TextView(context).apply {
            text = "Resumen del último mes"
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(24, 65, 120))
        })

        cuerpo.addView(TextView(context).apply {
            text = "Ventas: ${dinero(totalVentas)} CUP"
            textSize = 16f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.START
            setPadding(0, context.dp(10), 0, context.dp(10))
        })

        cuerpo.addView(TextView(context).apply {
            text = "Ganancia: ${dinero(ganancia)} CUP"
            textSize = 16f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.START
            setPadding(0, context.dp(10), 0, context.dp(10))
        })

        val resumenMoneda = "Resumen por moneda:\n" + porMoneda.entries.joinToString(separator = "\n") {
            "${it.key}: ${dinero(it.value)}"
        }

        cuerpo.addView(TextView(context).apply {
            text = resumenMoneda
            textSize = 15f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.START
            setPadding(0, context.dp(10), 0, context.dp(10))
        })

        scroll.addView(cuerpo)
        contenido.addView(scroll)
        raiz.addView(contenido)

        return raiz
    }
}