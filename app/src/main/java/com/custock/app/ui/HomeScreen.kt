package com.custock.app.ui

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.custock.app.AppConstants
import com.custock.app.dp
import com.custock.app.fondoRedondeado

class HomeScreen(
    private val context: Context,
    private val categorias: MutableList<String>,
    private val onOpenCategory: (String) -> Unit,
    private val onOpenSales: () -> Unit,
    private val onOpenDebts: () -> Unit,
    private val onOpenReports: () -> Unit,
    private val onOpenSettings: () -> Unit,
    private val onAddCategory: (String) -> Unit
) {

    private val azul = Color.rgb(24, 65, 120)
    private val fondo = Color.rgb(245, 248, 253)

    private fun icono(nombre: String): String {
        return when (nombre.trim().lowercase()) {
            "ropa" -> "👕"
            "alimentos" -> "🥫"
            "lencería" -> "🛏️"
            "lenceria" -> "🛏️"
            "calzado" -> "👟"
            "higiene" -> "🧴"
            "bebidas" -> "🥤"
            "electrodomésticos" -> "🔌"
            "electrodomesticos" -> "🔌"
            "accesorios" -> "👜"
            else -> "📦"
        }
    }

    fun buildView(): LinearLayout {
        val raiz = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(fondo)
        }

        val cabecera = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(context.dp(24), context.dp(34), context.dp(16), context.dp(28))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
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
            text = "📦  ${AppConstants.APP_NAME}"
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        })

        textos.addView(TextView(context).apply {
            text = "Tu inventario, siempre contigo"
            textSize = 14f
            setTextColor(Color.rgb(230, 238, 250))
            setPadding(0, context.dp(5), 0, 0)
        })

        fila.addView(textos, LinearLayout.LayoutParams(0, -2, 1f))

        fila.addView(TextView(context).apply {
            text = "☰"
            textSize = 28f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            isClickable = true
            isFocusable = true
            setPadding(context.dp(12), context.dp(8), context.dp(8), context.dp(8))
            setOnClickListener {
                mostrarMenu()
            }
        })

        cabecera.addView(fila)
        raiz.addView(cabecera)

        val desplazable = ScrollView(context)
        val contenido = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(context.dp(16), context.dp(16), context.dp(16), context.dp(24))
        }

        contenido.addView(TextView(context).apply {
            text = "Gestión del negocio"
            textSize = 23f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(azul)
        })

        val filaTitulo = LinearLayout(context).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, context.dp(16), 0, context.dp(4))
        }

        filaTitulo.addView(TextView(context).apply {
            text = "Categorías e inventario"
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(azul)
        }, LinearLayout.LayoutParams(0, -2, 1f))

        filaTitulo.addView(Button(context).apply {
            text = "＋ Añadir"
            isAllCaps = false
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                setColor(azul)
                cornerRadius = context.dp(12).toFloat()
            }
            setPadding(context.dp(12), context.dp(6), context.dp(12), context.dp(6))
            minHeight = context.dp(44)
            elevation = context.dp(2).toFloat()
            setOnClickListener { mostrarDialogoNuevaCategoria() }
        })

        contenido.addView(filaTitulo)

        contenido.addView(TextView(context).apply {
            text = "Selecciona una categoría para gestionar sus productos."
            textSize = 14f
            setTextColor(Color.rgb(90, 100, 115))
            setPadding(0, context.dp(5), 0, context.dp(12))
        })

        val grid = GridLayout(context).apply {
            columnCount = 2
            useDefaultMargins = false
        }

        val colores = listOf(
            0xFFDCEBFF.toInt(),
            0xFFFFE7CC.toInt(),
            0xFFFFDDE9.toInt(),
            0xFFE7DEFF.toInt(),
            0xFFD7F5EF.toInt(),
            0xFFFFDFDF.toInt()
        )

        categorias.forEachIndexed { indice, nombre ->
            val tarjeta = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(context.dp(10), context.dp(16), context.dp(10), context.dp(16))
                background = context.fondoRedondeado(Color.WHITE)
                elevation = context.dp(3).toFloat()
                isClickable = true
                isFocusable = true
                setOnClickListener { onOpenCategory(nombre) }
            }

            tarjeta.addView(TextView(context).apply {
                text = icono(nombre)
                textSize = 36f
                gravity = Gravity.CENTER
                setPadding(context.dp(12), context.dp(10), context.dp(12), context.dp(10))
                background = context.fondoRedondeado(colores[indice % colores.size])
            })

            tarjeta.addView(TextView(context).apply {
                text = nombre
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setTextColor(Color.rgb(35, 48, 70))
                maxLines = 2
                setPadding(0, context.dp(12), 0, 0)
            })

            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = context.dp(145)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(context.dp(6), context.dp(6), context.dp(6), context.dp(6))
            }

            grid.addView(tarjeta, params)
        }

        contenido.addView(grid)
        desplazable.addView(contenido)
        raiz.addView(desplazable, LinearLayout.LayoutParams(-1, 0, 1f))

        return raiz
    }

    private fun mostrarDialogoNuevaCategoria() {
        val campo = EditText(context).apply {
            hint = "Nombre de la categoría"
            setSingleLine(true)
        }

        AlertDialog.Builder(context)
            .setTitle("Nueva categoría")
            .setView(campo)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Crear") { _, _ ->
                val nombre = campo.text.toString().trim()
                when {
                    nombre.isBlank() -> {
                        // mostrar aviso si quieres
                    }
                    categorias.any { it.equals(nombre, true) } -> {
                        // mostrar aviso si quieres
                    }
                    else -> {
                        categorias.add(nombre)
                        onAddCategory(nombre)
                    }
                }
            }
            .show()
    }

    private fun mostrarMenu() {
        val opciones = listOf(
            "🛒 Nueva venta" to onOpenSales,
            "📒 Control de fiados" to onOpenDebts,
            "📜 Historial" to onOpenReports,
            "⚙️ Ajustes" to onOpenSettings
        )

        val panel = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(context.dp(22), context.dp(12), context.dp(22), context.dp(24))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadii = floatArrayOf(
                    context.dp(24).toFloat(), context.dp(24).toFloat(),
                    context.dp(24).toFloat(), context.dp(24).toFloat(),
                    0f, 0f, 0f, 0f
                )
            }
        }

        opciones.forEach { (titulo, accion) ->
            val item = Button(context).apply {
                text = titulo
                isAllCaps = false
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(azul)
                background = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = context.dp(12).toFloat()
                    setStroke(context.dp(1), Color.rgb(210, 220, 235))
                }
                setPadding(context.dp(18), context.dp(14), context.dp(18), context.dp(14))
                setOnClickListener { accion() }
            }
            panel.addView(item)
        }

        AlertDialog.Builder(context)
            .setTitle("Menú de CuStock")
            .setView(panel)
            .show()
    }
}