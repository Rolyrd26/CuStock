package com.custock.app

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*

class MainActivity : Activity() {
    private val prefs by lazy {
        getSharedPreferences("CuStock", MODE_PRIVATE)
    }

    private val categorias = mutableListOf<String>()
    private val predeterminadas = listOf(
        "Ropa", "Alimentos", "Lencería", "Calzado",
        "Higiene", "Bebidas", "Electrodomésticos",
        "Accesorios", "Otros"
    )

    private val iconos = mapOf(
        "ropa" to "👕",
        "alimentos" to "🥫",
        "lencería" to "🛏️",
        "lenceria" to "🛏️",
        "calzado" to "👟",
        "higiene" to "🧴",
        "bebidas" to "🥤",
        "electrodomésticos" to "🧊",
        "electrodomesticos" to "🧊",
        "accesorios" to "👜",
        "otros" to "📦"
    )

    private val azul = Color.rgb(24, 65, 120)
    private val fondo = Color.rgb(245, 248, 253)

    private fun icono(nombre: String): String =
        iconos[nombre.trim().lowercase()] ?: "📦"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val guardadas = prefs.getString("categorias", null)
        if (guardadas == null) {
            categorias.addAll(predeterminadas)
            guardar()
        } else {
            val json = org.json.JSONArray(guardadas)
            for (i in 0 until json.length()) {
                categorias.add(json.getString(i))
            }
            predeterminadas.forEach {
                if (categorias.none { c -> c.equals(it, true) }) {
                    categorias.add(it)
                }
            }
            guardar()
        }
        construirPantalla()
    }

    private fun guardar() {
        val json = org.json.JSONArray()
        categorias.forEach { json.put(it) }
        prefs.edit().putString("categorias", json.toString()).apply()
    }

    private fun dp(n: Int) =
        (n * resources.displayMetrics.density).toInt()

    private fun tarjeta(fondo: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(fondo)
            cornerRadius = dp(18).toFloat()
        }

    private fun construirPantalla() {
        val raiz = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(fondo)
        }

        val cabecera = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(28), dp(22), dp(24))
            setBackgroundColor(azul)
        }

        cabecera.addView(TextView(this).apply {
            text = "CuStock"
            textSize = 30f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        })

        cabecera.addView(TextView(this).apply {
            text = "Tu inventario, siempre contigo"
            textSize = 15f
            setTextColor(Color.WHITE)
        })

        raiz.addView(cabecera)

        val desplazable = ScrollView(this)
        val contenido = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(18), dp(16), dp(24))
        }

        val filaTitulo = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
        }

        filaTitulo.addView(TextView(this).apply {
            text = "Categorías"
            textSize = 25f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(azul)
        }, LinearLayout.LayoutParams(0, -2, 1f))

        filaTitulo.addView(Button(this).apply {
            text = "+ Añadir"
            setOnClickListener { nuevaCategoria() }
        })

        contenido.addView(filaTitulo)

        contenido.addView(TextView(this).apply {
            text = "Organiza tus productos por categoría"
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(0, dp(5), 0, dp(15))
        })

        val cuadrícula = GridLayout(this).apply {
            columnCount = 2
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
            val tarjeta = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(8), dp(14), dp(8), dp(14))
                background = tarjeta(Color.WHITE)
                elevation = dp(2).toFloat()
                isClickable = true
                isFocusable = true
                setOnClickListener { abrirCategoria(nombre) }
            }

            tarjeta.addView(TextView(this).apply {
                text = icono(nombre)
                textSize = 38f
                gravity = Gravity.CENTER
                setPadding(dp(10), dp(7), dp(10), dp(7))
                background = this@MainActivity.tarjeta(
                    colores[indice % colores.size]
                )
            })

            tarjeta.addView(TextView(this).apply {
                text = nombre
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setTextColor(Color.rgb(35, 48, 70))
                setPadding(0, dp(10), 0, 0)
            })

            val parametros = GridLayout.LayoutParams().apply {
                width = 0
                height = dp(135)
                columnSpec = GridLayout.spec(
                    GridLayout.UNDEFINED, 1f
                )
                setMargins(dp(5), dp(5), dp(5), dp(7))
            }

            cuadrícula.addView(tarjeta, parametros)
        }

        contenido.addView(cuadrícula)
        desplazable.addView(contenido)

        raiz.addView(
            desplazable,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        setContentView(raiz)
    }

    private fun nuevaCategoria() {
        val campo = EditText(this).apply {
            hint = "Nombre de la categoría"
            setSingleLine(true)
        }

        AlertDialog.Builder(this)
            .setTitle("Nueva categoría")
            .setMessage("Se asignará un icono genérico si no hay uno específico.")
            .setView(campo)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Crear") { _, _ ->
                val nombre = campo.text.toString().trim()

                if (nombre.isBlank()) {
                    Toast.makeText(
                        this, "Escribe un nombre",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (categorias.any {
                        it.equals(nombre, true)
                    }) {
                    Toast.makeText(
                        this, "La categoría ya existe",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    categorias.add(nombre)
                    guardar()
                    construirPantalla()
                    Toast.makeText(
                        this, "Categoría creada ${icono(nombre)}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .show()
    }

    private fun abrirCategoria(nombre: String) {
        AlertDialog.Builder(this)
            .setTitle("${icono(nombre)}  $nombre")
            .setMessage(
                "Categoría creada correctamente.\n\n" +
                "Aquí mostraremos los productos que pertenezcan " +
                "a esta categoría en el siguiente paso."
            )
            .setPositiveButton("Aceptar", null)
            .show()
    }
}
