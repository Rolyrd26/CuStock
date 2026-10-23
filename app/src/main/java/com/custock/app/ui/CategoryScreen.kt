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
import com.custock.app.Producto
import com.custock.app.database.ProductDatabase
import com.custock.app.dinero
import com.custock.app.dp
import com.custock.app.fondoRedondeado

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
            setPadding(context.dp(18), context.dp(10), context.dp(18), context.dp(8))
        }

        val busqueda = EditText(context).apply {
            hint = "Buscar por nombre o referencia"
            setSingleLine(true)
        }
        raiz.addView(busqueda)

        val lista = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val scroll = ScrollView(context).apply {
            addView(lista)
        }

        raiz.addView(scroll, LinearLayout.LayoutParams(-1, context.dp(330)))

        fun actualizarLista(termino: String = "") {
            lista.removeAllViews()
            val productos = db.listar(categoria, termino)

            if (productos.isEmpty()) {
                lista.addView(TextView(context).apply {
                    text = if (termino.isBlank()) "Todavía no hay productos en esta categoría."
                    else "No se encontraron productos."
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
                    setOnClickListener {
                        ProductFormDialog(context, categoria, producto) { _, _ ->
                            actualizarLista(busqueda.text.toString())
                        }.show()
                    }
                })

                tarjeta.addView(Button(context).apply {
                    text = "Eliminar producto"
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

        val dialog = AlertDialog.Builder(context)
            .setTitle("${icono(categoria)} $categoria")
            .setView(raiz)
            .setPositiveButton("Añadir producto", null)
            .setNegativeButton("Cerrar", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                ProductFormDialog(context, categoria, null) { _, _ ->
                    actualizarLista(busqueda.text.toString())
                }.show()
            }
        }

        dialog.show()
        actualizarLista()

        return raiz
    }
}