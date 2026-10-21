package com.custock.app

import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.app.Activity
import android.app.AlertDialog
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.*
import java.io.OutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("CuStock", MODE_PRIVATE) }
    private lateinit var baseDatos: ProductDatabase
    private val categorias = mutableListOf<String>()

    private val predeterminadas = listOf(
        "Ropa", "Alimentos", "Lencería", "Calzado", "Higiene",
        "Bebidas", "Electrodomésticos", "Accesorios", "Otros"
    )

    private val iconos = mapOf(
        "ropa" to "👕", "alimentos" to "🥫", "lencería" to "🛏️",
        "lenceria" to "🛏️", "calzado" to "👟", "higiene" to "🧴",
        "bebidas" to "🥤", "electrodomésticos" to "🧊",
        "electrodomesticos" to "🧊", "accesorios" to "👜", "otros" to "📦"
    )
    private var temaOscuro = false

    private val azul: Int
        get() = if (temaOscuro) Color.rgb(35, 45, 60) else Color.rgb(24, 65, 120)

    private val fondo: Int
        get() = if (temaOscuro) Color.rgb(25, 30, 38) else Color.rgb(245, 248, 253)
    private val CODIGO_CARPETA_INFORMES = 3001
    private val CLAVE_CARPETA_INFORMES = "carpeta_informes_uri"
    private val CODIGO_CREAR_COPIA = 3002
private val CODIGO_RESTAURAR_COPIA = 3003
private val NOMBRE_BASE_DATOS = "custock.db"

    private fun icono(nombre: String) =
        iconos[nombre.trim().lowercase(Locale.ROOT)] ?: "📦"

    private fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()

    private fun dinero(valor: Double) =
        String.format(Locale("es", "CU"), "%,.2f", valor)

    private fun fechaHora(ms: Long) =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(ms))

    private fun aviso(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }

    private fun fondoRedondeado(color: Int) =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(16).toFloat()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        baseDatos = ProductDatabase(this)
        temaOscuro = prefs.getBoolean("tema_oscuro", false)
        
        val guardadas = prefs.getString("categorias", null)
        if (guardadas == null) {
            categorias.addAll(predeterminadas)
        } else {
            val json = org.json.JSONArray(guardadas)
            for (i in 0 until json.length()) categorias.add(json.getString(i))
            predeterminadas.forEach { nombre ->
                if (categorias.none { it.equals(nombre, true) }) categorias.add(nombre)
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

    private fun nuevaRaiz(): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(fondo)
        }

    private fun agregarCabecera(raiz: LinearLayout, titulo: String, subtitulo: String) {
        val cabecera = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(22), dp(22), dp(20))
            setBackgroundColor(azul)
        }
        cabecera.addView(TextView(this).apply {
            text = titulo
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        })
        cabecera.addView(TextView(this).apply {
            text = subtitulo
            textSize = 14f
            setTextColor(Color.WHITE)
        })
        raiz.addView(cabecera)
    }

    
    private fun agregarBoton(contenedor: LinearLayout, texto: String, accion: () -> Unit) {
        val boton = Button(this).apply {
            text = texto
            isAllCaps = false
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER_VERTICAL
            setTextColor(if (temaOscuro) Color.WHITE else azul)
            background = GradientDrawable().apply {
                setColor(if (temaOscuro) Color.rgb(42, 50, 63) else Color.WHITE)
                cornerRadius = dp(14).toFloat()
                setStroke(
                    dp(1),
                    if (temaOscuro) Color.rgb(75, 95, 120)
                    else Color.rgb(210, 220, 235)
                )
            }
            setPadding(dp(18), dp(14), dp(18), dp(14))
            minHeight = dp(58)
            elevation = dp(2).toFloat()
            setOnClickListener { accion() }
        }

        val parametros = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = dp(10)
        }

        contenedor.addView(boton, parametros)
    }


    private fun construirPantalla() {
        val raiz = nuevaRaiz()
        agregarCabecera(raiz, "CuStock", "Tu inventario, siempre contigo")
        val desplazable = ScrollView(this)
        val contenido = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(24))
        }
        contenido.addView(TextView(this).apply {
            text = "Gestión del negocio"
            textSize = 23f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(azul)
        })
        agregarBoton(contenido, "🛒  Nueva venta") { abrirVentas() }
        agregarBoton(contenido, "📒  Control de fiados") { abrirFiados() }
        agregarBoton(contenido, "📊  Informes y ganancias") { abrirInformes() } 
        agregarBoton(contenido, "⚙️  Ajustes") { abrirAjustes() }

        val filaTitulo = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(16), 0, dp(4))
        }
        filaTitulo.addView(TextView(this).apply {
            text = "Categorías e inventario"
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(azul)
        }, LinearLayout.LayoutParams(0, -2, 1f))
        
        filaTitulo.addView(Button(this).apply {
            text = "＋ Añadir"
            isAllCaps = false
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                setColor(azul)
                cornerRadius = dp(12).toFloat()
            }
            setPadding(dp(12), dp(6), dp(12), dp(6))
            minHeight = dp(44)
            elevation = dp(2).toFloat()
            setOnClickListener { nuevaCategoria() }
        })

        contenido.addView(filaTitulo)
        contenido.addView(TextView(this).apply {
            text = "Selecciona una categoría para gestionar sus productos."
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(0, dp(5), 0, dp(12))
        })

        val grid = GridLayout(this).apply { columnCount = 2 }
        val colores = listOf(
            0xFFDCEBFF.toInt(), 0xFFFFE7CC.toInt(), 0xFFFFDDE9.toInt(),
            0xFFE7DEFF.toInt(), 0xFFD7F5EF.toInt(), 0xFFFFDFDF.toInt()
        )
        categorias.forEachIndexed { indice, nombre ->
            val tarjeta = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(8), dp(14), dp(8), dp(14))
                                background = fondoRedondeado(
                    if (temaOscuro) Color.rgb(42, 49, 60) else Color.WHITE
                )
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
                background = fondoRedondeado(colores[indice % colores.size])
            })
            tarjeta.addView(TextView(this).apply {
                text = nombre
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                                setTextColor(if (temaOscuro) Color.WHITE else Color.rgb(35, 48, 70))
                setPadding(0, dp(10), 0, 0)
            })
            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = dp(135)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(dp(5), dp(5), dp(5), dp(7))
            }
            grid.addView(tarjeta, params)
        }
        contenido.addView(grid)
        desplazable.addView(contenido)
        raiz.addView(desplazable, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(raiz)
    }

        
    private fun abrirAjustes() {
        val raiz = nuevaRaiz()
        agregarCabecera(raiz, "Ajustes", "Personaliza CuStock")

        val contenido = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(20), dp(18), dp(20))
        }

        agregarBoton(contenido, "← Volver al inicio") {
            construirPantalla()
        }

        contenido.addView(TextView(this).apply {
            text = "PREFERENCIAS"
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(azul)
            setPadding(dp(4), dp(20), 0, dp(10))
        })

        agregarBoton(contenido, "💾  Copias de seguridad") {
            abrirCopiasSeguridad()
        }

        agregarBoton(
            contenido,
            if (temaOscuro) "☀️  Activar tema claro"
            else "🌙  Activar tema oscuro"
        ) {
            temaOscuro = !temaOscuro
            prefs.edit().putBoolean("tema_oscuro", temaOscuro).apply()
            abrirAjustes()
            aviso(
                if (temaOscuro) "Tema oscuro activado"
                else "Tema claro activado"
            )
        }

        val desplazable = ScrollView(this).apply {
            addView(contenido)
        }

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
        AlertDialog.Builder(this).setTitle("Nueva categoría").setView(campo)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Crear") { _, _ ->
                val nombre = campo.text.toString().trim()
                when {
                    nombre.isBlank() -> aviso("Escribe un nombre")
                    categorias.any { it.equals(nombre, true) } -> aviso("La categoría ya existe")
                    else -> {
                        categorias.add(nombre)
                        guardarCategorias()
                        construirPantalla()
                        aviso("Categoría creada")
                    }
                }
            }.show()
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
        val lista = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(lista) }
        raiz.addView(scroll, LinearLayout.LayoutParams(-1, dp(330)))

        fun actualizarLista(termino: String = "") {
            lista.removeAllViews()
            val productos = baseDatos.listar(categoria, termino)
            if (productos.isEmpty()) {
                lista.addView(TextView(this).apply {
                    text = if (termino.isBlank()) "Todavía no hay productos en esta categoría."
                    else "No se encontraron productos."
                    textSize = 15f
                    setPadding(0, dp(15), 0, dp(15))
                })
            }
            productos.forEach { producto ->
                val tarjeta = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(12), dp(10), dp(12), dp(10))
                    background = fondoRedondeado(
    if (temaOscuro) Color.rgb(42, 49, 60) else Color.rgb(240, 245, 252)
)
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
                        "Referencia: ${producto.referencia.ifBlank { "—" }}\n" +
                        "Descripción: ${producto.descripcion.ifBlank { "—" }}"
                    textSize = 14f
                    setTextColor(if (temaOscuro) Color.WHITE else Color.DKGRAY)
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
                        AlertDialog.Builder(this@MainActivity).setTitle("Eliminar producto")
                            .setMessage("¿Eliminar ${producto.nombre}?")
                            .setNegativeButton("Cancelar", null)
                            .setPositiveButton("Eliminar") { _, _ ->
                                baseDatos.eliminar(producto.id)
                                actualizarLista(busqueda.text.toString())
                                aviso("Producto eliminado")
                            }.show()
                    }
                })
                lista.addView(tarjeta)
                lista.addView(TextView(this).apply { height = dp(8) })
            }
        }

        busqueda.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                actualizarLista(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        val dialog = AlertDialog.Builder(this).setTitle("${icono(categoria)} $categoria")
            .setView(raiz).setPositiveButton("Añadir producto", null)
            .setNegativeButton("Cerrar", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                formularioProducto(categoria, null) {
                    actualizarLista(busqueda.text.toString())
                }
            }
        }
        dialog.show()
        actualizarLista()
    }

    private fun formularioProducto(categoria: String, existente: Producto?, alGuardar: () -> Unit) {
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
        coste.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        val venta = campo("Precio de venta en CUP", existente?.venta?.toString() ?: "0")
        venta.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        val referencia = campo("Referencia (opcional)", existente?.referencia.orEmpty())
        val descripcion = campo("Descripción (opcional)", existente?.descripcion.orEmpty())

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (existente == null) "Nuevo producto" else "Editar producto")
            .setView(contenedor).setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val n = nombre.text.toString().trim()
                val q = cantidad.text.toString().toIntOrNull()
                val c = coste.text.toString().replace(',', '.').toDoubleOrNull()
                val v = venta.text.toString().replace(',', '.').toDoubleOrNull()
                if (n.isBlank() || q == null || q < 0 || c == null || c < 0 ||
                    !c.isFinite() || v == null || v < 0 || !v.isFinite()) {
                    aviso("Revisa el nombre, las existencias y los precios.")
                    return@setOnClickListener
                }
                try {
                    val resultado = baseDatos.guardar(
                        Producto(
                            id = existente?.id ?: 0L, nombre = n, categoria = categoria,
                            cantidad = q, coste = c, venta = v,
                            referencia = referencia.text.toString().trim(),
                            descripcion = descripcion.text.toString().trim()
                        )
                    )
                    if (resultado > 0) {
                        dialog.dismiss()
                        alGuardar()
                        aviso("Producto guardado")
                    } else aviso("No se pudo guardar el producto.")
                } catch (e: Exception) {
                    aviso("Error al guardar: " + (e.localizedMessage ?: "desconocido"))
                }
            }
        }
        dialog.show()
    }

    private fun todosLosProductos(): List<Producto> =
        categorias.flatMap { baseDatos.listar(it) }
            .distinctBy { it.id }.sortedBy { it.nombre.lowercase(Locale.ROOT) }

    private fun abrirVentas() {
        val carrito = mutableListOf<LineaVenta>()
        val raiz = nuevaRaiz()
        agregarCabecera(raiz, "Nueva venta", "Importes en pesos cubanos (CUP)")
        val contenido = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(10), dp(16), dp(16))
        }
        agregarBoton(contenido, "← Volver al inicio") { construirPantalla() }
        val cliente = EditText(this).apply {
            hint = "Nombre del cliente (obligatorio para fiar)"
            setSingleLine(true)
        }
        contenido.addView(cliente)
        val lista = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(lista) }
        val totalTexto = TextView(this).apply {
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(azul)
            setPadding(0, dp(10), 0, dp(10))
        }
        lateinit var renderizarCarrito: () -> Unit
        lateinit var confirmar: (Boolean) -> Unit

        agregarBoton(contenido, "+ Añadir producto") {
            val disponibles = todosLosProductos().filter { it.cantidad > 0 }
            if (disponibles.isEmpty()) aviso("No hay productos con existencias disponibles.")
            else {
                val opciones = disponibles.map {
                    "${it.nombre} — ${dinero(it.venta)} CUP (stock: ${it.cantidad})"
                }.toTypedArray()
                AlertDialog.Builder(this).setTitle("Selecciona un producto")
                    .setItems(opciones) { _, indice ->
                        val producto = disponibles[indice]
                        val yaEnCarrito = carrito.filter { it.productoId == producto.id }.sumOf { it.cantidad }
                        val restante = producto.cantidad - yaEnCarrito
                        if (restante <= 0) aviso("Ya añadiste todas las existencias disponibles de ${producto.nombre}.")
                        else {
                            val campo = EditText(this).apply {
                                hint = "Cantidad (máximo $restante)"
                                inputType = InputType.TYPE_CLASS_NUMBER
                                setText("1")
                                selectAll()
                            }
                            val dlg = AlertDialog.Builder(this).setTitle(producto.nombre)
                                .setMessage("Disponibles para añadir: $restante").setView(campo)
                                .setNegativeButton("Cancelar", null).setPositiveButton("Añadir", null).create()
                            dlg.setOnShowListener {
                                dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                                    val cantidad = campo.text.toString().toIntOrNull()
                                    if (cantidad == null || cantidad <= 0 || cantidad > restante)
                                        aviso("Introduce una cantidad entre 1 y $restante.")
                                    else {
                                        val idx = carrito.indexOfFirst { it.productoId == producto.id }
                                        if (idx >= 0) carrito[idx] = carrito[idx].copy(cantidad = carrito[idx].cantidad + cantidad)
                                        else carrito.add(LineaVenta(producto.id, cantidad, producto.venta))
                                        dlg.dismiss()
                                        renderizarCarrito()
                                    }
                                }
                            }
                            dlg.show()
                        }
                    }.setNegativeButton("Cancelar", null).show()
            }
        }
        contenido.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        contenido.addView(totalTexto)
        agregarBoton(contenido, "Confirmar venta pagada") { confirmar(false) }
        agregarBoton(contenido, "Registrar venta fiada") { confirmar(true) }
        raiz.addView(contenido, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(raiz)

        renderizarCarrito = {
            lista.removeAllViews()
            var total = 0.0
            if (carrito.isEmpty()) lista.addView(TextView(this).apply {
                text = "Añade productos para comenzar la venta."
                setPadding(0, dp(12), 0, dp(12))
            })
            carrito.toList().forEach { linea ->
                val producto = baseDatos.obtenerProducto(linea.productoId)
                if (producto == null) carrito.remove(linea)
                else {
                    val subtotal = linea.cantidad * linea.precioUnitario
                    total += subtotal
                    val tarjeta = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(dp(10), dp(10), dp(10), dp(10))
                        background = fondoRedondeado(Color.WHITE)
                    }
                    tarjeta.addView(TextView(this).apply {
                        text = producto.nombre
                        textSize = 17f
                        typeface = Typeface.DEFAULT_BOLD
                        setTextColor(azul)
                    })
                    tarjeta.addView(TextView(this).apply {
                        text = "${linea.cantidad} × ${dinero(linea.precioUnitario)} CUP\nSubtotal: ${dinero(subtotal)} CUP"
                        textSize = 14f
                    })
                    tarjeta.addView(Button(this).apply {
                        text = "Quitar del carrito"
                        setOnClickListener {
                            carrito.removeAll { it.productoId == linea.productoId }
                            renderizarCarrito()
                        }
                    })
                    lista.addView(tarjeta)
                    lista.addView(TextView(this).apply { height = dp(6) })
                }
            }
            totalTexto.text = "TOTAL: ${dinero(total)} CUP"
        }

        confirmar = { esFiado ->
            if (carrito.isEmpty()) aviso("Añade al menos un producto.")
            else if (esFiado && cliente.text.toString().isBlank())
                aviso("Escribe el nombre del cliente para registrar el fiado.")
            else AlertDialog.Builder(this)
                .setTitle(if (esFiado) "Confirmar fiado" else "Confirmar venta")
                .setMessage(
                    "Total: ${dinero(carrito.sumOf { it.cantidad * it.precioUnitario })} CUP\n\n" +
                        if (esFiado) "Se creará una deuda independiente para ${cliente.text}."
                        else "Se descontarán las existencias del inventario."
                ).setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar") { _, _ ->
                    try {
                        val resultado = baseDatos.registrarVenta(
                            lineas = carrito.toList(),
                            cliente = cliente.text.toString().trim(),
                            esFiado = esFiado
                        )
                        aviso("Venta registrada. Total: ${dinero(resultado.total)} CUP")
                        construirPantalla()
                    } catch (e: Exception) {
                        aviso("No se pudo registrar la venta: " + (e.localizedMessage ?: "error desconocido"))
                    }
                }.show()
        }
        renderizarCarrito()
    }

    private fun abrirFiados() {
        val raiz = nuevaRaiz()
        agregarCabecera(raiz, "Control de fiados", "Cada deuda se controla por separado")
        val contenido = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(10), dp(16), dp(18))
        }
        agregarBoton(contenido, "← Volver al inicio") { construirPantalla() }
        val busqueda = EditText(this).apply {
            hint = "Buscar cliente"
            setSingleLine(true)
        }
        contenido.addView(busqueda)
        val pendientes = CheckBox(this).apply {
            text = "Mostrar solo fiados pendientes"
            isChecked = true
        }
        contenido.addView(pendientes)
        val lista = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val scroll = ScrollView(this).apply { addView(lista) }
        contenido.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        raiz.addView(contenido, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(raiz)

        fun actualizar() {
            lista.removeAllViews()
            val fiados = baseDatos.listarFiados(busqueda.text.toString().trim(), pendientes.isChecked)
            if (fiados.isEmpty()) lista.addView(TextView(this).apply {
                text = "No hay fiados que coincidan con la búsqueda."
                setPadding(0, dp(15), 0, dp(15))
            })
            fiados.forEach { fiado ->
                val tarjeta = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(12), dp(12), dp(12), dp(12))
                    background = fondoRedondeado(Color.WHITE)
                }
                tarjeta.addView(TextView(this).apply {
                    text = fiado.cliente
                    textSize = 18f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(azul)
                })
                tarjeta.addView(TextView(this).apply {
                    text = "Fiado #${fiado.id} · ${fechaHora(fiado.fecha)}\n" +
                        "Total: ${dinero(fiado.total)} CUP\nPagado: ${dinero(fiado.pagado)} CUP\n" +
                        "Pendiente: ${dinero(fiado.saldo)} CUP"
                    textSize = 14f
                })
                tarjeta.addView(Button(this).apply {
                    text = "Ver detalle y registrar pago"
                    setOnClickListener { detalleFiado(fiado.id) }
                })
                lista.addView(tarjeta)
                lista.addView(TextView(this).apply { height = dp(8) })
            }
        }
        busqueda.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { actualizar() }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        pendientes.setOnCheckedChangeListener { _, _ -> actualizar() }
        actualizar()
    }


    private fun detalleFiado(fiadoId: Long) {
        val fiado = baseDatos.listarFiados("", false)
            .firstOrNull { it.id == fiadoId }

        if (fiado == null) {
            aviso("No se encontró este fiado.")
            return
        }

        val contenido = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(10), dp(18), dp(10))
        }

        contenido.addView(TextView(this).apply {
            text = "Cliente: ${fiado.cliente}\n" +
                "Fiado #${fiado.id}\n" +
                "Fecha: ${fechaHora(fiado.fecha)}\n\n" +
                "Importe original: ${dinero(fiado.total)} CUP\n" +
                "Total pagado: ${dinero(fiado.pagado)} CUP\n" +
                "Saldo pendiente: ${dinero(fiado.saldo)} CUP"
            textSize = 16f
            setTextColor(azul)
        })

        contenido.addView(TextView(this).apply {
            text = "\nProductos del fiado"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
        })

        val productosFiado = baseDatos.productosDelFiado(fiado.ventaId)

        if (productosFiado.isEmpty()) {
            contenido.addView(TextView(this).apply {
                text = "No hay productos registrados en este fiado."
                textSize = 14f
            })
        } else {
            productosFiado.forEach { producto ->
                contenido.addView(TextView(this).apply {
                    text = "${producto.nombre}\n" +
                        "Cantidad: ${producto.cantidad}\n" +
                        "Precio unitario: ${dinero(producto.precioUnitario)} CUP\n" +
                        "Subtotal: ${dinero(producto.subtotal)} CUP"
                    textSize = 15f
                    setPadding(0, dp(6), 0, dp(6))
                })
            }
        }

        contenido.addView(TextView(this).apply {
            text = "\nHistorial de pagos"
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
        })

        val pagos = baseDatos.historialPagos(fiadoId)

        if (pagos.isEmpty()) {
            contenido.addView(TextView(this).apply {
                text = "Todavía no hay pagos registrados."
            })
        } else {
            pagos.forEach { pago ->
                contenido.addView(TextView(this).apply {
                    text = "${fechaHora(pago.fecha)} — " +
                        "${dinero(pago.importe)} CUP"
                    textSize = 14f
                    setPadding(0, dp(4), 0, dp(4))
                })
            }
        }

        val scroll = ScrollView(this).apply {
            addView(contenido)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Detalle del fiado")
            .setView(scroll)
            .setNegativeButton("Cerrar", null)
            .setNeutralButton("Registrar pago", null)
            .create()

        dialog.setOnShowListener {
            val boton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL)
            boton.isEnabled = fiado.saldo > 0.009

            boton.setOnClickListener {
                dialog.dismiss()
                pedirPago(fiadoId)
            }
        }

        dialog.show()
    }


    private fun pedirPago(fiadoId: Long) {
        val fiado = baseDatos.listarFiados("", false).firstOrNull { it.id == fiadoId } ?: return
        val campo = EditText(this).apply {
            hint = "Importe del pago en CUP"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val dialog = AlertDialog.Builder(this).setTitle("Registrar pago")
            .setMessage(
                "Cliente: ${fiado.cliente}\nSaldo pendiente: ${dinero(fiado.saldo)} CUP\n" +
                    "Puedes pagar cualquier cantidad hasta el saldo pendiente."
            ).setView(campo).setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar pago", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val importe = campo.text.toString().replace(',', '.').toDoubleOrNull()
                if (importe == null || !importe.isFinite() || importe <= 0) aviso("Introduce un importe válido.")
                else try {
                    baseDatos.registrarPago(fiadoId, importe)
                    dialog.dismiss()
                    aviso("Pago registrado correctamente.")
                    detalleFiado(fiadoId)
                } catch (e: Exception) {
                    aviso("No se pudo registrar el pago: " + (e.localizedMessage ?: "error desconocido"))
                }
            }
        }
        dialog.show()
    }
// COPIAS DE SEGURIDAD

private fun abrirCopiasSeguridad() {
    val raiz = nuevaRaiz()
    agregarCabecera(
        raiz,
        "Copias de seguridad",
        "Protege los datos de tu negocio"
    )

    val contenido = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(20), dp(15), dp(20), dp(20))
    }

    agregarBoton(contenido, "← Volver al inicio") {
        construirPantalla()
    }

    contenido.addView(TextView(this).apply {
        text = "Puedes guardar una copia de tus productos, " +
            "existencias, ventas, fiados y pagos. " +
            "Guarda el archivo ZIP en un lugar seguro."
        textSize = 16f
        setTextColor(Color.DKGRAY)
        setPadding(0, dp(12), 0, dp(12))
    })

    agregarBoton(contenido, "📦 Crear copia de seguridad") {
        crearCopiaSeguridad()
    }

    agregarBoton(contenido, "♻️ Restaurar una copia") {
        AlertDialog.Builder(this)
            .setTitle("Restaurar copia")
            .setMessage(
                "La restauración sustituirá los datos actuales " +
                "por los incluidos en la copia seleccionada. " +
                "Se recomienda crear primero una copia actual."
            )
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Continuar") { _, _ ->
                seleccionarCopiaParaRestaurar()
            }
            .show()
    }

    raiz.addView(
        contenido,
        LinearLayout.LayoutParams(-1, 0, 1f)
    )

    setContentView(raiz)
}

private fun crearCopiaSeguridad() {
    val nombre = "CuStock_Copia_" +
        SimpleDateFormat(
            "yyyy-MM-dd_HHmm",
            Locale.getDefault()
        ).format(Date()) + ".zip"

    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "application/zip"
        putExtra(Intent.EXTRA_TITLE, nombre)
    }

    startActivityForResult(intent, CODIGO_CREAR_COPIA)
}

private fun seleccionarCopiaParaRestaurar() {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "application/zip"
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    startActivityForResult(intent, CODIGO_RESTAURAR_COPIA)
}
  

private fun crearCopiaDesdeUri(uri: Uri) {
    val archivoBD = getDatabasePath(NOMBRE_BASE_DATOS)

    if (!archivoBD.exists()) {
        aviso("No se encontró la base de datos.")
        return
    }

    try {
        val salida = contentResolver.openOutputStream(uri)
            ?: throw Exception("No se pudo abrir el archivo de destino.")

        salida.use { flujo ->
            baseDatos.close()

            try {
                File(archivoBD.absolutePath + "-wal").delete()
                File(archivoBD.absolutePath + "-shm").delete()

                ZipOutputStream(flujo).use { zip ->
                    zip.putNextEntry(ZipEntry(NOMBRE_BASE_DATOS))

                    FileInputStream(archivoBD).use { entrada ->
                        entrada.copyTo(zip)
                    }

                    zip.closeEntry()

                    zip.putNextEntry(ZipEntry("categorias.json"))

                    val categoriasJson =
                        prefs.getString("categorias", "[]") ?: "[]"

                    zip.write(categoriasJson.toByteArray(Charsets.UTF_8))
                    zip.closeEntry()
                }
            } finally {
                baseDatos = ProductDatabase(this)
                baseDatos.readableDatabase
            }
        }

        aviso("Copia de seguridad creada correctamente.")
    } catch (e: Exception) {
        aviso(
            "Error al crear la copia: " +
                (e.localizedMessage ?: "desconocido")
        )
    }
}


private fun restaurarCopiaDesdeUri(uri: Uri) {
    val temporal = File(cacheDir, "custock_restauracion.tmp")
    val categoriasTemporal = File(cacheDir, "categorias_restauracion.tmp")

    try {
        var encontroBD = false
        var encontroCategorias = false

        val entradaZip = contentResolver.openInputStream(uri)
            ?: throw Exception("No se pudo abrir el archivo.")

        entradaZip.use { entrada ->
            ZipInputStream(entrada).use { zip ->
                var elemento = zip.nextEntry

                while (elemento != null) {
                    when (elemento.name) {
                        NOMBRE_BASE_DATOS -> {
                            FileOutputStream(temporal).use { salida ->
                                zip.copyTo(salida)
                            }
                            encontroBD = true
                        }

                        "categorias.json" -> {
                            FileOutputStream(categoriasTemporal).use { salida ->
                                zip.copyTo(salida)
                            }
                            encontroCategorias = true
                        }
                    }

                    zip.closeEntry()
                    elemento = zip.nextEntry
                }
            }
        }

        if (!encontroBD || !temporal.exists() || temporal.length() == 0L) {
            throw Exception("La copia no contiene una base de datos válida.")
        }

        val comprobacion = android.database.sqlite.SQLiteDatabase.openDatabase(
            temporal.absolutePath,
            null,
            android.database.sqlite.SQLiteDatabase.OPEN_READONLY
        )

        try {
            val tablasNecesarias = listOf(
                "productos",
                "ventas",
                "detalle_ventas",
                "fiados",
                "pagos_fiados"
            )

            val cursor = comprobacion.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table'",
                null
            )

            val tablas = mutableSetOf<String>()
            cursor.use {
                while (it.moveToNext()) {
                    tablas.add(it.getString(0))
                }
            }

            if (!tablasNecesarias.all { it in tablas }) {
                throw Exception("La base de datos no corresponde a CuStock.")
            }
        } finally {
            comprobacion.close()
        }

        var categoriasRestauradas = prefs.getString("categorias", "[]") ?: "[]"

        if (encontroCategorias) {
            categoriasRestauradas = categoriasTemporal.readText(Charsets.UTF_8)
            org.json.JSONArray(categoriasRestauradas)
        }

        val archivoBD = getDatabasePath(NOMBRE_BASE_DATOS)
        val respaldoAnterior = File(cacheDir, "custock_anterior.tmp")

        baseDatos.close()

        if (archivoBD.exists() && !archivoBD.copyTo(respaldoAnterior, true).exists()) {
            throw Exception("No se pudo proteger la base de datos anterior.")
        }

        try {
            File(archivoBD.absolutePath + "-wal").delete()
            File(archivoBD.absolutePath + "-shm").delete()

            if (!temporal.copyTo(archivoBD, true).exists()) {
                throw Exception("No se pudo reemplazar la base de datos.")
            }

            if (encontroCategorias) {
                prefs.edit()
                    .putString("categorias", categoriasRestauradas)
                    .apply()

                categorias.clear()
                val lista = org.json.JSONArray(categoriasRestauradas)
                for (i in 0 until lista.length()) {
                    categorias.add(lista.getString(i))
                }
            }

            baseDatos = ProductDatabase(this)
            baseDatos.readableDatabase

            respaldoAnterior.delete()
            aviso("Copia restaurada correctamente.")
            construirPantalla()
        } catch (e: Exception) {
            archivoBD.delete()
            if (respaldoAnterior.exists()) {
                respaldoAnterior.copyTo(archivoBD, true)
            }
            baseDatos = ProductDatabase(this)
            baseDatos.readableDatabase
            throw e
        }
    } catch (e: Exception) {
        aviso("Error al restaurar: ${e.localizedMessage ?: "archivo no válido"}")
    } finally {
        temporal.delete()
        categoriasTemporal.delete()
    }
}
  
    // SELECCIÓN PERSISTENTE DE CARPETA E INFORME WORD

    private fun seleccionarCarpetaInformes() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(intent, CODIGO_CARPETA_INFORMES)
    }
    
@Deprecated("Se conserva para compatibilidad con los selectores de Android.")
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode == CODIGO_CREAR_COPIA ||
        requestCode == CODIGO_RESTAURAR_COPIA
    ) {
        if (resultCode == RESULT_OK) {
            val uri = data?.data

            if (uri == null) {
                aviso("No se seleccionó ningún archivo.")
            } else if (requestCode == CODIGO_CREAR_COPIA) {
                crearCopiaDesdeUri(uri)
            } else {
                restaurarCopiaDesdeUri(uri)
            }
        }
        return
    }

    if (requestCode == CODIGO_CARPETA_INFORMES &&
        resultCode == RESULT_OK
    ) {
        val uri = data?.data

        if (uri == null) {
            aviso("No se seleccionó ninguna carpeta.")
            return
        }

        try {
            val flags = data.flags and
                (Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

            contentResolver.takePersistableUriPermission(uri, flags)

            prefs.edit()
                .putString(CLAVE_CARPETA_INFORMES, uri.toString())
                .apply()

            aviso("Carpeta de informes guardada.")
            abrirInformes()
        } catch (e: Exception) {
            aviso(
                "No se pudo guardar el permiso de la carpeta: " +
                    (e.localizedMessage ?: "error desconocido")
            )
        }
    }
}

    private fun carpetaInformesGuardada(): Uri? =
        prefs.getString(CLAVE_CARPETA_INFORMES, null)?.let {
            runCatching { Uri.parse(it) }.getOrNull()
        }

    private fun escaparXml(texto: String): String =
        texto.replace("&", "&amp;").replace("<", "&lt;")
            .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;")

    private fun parrafoWord(texto: String, negrita: Boolean = false): String {
        val bold = if (negrita) "<w:rPr><w:b/></w:rPr>" else ""
        return "<w:p><w:r>$bold<w:t xml:space=\"preserve\">${escaparXml(texto)}</w:t></w:r></w:p>"
    }

    private fun escribirDocx(salida: OutputStream, parrafos: List<Pair<String, Boolean>>) {
        val documentXml = buildString {
            append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            append("""<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"><w:body>""")
            parrafos.forEach { (texto, negrita) -> append(parrafoWord(texto, negrita)) }
            append("""<w:sectPr><w:pgSz w:w="11906" w:h="16838"/><w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="708" w:footer="708" w:gutter="0"/></w:sectPr></w:body></w:document>""")
        }
        val tipos = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="xml" ContentType="application/xml"/><Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/></Types>"""
        val relaciones = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/></Relationships>"""
        ZipOutputStream(salida).use { zip ->
            fun entrada(nombre: String, contenido: String) {
                zip.putNextEntry(ZipEntry(nombre))
                zip.write(contenido.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
            entrada("[Content_Types].xml", tipos)
            entrada("_rels/.rels", relaciones)
            entrada("word/document.xml", documentXml)
        }
    }

    private fun guardarInformeWord() {
        val carpeta = carpetaInformesGuardada()
        if (carpeta == null) {
            aviso("Primero selecciona la carpeta donde guardar los informes.")
            seleccionarCarpetaInformes()
            return
        }
        val calendario = Calendar.getInstance()
        calendario.set(Calendar.HOUR_OF_DAY, 0)
        calendario.set(Calendar.MINUTE, 0)
        calendario.set(Calendar.SECOND, 0)
        calendario.set(Calendar.MILLISECOND, 0)
        val inicio = calendario.timeInMillis
        calendario.add(Calendar.DAY_OF_MONTH, 1)
        val fin = calendario.timeInMillis
        val fechaArchivo = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(inicio))
        val fechaLegible = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(inicio))
        val ventas = baseDatos.ventasDelPeriodo(inicio, fin)
        val pagos = baseDatos.pagosFiadosDelPeriodo(inicio, fin)
        val totalVentas = ventas.sumOf { it.total }
        val ganancia = baseDatos.gananciaRegistrada(inicio, fin)
        val totalPagos = pagos.sumOf { it.importe }
        val pendientes = baseDatos.listarFiados("", true).sumOf { it.saldo }

        val parrafos = mutableListOf<Pair<String, Boolean>>()
        parrafos.add("CuStock — Informe diario" to true)
        parrafos.add("Fecha: $fechaLegible" to false)
        parrafos.add("" to false)
        parrafos.add("RESUMEN DEL DÍA" to true)
        parrafos.add("Ventas registradas: ${dinero(totalVentas)} CUP" to false)
        parrafos.add("Ganancia bruta registrada: ${dinero(ganancia)} CUP" to false)
        parrafos.add("Cobros de fiados recibidos hoy: ${dinero(totalPagos)} CUP" to false)
        parrafos.add("Saldo total de fiados pendientes: ${dinero(pendientes)} CUP" to false)
        parrafos.add("" to false)
        parrafos.add("DETALLE DE VENTAS" to true)
        if (ventas.isEmpty()) parrafos.add("No hay ventas registradas hoy." to false)
        ventas.forEach { venta ->
            val hora = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(venta.fecha))
            parrafos.add("Venta #${venta.id} — $hora — Cliente: ${venta.cliente.ifBlank { "Sin nombre" }}" to true)
            parrafos.add("Tipo: ${if (venta.esFiado) "Fiada" else "Pagada"} — Total: ${dinero(venta.total)} CUP" to false)
            venta.lineas.forEach { linea ->
                parrafos.add("  • ${linea.nombre}: ${linea.cantidad} × ${dinero(linea.precioUnitario)} CUP = ${dinero(linea.subtotal)} CUP" to false)
            }
            parrafos.add("" to false)
        }
        parrafos.add("PAGOS DE FIADOS RECIBIDOS HOY" to true)
        if (pagos.isEmpty()) parrafos.add("No se registraron pagos de fiados hoy." to false)
        
        pagos.forEach { pago ->
            val hora = SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            ).format(Date(pago.fecha))

            parrafos.add(
                "$hora — ${pago.cliente} — " +
                    "Fiado #${pago.fiadoId}: " +
                    "${dinero(pago.importe)} CUP" to false
            )

            val fiadoRelacionado = baseDatos.listarFiados("", false)
                .firstOrNull { it.id == pago.fiadoId }

            if (fiadoRelacionado != null) {
                val productos = baseDatos.productosDelFiado(
                    fiadoRelacionado.ventaId
                )

                parrafos.add(
                    "Productos asociados a este fiado:" to true
                )

                productos.forEach { producto ->
                    parrafos.add(
                        "  • ${producto.nombre}: " +
                            "${producto.cantidad} unidades — " +
                            "Precio unitario: " +
                            "${dinero(producto.precioUnitario)} CUP — " +
                            "Subtotal: ${dinero(producto.subtotal)} CUP"
                        to false
                    )
                }
            }

            parrafos.add("" to false)
        }

        parrafos.add("" to false)
        parrafos.add("Nota: la ganancia es bruta y se calcula con los costes guardados en cada venta. No descuenta otros gastos del negocio." to false)

        try {
            val nombre = "CuStock_Informe_$fechaArchivo.docx"
            val carpetaDocumento = DocumentsContract.buildDocumentUriUsingTree(
    carpeta,
    DocumentsContract.getTreeDocumentId(carpeta)
)

val documentoUri = DocumentsContract.createDocument(
    contentResolver, carpetaDocumento,
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    nombre
) ?: throw IllegalStateException(
    "Android no pudo crear el documento en esa carpeta."
)
            val salida = contentResolver.openOutputStream(documentoUri, "w")
                ?: throw IllegalStateException("No se pudo abrir el archivo para escribir.")
            salida.use { escribirDocx(it, parrafos) }
            aviso("Informe Word guardado: $nombre")
        } catch (e: Exception) {
            aviso("Error al guardar el informe: " + (e.localizedMessage ?: "desconocido"))
        }
    }

    private fun abrirInformes() {
        val calendario = Calendar.getInstance()
        calendario.set(Calendar.HOUR_OF_DAY, 0)
        calendario.set(Calendar.MINUTE, 0)
        calendario.set(Calendar.SECOND, 0)
        calendario.set(Calendar.MILLISECOND, 0)
        val inicio = calendario.timeInMillis
        calendario.add(Calendar.DAY_OF_MONTH, 1)
        val fin = calendario.timeInMillis
        val ventas = baseDatos.totalVentas(inicio, fin)
        val ganancia = baseDatos.gananciaRegistrada(inicio, fin)
        val fiadosPendientes = baseDatos.listarFiados("", true).sumOf { it.saldo }
        val carpeta = carpetaInformesGuardada()

        val raiz = nuevaRaiz()
        agregarCabecera(raiz, "Informes", "Resumen del negocio")
        val contenido = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(15), dp(20), dp(20))
        }
        agregarBoton(contenido, "← Volver al inicio") { construirPantalla() }
        contenido.addView(TextView(this).apply {
            text = "Resumen de hoy\n\nFecha: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}\n\n" +
                "Ventas registradas: ${dinero(ventas)} CUP\n" +
                "Ganancia bruta registrada: ${dinero(ganancia)} CUP\n" +
                "Fiados pendientes: ${dinero(fiadosPendientes)} CUP\n\n" +
                "Carpeta de informes: ${if (carpeta == null) "No seleccionada" else "Seleccionada"}"
            textSize = 18f
            setTextColor(azul)
            setPadding(0, dp(15), 0, dp(15))
        })
        contenido.addView(TextView(this).apply {
            text = "La ganancia se calcula usando el precio de venta y el coste guardados al realizar cada venta. No incluye otros gastos."
            textSize = 14f
            setTextColor(Color.DKGRAY)
        })
        
        val ventasDetalle = baseDatos.ventasDelPeriodo(inicio, fin)
        val pagosDetalle = baseDatos.pagosFiadosDelPeriodo(inicio, fin)

        contenido.addView(TextView(this).apply {
            text = "\nDETALLE DE VENTAS DE HOY"
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(azul)
            setPadding(0, dp(18), 0, dp(8))
        })

        if (ventasDetalle.isEmpty()) {
            contenido.addView(TextView(this).apply {
                text = "No hay ventas registradas hoy."
                textSize = 15f
            })
        }

        ventasDetalle.forEach { venta ->
            contenido.addView(TextView(this).apply {
                text = "Venta #${venta.id} — ${fechaHora(venta.fecha)}\n" +
                    "Cliente: ${venta.cliente.ifBlank { "Sin nombre" }}\n" +
                    "Tipo: ${if (venta.esFiado) "Fiada" else "Pagada"}\n" +
                    "Total: ${dinero(venta.total)} CUP\n" +
                    venta.lineas.joinToString("\n") { linea ->
                        "• ${linea.nombre}: ${linea.cantidad} × " +
                            "${dinero(linea.precioUnitario)} CUP = " +
                            "${dinero(linea.subtotal)} CUP"
                    }
                textSize = 15f
                setTextColor(if (temaOscuro) Color.WHITE else Color.DKGRAY)
                setPadding(dp(10), dp(10), dp(10), dp(10))
                background = fondoRedondeado(
                    if (temaOscuro) Color.rgb(42, 49, 60)
                    else Color.WHITE
                )
            })
        }

        contenido.addView(TextView(this).apply {
            text = "\nPAGOS DE FIADOS RECIBIDOS HOY"
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(azul)
            setPadding(0, dp(18), 0, dp(8))
        })

        if (pagosDetalle.isEmpty()) {
            contenido.addView(TextView(this).apply {
                text = "No se registraron pagos de fiados hoy."
                textSize = 15f
            })
        }

        pagosDetalle.forEach { pago ->
            val fiadoRelacionado = baseDatos.listarFiados("", false)
                .firstOrNull { it.id == pago.fiadoId }

            val productosAsociados = fiadoRelacionado?.let {
                baseDatos.productosDelFiado(it.ventaId)
            }.orEmpty()

            val textoProductos = if (productosAsociados.isEmpty()) {
                "No hay productos asociados disponibles."
            } else {
                "Productos asociados a este fiado:\n" +
                    productosAsociados.joinToString("\n") { producto ->
                        "• ${producto.nombre}: ${producto.cantidad} unidades — " +
                            "${dinero(producto.precioUnitario)} CUP por unidad — " +
                            "Subtotal: ${dinero(producto.subtotal)} CUP"
                    }
            }

            contenido.addView(TextView(this).apply {
                text = "${fechaHora(pago.fecha)} — ${pago.cliente}\n" +
                    "Fiado #${pago.fiadoId}: ${dinero(pago.importe)} CUP\n" +
                    textoProductos
                textSize = 15f
                setTextColor(if (temaOscuro) Color.WHITE else Color.DKGRAY)
                setPadding(dp(10), dp(10), dp(10), dp(10))
                background = fondoRedondeado(
                    if (temaOscuro) Color.rgb(42, 49, 60)
                    else Color.WHITE
                )
            })
        }

        agregarBoton(contenido, "📁 Seleccionar carpeta de informes") { seleccionarCarpetaInformes() }
        agregarBoton(contenido, "📝 Guardar informe diario en Word (.docx)") { guardarInformeWord() }
        raiz.addView(contenido, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(raiz)
    }

    override fun onDestroy() {
        if (::baseDatos.isInitialized) baseDatos.close()
        super.onDestroy()
    }
}
