package com.custock.app.ui

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.custock.app.AppConstants
import com.custock.app.dp

class SettingsScreen(
    private val context: Context,
    private val prefs: SharedPreferences,
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
            text = "⚙️ Ajustes"
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        })

        textos.addView(TextView(context).apply {
            text = "Personaliza CuStock"
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
            setPadding(context.dp(18), context.dp(20), context.dp(18), context.dp(20))
        }

        val btnTema = Button(context).apply {
            text = "Activar tema oscuro"
            setOnClickListener {
                val actual = prefs.getBoolean(AppConstants.KEY_TEMA_OSCURO, false)
                prefs.edit().putBoolean(AppConstants.KEY_TEMA_OSCURO, !actual).apply()
            }
        }

        val btnCopia = Button(context).apply {
            text = "Copias de seguridad"
            setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle("Copias de seguridad")
                    .setMessage("Funcionalidad pendiente: exportación o restauración de la base de datos.")
                    .setPositiveButton("Aceptar", null)
                    .show()
            }
        }

        contenido.addView(btnTema)
        contenido.addView(btnCopia)

        raiz.addView(contenido)
        return raiz
    }
}