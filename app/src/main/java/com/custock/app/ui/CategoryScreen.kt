package com.custock.app.ui

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.custock.app.Producto
import com.custock.app.database.ProductDatabase
import com.custock.app.dinero
import com.custock.app.dp
import com.custock.app.fondoRedondeado
import com.custock.app.applyCuStockStyle

class CategoryScreen(
    private val context: Context,
    private val db: ProductDatabase,
    private val categoria: String,
    private val onBack: () -> Unit
) {

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
            text = "${icono(categoria)} $categoria"
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        })

        textos.addView(TextView(context).apply {
            text = "Inventario y productos"
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

        val busqueda = EditText(context).apply {
            hint = "Buscar por nombre o referencia"
            setSingleLine(true)
            setPadding(context.dp(12), context.dp(10), context.dp(12), context.dp(10))
            background = context.fondoRedondeado(Color.WHITE)
        }

        contenido.addView(busqueda)

        val lista = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val scroll = ScrollView(context).apply {
            addView(lista)
        }

        contenido.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        fun actualizarLista(termino: String = "") {
            lista.removeAllViews()
            val productos = db.listar(categoria, termino)

            if (productos.isEmpty()) {
                lista.addView(TextView(context).apply {
                    text = if (termino.isBlank()) {
                        "Todavía no hay productos en esta categoría."
                    } else {
                        "No se encontraron productos."
                    }
                    textSize = 15f
                    setPadding(0, context.dp(15), 0, context.dp(15))
                })
                return
            }

            productos.forEach { producto ->
                val tarjeta = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(context.dp(12), context.dp(10), context.dp(12), context.dp(10))
                    background = context.fondoRedondeado(Color.rgb(240, 245, 252))
                }

                tarjeta.addView(TextView(context).apply {
                    text = producto.nombre
                    textSize = 18f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(Color.rgb(24, 65, 120))
                })

                tarjeta.addView(TextView(context).apply {
                    text = "Existencias: ${producto.cantidad}\n" +
                        "Coste: ${dinero(producto.coste)} ${producto.moneda}\n" +
                        "Venta: ${dinero(producto.venta)} ${producto.moneda}\n" +
                        "Referencia: ${producto.referencia.ifBlank { "—" }}\n" +
                        "Descripción: ${producto.descripcion.ifBlank { "—" }}"
                    textSize = 14f
                    setTextColor(Color.DKGRAY)
                })

                tarjeta.addView(Button(context).apply {
                    text = "Editar producto"
                    applyCuStockStyle(primary = false)
                    setOnClickListener {
                        ProductFormDialog(
                            context = context,
                            categoria = categoria,
                            existente = producto,
                            onSave = { _, _ ->
                                actualizarLista(busqueda.text.toString())
                            }
                        ).show()
                    }
                })

                tarjeta.addView(Button(context).apply {
                    text = "Eliminar producto"
                    background = android.graphics.drawable.GradientDrawable().apply {
    cornerRadius = context.dp(14).toFloat()
    setColor(Color.rgb(255, 235, 238))
}
setTextColor(Color.rgb(183, 28, 28))
textSize = 15f
isAllCaps = false
                    setOnClickListener {
                        AlertDialog.Builder(context)
                            .setTitle("Eliminar producto")
                            .setMessage("¿Eliminar ${producto.nombre}?")
                            .setNegativeButton("Cancelar", null)
                            .setPositiveButton("Eliminar") { _, _ ->
                                db.eliminar(producto.id)
                                actualizarLista(busqueda.text.toString())
                            }
                            .show()
                    }
                })

                lista.addView(tarjeta)
                lista.addView(TextView(context).apply { height = context.dp(8) })
            }
        }

        busqueda.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                actualizarLista(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        
        val btnAgregarProducto = Button(context).apply {
            text = "＋ Añadir producto"
            applyCuStockStyle(primary = true)
            setOnClickListener {
                ProductFormDialog(
                    context = context,
                    categoria = categoria,
                    existente = null,
                    onSave = { _, _ ->
                        actualizarLista(busqueda.text.toString())
                    }
                ).show()
            }
        }

        contenido.addView(btnAgregarProducto)
        raiz.addView(contenido)
        actualizarLista()

        return raiz

    }
}
