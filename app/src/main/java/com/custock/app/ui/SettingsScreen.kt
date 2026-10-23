package com.custock.app.ui

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.custock.app.AppConstants
import com.custock.app.applyCuStockStyle
import com.custock.app.database.BackupManager
import com.custock.app.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

        val backupManager = BackupManager(context)

        val btnTema = Button(context).apply {
    val oscuro = prefs.getBoolean(AppConstants.KEY_TEMA_OSCURO, false)
    text = if (oscuro) "Activar tema claro" else "Activar tema oscuro"

    setOnClickListener {
        val nuevoTema = !prefs.getBoolean(AppConstants.KEY_TEMA_OSCURO, false)
        prefs.edit().putBoolean(AppConstants.KEY_TEMA_OSCURO, nuevoTema).apply()

        Toast.makeText(
            context,
            "Tema guardado. Se aplicará al volver a abrir CuStock.",
            Toast.LENGTH_LONG
        ).show()

        onBack()
    }
        }

        val btnCopia = Button(context).apply {
            text = "Crear copia de seguridad"
            setOnClickListener {
                try {
                    val fecha = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val archivo = backupManager.createBackupFile("custock_backup_$fecha.db")
                    backupManager.exportDatabase(archivo)
                    Toast.makeText(context, "Copia creada en: ${archivo.absolutePath}", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al crear la copia: ${e.message ?: "desconocido"}", Toast.LENGTH_LONG).show()
                }
            }
        }

        val btnRestaurar = Button(context).apply {
            text = "Restaurar copia de seguridad"
            setOnClickListener {
                try {
                    val dir = backupManager.getBackupDir()
                    val archivos = dir.listFiles()?.filter { it.isFile && it.name.endsWith(".db") } ?: emptyList()
                    if (archivos.isEmpty()) {
                        Toast.makeText(context, "No hay copias de seguridad disponibles.", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }

                    val archivo = archivos.last()
                    backupManager.restoreDatabase(archivo)
                    Toast.makeText(context, "Base de datos restaurada desde: ${archivo.absolutePath}", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error al restaurar: ${e.message ?: "desconocido"}", Toast.LENGTH_LONG).show()
                }
            }
        }

        btnTema.applyCuStockStyle(primary = true)
btnCopia.applyCuStockStyle(primary = false)
btnRestaurar.applyCuStockStyle(primary = false)

contenido.addView(btnTema)
contenido.addView(btnCopia)
contenido.addView(btnRestaurar)

        raiz.addView(contenido)
        return raiz
    }
}
