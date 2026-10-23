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
    private val prefs: SharedPreferences
) {
    fun buildView(): LinearLayout {
        val raiz = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(context.dp(18), context.dp(20), context.dp(18), context.dp(20))
        }

        val btnTema = Button(context).apply {
            text = "Activar tema oscuro"
            setOnClickListener {
                val editor = prefs.edit()
                val actual = prefs.getBoolean(AppConstants.KEY_TEMA_OSCURO, false)
                editor.putBoolean(AppConstants.KEY_TEMA_OSCURO, !actual)
                editor.apply()
            }
        }

        val btnCopia = Button(context).apply {
            text = "Copias de seguridad"
            setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle("Copias de seguridad")
                    .setMessage("Funcionalidad pendiente de mover a backup manager.")
                    .setPositiveButton("Aceptar", null)
                    .show()
            }
        }

        raiz.addView(TextView(context).apply {
            text = "Ajustes"
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.rgb(24, 65, 120))
        })

        raiz.addView(btnTema)
        raiz.addView(btnCopia)

        return raiz
    }
}