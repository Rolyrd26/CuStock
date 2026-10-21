
package com.custock.app

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {

    private val prefs by lazy {
        getSharedPreferences("CuStock", MODE_PRIVATE)
    }

    private lateinit var baseDatos: ProductDatabase
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

        baseDatos = ProductDatabase(this)

        val guardadas = prefs.getString("categorias", null)
        if (guardadas == null) {
            categorias.addAll(predeterminadas)
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
        }

        guardarCategorias()
        construirPantalla()
    }

    private fun guardarCategorias() {
        val json = org.json.JSONArray()
        categorias.forEach { json.put(it) }
        prefs.edit().putString("categorias", json.toString()).apply()
    }

    private fun dp(n: Int): Int =
        (n * resources.displayMetrics.density).toInt()

    private fun fondoRedondeado(color: Int) =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(16).toFloat()
        }

    private fun construirPantalla() {
        val raiz = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(fondo)
        }

        val cabecera = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(25), dp(22), dp(22))
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
            text = "Selecciona una categoría para gestionar sus productos"
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(0, dp(6), 0, dp(15))
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
                background = fondoRedondeado(Color.WHITE)
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
                background = fondoRedondeado(
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
            .setView(campo)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Crear") { _, _ ->
                val nombre = campo.text.toString().trim()

                if (nombre.isBlank()) {
                    aviso("Escribe un nombre")
                } else if (categorias.any {
                        it.equals(nombre, true)
                    }) {
                    aviso("La categoría ya existe")
                } else {
                    categorias.add(nombre)
                    guardarCategorias()
                    construirPantalla()
                    aviso("Categoría creada")
                }
            }
            .show()
    }

    private fun abrirCategoria(categoria: String) {
        val raiz = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(10), dp(18), dp(8))
        }

        val busqueda = EditText(this).apply {
            hint = "Buscar por nombre o referencia"
            setSingleLine(true)
        }
        raiz.addView(busqueda)

        val lista = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val desplazable = ScrollView(this)
        desplazable.addView(lista)

        raiz.addView(
            desplazable,
            LinearLayout.LayoutParams(-1, dp(330))
        )

        fun actualizarLista(termino: String = "") {
            lista.removeAllViews()

            val productos = baseDatos.listar(categoria, termino)

            if (productos.isEmpty()) {
                lista.addView(TextView(this).apply {
                    text = if (termino.isBlank()) {
                        "Todavía no hay productos en esta categoría."
                    } else {
                        "No se encontraron productos."
                    }
                    textSize = 15f
                    setPadding(0, dp(15), 0, dp(15))
                })
            }

            productos.forEach { producto ->
                val tarjeta = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(12), dp(10), dp(12), dp(10))
                    background = fondoRedondeado(Color.rgb(240, 245, 252))
                }

                tarjeta.addView(TextView(this).apply {
                    text = producto.nombre
                    textSize = 18f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(azul)
                })

                tarjeta.addView(TextView(this).apply {
                    text = "Existencias: ${producto.cantidad}\n" +
                        "Coste: ${dinero(producto.coste)} CUP\n" +
                        "Venta: ${dinero(producto.venta)} CUP\n" +
                        "Referencia: ${producto.referencia.ifBlank { "—" }}"
                    textSize = 14f
                    setTextColor(Color.DKGRAY)
                })

                tarjeta.addView(Button(this).apply {
                    text = "Editar producto"
                    setOnClickListener {
                        formularioProducto(categoria, producto) {
                            actualizarLista(busqueda.text.toString())
                        }
                    }
                })

                tarjeta.addView(Button(this).apply {
                    text = "Eliminar producto"
                    setOnClickListener {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("Eliminar producto")
                            .setMessage("¿Eliminar ${producto.nombre}?")
                            .setNegativeButton("Cancelar", null)
                            .setPositiveButton("Eliminar") { _, _ ->
                                baseDatos.eliminar(producto.id)
                                actualizarLista(busqueda.text.toString())
                                aviso("Producto eliminado")
                            }
                            .show()
                    }
                })

                lista.addView(tarjeta)
                lista.addView(TextView(this).apply {
                    text = ""
                    height = dp(8)
                })
            }
        }

        busqueda.addTextChangedListener(
            object : android.text.TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?, start: Int, before: Int, count: Int
                ) {
                    actualizarLista(s?.toString().orEmpty())
                }

                override fun afterTextChanged(
                    s: android.text.Editable?
                ) {}
            }
        )

        AlertDialog.Builder(this)
            .setTitle("${icono(categoria)} $categoria")
            .setView(raiz)
            .setPositiveButton("Añadir producto", null)
            .setNegativeButton("Cerrar", null)
            .create()
            .also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener {
                            formularioProducto(categoria, null) {
                                actualizarLista(busqueda.text.toString())
                            }
                        }
                }
                dialog.show()
                actualizarLista()
            }
    }

    private fun formularioProducto(
        categoria: String,
        existente: Producto?,
        alGuardar: () -> Unit
    ) {
        val contenedor = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(5), dp(20), dp(5))
        }

        fun campo(texto: String, valor: String = ""): EditText {
            val edit = EditText(this).apply {
                hint = texto
                setSingleLine(true)
                setText(valor)
            }
            contenedor.addView(edit)
            return edit
        }

        val nombre = campo("Nombre del producto", existente?.nombre.orEmpty())
        val cantidad = campo("Existencias", existente?.cantidad?.toString() ?: "0")
        cantidad.inputType = InputType.TYPE_CLASS_NUMBER

        val coste = campo("Precio de coste en CUP", existente?.coste?.toString() ?: "0")
        coste.inputType = InputType.TYPE_CLASS_NUMBER or
            InputType.TYPE_NUMBER_FLAG_DECIMAL

        val venta = campo("Precio de venta en CUP", existente?.venta?.toString() ?: "0")
        venta.inputType = InputType.TYPE_CLASS_NUMBER or
            InputType.TYPE_NUMBER_FLAG_DECIMAL

        val referencia = campo("Referencia (opcional)", existente?.referencia.orEmpty())
        val descripcion = campo("Descripción (opcional)", existente?.descripcion.orEmpty())

        AlertDialog.Builder(this)
            .setTitle(if (existente == null) "Nuevo producto" else "Editar producto")
            .setView(contenedor)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar", null)
            .create()
            .also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener {
                            val n = nombre.text.toString().trim()
                            val q = cantidad.text.toString().toIntOrNull()
                            val c = coste.text.toString().replace(',', '.').toDoubleOrNull()
                            val v = venta.text.toString().replace(',', '.').toDoubleOrNull()

                            if (n.isBlank() || q == null || q < 0 ||
                                c == null || c < 0 || v == null || v < 0
                            ) {
                                aviso("Revisa el nombre, las existencias y los precios.")
                                return@setOnClickListener
                            }

                            val producto = Producto(
                                id = existente?.id ?: 0L,
                                nombre = n,
                                categoria = categoria,
                                cantidad = q,
                                coste = c,
                                venta = v,
                                referencia = referencia.text.toString().trim(),
                                descripcion = descripcion.text.toString().trim()
                            )

                            try {
                                val resultado = baseDatos.guardar(producto)
                                if (resultado > 0) {
                                    dialog.dismiss()
                                    alGuardar()
                                    aviso("Producto guardado")
                                } else {
                                    aviso("No se pudo guardar el producto.")
                                }
                            } catch (e: Exception) {
                                aviso("Error al guardar: ${e.localizedMessage ?: "desconocido"}")
                            }
                        }
                }
                dialog.show()
            }
    }

    private fun dinero(valor: Double): String =
        String.format(java.util.Locale("es", "CU"), "%,.2f", valor)

    private fun aviso(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        if (::baseDatos.isInitialized) baseDatos.close()
        super.onDestroy()
    }
}
