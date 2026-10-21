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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("CuStock", MODE_PRIVATE) }
    private lateinit var baseDatos: ProductDatabase
    private val categorias = mutableListOf<String>()

    private val predeterminadas = listOf(
        "Ropa", "Alimentos", "Lencería", "Calzado", "Higiene",
        "Bebidas", "Electrodomésticos", "Accesorios", "Otros"
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

    private fun icono(nombre: String) =
        iconos[nombre.trim().lowercase(Locale.ROOT)] ?: "📦"

    private fun dp(n: Int) =
        (n * resources.displayMetrics.density).toInt()

    private fun dinero(valor: Double) =
        String.format(Locale("es", "CU"), "%,.2f", valor)

    private fun fechaHora(ms: Long) =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            .format(Date(ms))

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

        val guardadas = prefs.getString("categorias", null)

        if (guardadas == null) {
            categorias.addAll(predeterminadas)
        } else {
            val json = org.json.JSONArray(guardadas)

            for (i in 0 until json.length()) {
                categorias.add(json.getString(i))
            }

            predeterminadas.forEach { nombre ->
                if (categorias.none { it.equals(nombre, true) }) {
                    categorias.add(nombre)
                }
            }
        }

        guardarCategorias()
        construirPantalla()
    }

    private fun guardarCategorias() {
        val json = org.json.JSONArray()

        categorias.forEach { json.put(it) }

        prefs.edit()
            .putString("categorias", json.toString())
            .apply()
    }

    private fun nuevaRaiz(): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(fondo)
        }

    private fun agregarCabecera(
        raiz: LinearLayout,
        titulo: String,
        subtitulo: String
    ) {
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

    private fun agregarBoton(
        contenedor: LinearLayout,
        texto: String,
        accion: () -> Unit
    ) {
        contenedor.addView(Button(this).apply {
            text = texto
            setOnClickListener { accion() }
        })
    }

    // PANTALLA PRINCIPAL

    private fun construirPantalla() {
        val raiz = nuevaRaiz()

        agregarCabecera(
            raiz,
            "CuStock",
            "Tu inventario, siempre contigo"
        )

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

        agregarBoton(contenido, "🛒  Nueva venta") {
            abrirVentas()
        }

        agregarBoton(contenido, "📒  Control de fiados") {
            abrirFiados()
        }

        agregarBoton(contenido, "📊  Informes y ganancias") {
            abrirInformes()
        }

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
            text = "+ Añadir"
            setOnClickListener { nuevaCategoria() }
        })

        contenido.addView(filaTitulo)

        contenido.addView(TextView(this).apply {
            text = "Selecciona una categoría para gestionar sus productos."
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(0, dp(5), 0, dp(12))
        })

        val grid = GridLayout(this).apply {
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
                setOnClickListener {
                    abrirCategoria(nombre)
                }
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

            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = dp(135)
                columnSpec = GridLayout.spec(
                    GridLayout.UNDEFINED,
                    1f
                )
                setMargins(dp(5), dp(5), dp(5), dp(7))
            }

            grid.addView(tarjeta, params)
        }

        contenido.addView(grid)
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

                when {
                    nombre.isBlank() -> {
                        aviso("Escribe un nombre")
                    }

                    categorias.any { it.equals(nombre, true) } -> {
                        aviso("La categoría ya existe")
                    }

                    else -> {
                        categorias.add(nombre)
                        guardarCategorias()
                        construirPantalla()
                        aviso("Categoría creada")
                    }
                }
            }
            .show()
    }

    // INVENTARIO Y CATEGORÍAS

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

        val scroll = ScrollView(this).apply {
            addView(lista)
        }

        raiz.addView(
            scroll,
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
                    background = fondoRedondeado(
                        Color.rgb(240, 245, 252)
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
                    height = dp(8)
                })
            }
        }

        busqueda.addTextChangedListener(
            object : android.text.TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    actualizarLista(s?.toString().orEmpty())
                }

                override fun afterTextChanged(
                    s: android.text.Editable?
                ) {}
            }
        )

        val dialog = AlertDialog.Builder(this)
            .setTitle("${icono(categoria)} $categoria")
            .setView(raiz)
            .setPositiveButton("Añadir producto", null)
            .setNegativeButton("Cerrar", null)
            .create()

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

        val nombre = campo(
            "Nombre del producto",
            existente?.nombre.orEmpty()
        )

        val cantidad = campo(
            "Existencias",
            existente?.cantidad?.toString() ?: "0"
        )

        cantidad.inputType = InputType.TYPE_CLASS_NUMBER

        val coste = campo(
            "Precio de coste en CUP",
            existente?.coste?.toString() ?: "0"
        )

        coste.inputType =
            InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_FLAG_DECIMAL

        val venta = campo(
            "Precio de venta en CUP",
            existente?.venta?.toString() ?: "0"
        )

        venta.inputType =
            InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_FLAG_DECIMAL

        val referencia = campo(
            "Referencia (opcional)",
            existente?.referencia.orEmpty()
        )

        val descripcion = campo(
            "Descripción (opcional)",
            existente?.descripcion.orEmpty()
        )

        val dialog = AlertDialog.Builder(this)
            .setTitle(
                if (existente == null) {
                    "Nuevo producto"
                } else {
                    "Editar producto"
                }
            )
            .setView(contenedor)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    val n = nombre.text.toString().trim()
                    val q = cantidad.text.toString().toIntOrNull()

                    val c = coste.text.toString()
                        .replace(',', '.')
                        .toDoubleOrNull()

                    val v = venta.text.toString()
                        .replace(',', '.')
                        .toDoubleOrNull()

                    if (
                        n.isBlank() ||
                        q == null ||
                        q < 0 ||
                        c == null ||
                        c < 0 ||
                        !c.isFinite() ||
                        v == null ||
                        v < 0 ||
                        !v.isFinite()
                    ) {
                        aviso(
                            "Revisa el nombre, las existencias y los precios."
                        )
                        return@setOnClickListener
                    }

                    try {
                        val resultado = baseDatos.guardar(
                            Producto(
                                id = existente?.id ?: 0L,
                                nombre = n,
                                categoria = categoria,
                                cantidad = q,
                                coste = c,
                                venta = v,
                                referencia = referencia.text.toString().trim(),
                                descripcion = descripcion.text.toString().trim()
                            )
                        )

                        if (resultado > 0) {
                            dialog.dismiss()
                            alGuardar()
                            aviso("Producto guardado")
                        } else {
                            aviso("No se pudo guardar el producto.")
                        }
                    } catch (e: Exception) {
                        aviso(
                            "Error al guardar: " +
                                (e.localizedMessage ?: "desconocido")
                        )
                    }
                }
        }

        dialog.show()
    }

    private fun todosLosProductos(): List<Producto> =
        categorias.flatMap { baseDatos.listar(it) }
            .distinctBy { it.id }
            .sortedBy { it.nombre.lowercase(Locale.ROOT) }

    // VENTAS

    private fun abrirVentas() {
        val carrito = mutableListOf<LineaVenta>()

        val raiz = nuevaRaiz()

        agregarCabecera(
            raiz,
            "Nueva venta",
            "Importes en pesos cubanos (CUP)"
        )

        val contenido = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(10), dp(16), dp(16))
        }

        agregarBoton(contenido, "← Volver al inicio") {
            construirPantalla()
        }

        val cliente = EditText(this).apply {
            hint = "Nombre del cliente (obligatorio para fiar)"
            setSingleLine(true)
        }

        contenido.addView(cliente)

        val lista = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val scroll = ScrollView(this).apply {
            addView(lista)
        }

        val totalTexto = TextView(this).apply {
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(azul)
            setPadding(0, dp(10), 0, dp(10))
        }

        lateinit var renderizarCarrito: () -> Unit
        lateinit var confirmar: (Boolean) -> Unit

        agregarBoton(contenido, "+ Añadir producto") {
            val disponibles = todosLosProductos()
                .filter { it.cantidad > 0 }

            if (disponibles.isEmpty()) {
                aviso("No hay productos con existencias disponibles.")
            } else {
                val opciones = disponibles.map {
                    "${it.nombre} — ${dinero(it.venta)} CUP " +
                        "(stock: ${it.cantidad})"
                }.toTypedArray()

                AlertDialog.Builder(this)
                    .setTitle("Selecciona un producto")
                    .setItems(opciones) { _, indice ->
                        val producto = disponibles[indice]

                        val yaEnCarrito = carrito
                            .filter { it.productoId == producto.id }
                            .sumOf { it.cantidad }

                        val restante = producto.cantidad - yaEnCarrito

                        if (restante <= 0) {
                            aviso(
                                "Ya añadiste todas las existencias " +
                                    "disponibles de ${producto.nombre}."
                            )
                        } else {
                            val campo = EditText(this).apply {
                                hint = "Cantidad (máximo $restante)"
                                inputType = InputType.TYPE_CLASS_NUMBER
                                setText("1")
                                selectAll()
                            }

                            val dlg = AlertDialog.Builder(this)
                                .setTitle(producto.nombre)
                                .setMessage(
                                    "Disponibles para añadir: $restante"
                                )
                                .setView(campo)
                                .setNegativeButton("Cancelar", null)
                                .setPositiveButton("Añadir", null)
                                .create()

                            dlg.setOnShowListener {
                                dlg.getButton(AlertDialog.BUTTON_POSITIVE)
                                    .setOnClickListener {
                                        val cantidad = campo.text
                                            .toString()
                                            .toIntOrNull()

                                        if (
                                            cantidad == null ||
                                            cantidad <= 0 ||
                                            cantidad > restante
                                        ) {
                                            aviso(
                                                "Introduce una cantidad " +
                                                    "entre 1 y $restante."
                                            )
                                        } else {
                                            val idx = carrito
                                                .indexOfFirst {
                                                    it.productoId == producto.id
                                                }

                                            if (idx >= 0) {
                                                carrito[idx] = carrito[idx]
                                                    .copy(
                                                        cantidad =
                                                            carrito[idx].cantidad +
                                                                cantidad
                                                    )
                                            } else {
                                                carrito.add(
                                                    LineaVenta(
                                                        producto.id,
                                                        cantidad,
                                                        producto.venta
                                                    )
                                                )
                                            }

                                            dlg.dismiss()
                                            renderizarCarrito()
                                        }
                                    }
                            }

                            dlg.show()
                        }
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }

        contenido.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        contenido.addView(totalTexto)

        agregarBoton(contenido, "Confirmar venta pagada") {
            confirmar(false)
        }

        agregarBoton(contenido, "Registrar venta fiada") {
            confirmar(true)
        }

        raiz.addView(
            contenido,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        setContentView(raiz)

        renderizarCarrito = {
            lista.removeAllViews()

            var total = 0.0

            if (carrito.isEmpty()) {
                lista.addView(TextView(this).apply {
                    text = "Añade productos para comenzar la venta."
                    setPadding(0, dp(12), 0, dp(12))
                })
            }

            carrito.toList().forEach { linea ->
                val producto = baseDatos.obtenerProducto(linea.productoId)

                if (producto == null) {
                    carrito.remove(linea)
                } else {
                    val subtotal =
                        linea.cantidad * linea.precioUnitario

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
                        text =
                            "${linea.cantidad} × " +
                                "${dinero(linea.precioUnitario)} CUP\n" +
                                "Subtotal: ${dinero(subtotal)} CUP"
                        textSize = 14f
                    })

                    tarjeta.addView(Button(this).apply {
                        text = "Quitar del carrito"

                        setOnClickListener {
                            carrito.removeAll {
                                it.productoId == linea.productoId
                            }

                            renderizarCarrito()
                        }
                    })

                    lista.addView(tarjeta)

                    lista.addView(TextView(this).apply {
                        height = dp(6)
                    })
                }
            }

            totalTexto.text = "TOTAL: ${dinero(total)} CUP"
        }

        confirmar = { esFiado ->
            if (carrito.isEmpty()) {
                aviso("Añade al menos un producto.")
            } else if (
                esFiado &&
                cliente.text.toString().isBlank()
            ) {
                aviso(
                    "Escribe el nombre del cliente para registrar el fiado."
                )
            } else {
                AlertDialog.Builder(this)
                    .setTitle(
                        if (esFiado) {
                            "Confirmar fiado"
                        } else {
                            "Confirmar venta"
                        }
                    )
                    .setMessage(
                        "Total: " +
                            "${dinero(carrito.sumOf {
                                it.cantidad * it.precioUnitario
                            })} CUP\n\n" +
                            if (esFiado) {
                                "Se creará una deuda independiente para " +
                                    "${cliente.text}."
                            } else {
                                "Se descontarán las existencias del inventario."
                            }
                    )
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Confirmar") { _, _ ->
                        try {
                            val resultado = baseDatos.registrarVenta(
                                lineas = carrito.toList(),
                                cliente = cliente.text.toString().trim(),
                                esFiado = esFiado
                            )

                            aviso(
                                "Venta registrada. Total: " +
                                    "${dinero(resultado.total)} CUP"
                            )

                            construirPantalla()
                        } catch (e: Exception) {
                            aviso(
                                "No se pudo registrar la venta: " +
                                    (e.localizedMessage ?: "error desconocido")
                            )
                        }
                    }
                    .show()
            }
        }

        renderizarCarrito()
    }

    // CONTROL DE FIADOS

    private fun abrirFiados() {
        val raiz = nuevaRaiz()

        agregarCabecera(
            raiz,
            "Control de fiados",
            "Cada deuda se controla por separado"
        )

        val contenido = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(10), dp(16), dp(18))
        }

        agregarBoton(contenido, "← Volver al inicio") {
            construirPantalla()
        }

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

        val lista = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val scroll = ScrollView(this).apply {
            addView(lista)
        }

        contenido.addView(
            scroll,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        raiz.addView(
            contenido,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        setContentView(raiz)

        fun actualizar() {
            lista.removeAllViews()

            val fiados = baseDatos.listarFiados(
                busqueda.text.toString().trim(),
                pendientes.isChecked
            )

            if (fiados.isEmpty()) {
                lista.addView(TextView(this).apply {
                    text = "No hay fiados que coincidan con la búsqueda."
                    setPadding(0, dp(15), 0, dp(15))
                })
            }

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
                    text =
                        "Fiado #${fiado.id} · ${fechaHora(fiado.fecha)}\n" +
                            "Total: ${dinero(fiado.total)} CUP\n" +
                            "Pagado: ${dinero(fiado.pagado)} CUP\n" +
                            "Pendiente: ${dinero(fiado.saldo)} CUP"
                    textSize = 14f
                })

                tarjeta.addView(Button(this).apply {
                    text = "Ver detalle y registrar pago"

                    setOnClickListener {
                        detalleFiado(fiado.id)
                    }
                })

                lista.addView(tarjeta)

                lista.addView(TextView(this).apply {
                    height = dp(8)
                })
            }
        }

        busqueda.addTextChangedListener(
            object : android.text.TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    actualizar()
                }

                override fun afterTextChanged(
                    s: android.text.Editable?
                ) {}
            }
        )

        pendientes.setOnCheckedChangeListener { _, _ ->
            actualizar()
        }

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
            setPadding(dp(18), dp(5), dp(18), dp(5))
        }

        contenido.addView(TextView(this).apply {
            text =
                "Cliente: ${fiado.cliente}\n" +
                    "Fiado #${fiado.id}\n" +
                    "Fecha: ${fechaHora(fiado.fecha)}\n\n" +
                    "Importe original: ${dinero(fiado.total)} CUP\n" +
                    "Total pagado: ${dinero(fiado.pagado)} CUP\n" +
                    "Saldo pendiente: ${dinero(fiado.saldo)} CUP"

            textSize = 16f
            setTextColor(azul)
        })

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
                    text =
                        "${fechaHora(pago.fecha)} — " +
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
        val fiado = baseDatos.listarFiados("", false)
            .firstOrNull { it.id == fiadoId } ?: return

        val campo = EditText(this).apply {
            hint = "Importe del pago en CUP"
            inputType =
                InputType.TYPE_CLASS_NUMBER or
                    InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Registrar pago")
            .setMessage(
                "Cliente: ${fiado.cliente}\n" +
                    "Saldo pendiente: ${dinero(fiado.saldo)} CUP\n" +
                    "Puedes pagar cualquier cantidad hasta el saldo pendiente."
            )
            .setView(campo)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar pago", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    val importe = campo.text.toString()
                        .replace(',', '.')
                        .toDoubleOrNull()

                    if (
                        importe == null ||
                        !importe.isFinite() ||
                        importe <= 0
                    ) {
                        aviso("Introduce un importe válido.")
                    } else {
                        try {
                            baseDatos.registrarPago(fiadoId, importe)

                            dialog.dismiss()

                            aviso("Pago registrado correctamente.")

                            detalleFiado(fiadoId)
                        } catch (e: Exception) {
                            aviso(
                                "No se pudo registrar el pago: " +
                                    (e.localizedMessage ?: "error desconocido")
                            )
                        }
                    }
                }
        }

        dialog.show()
    }

    // INFORMES Y GANANCIAS DEL DÍA

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

        val fiadosPendientes = baseDatos
            .listarFiados("", true)
            .sumOf { it.saldo }

        val raiz = nuevaRaiz()

        agregarCabecera(
            raiz,
            "Informes",
            "Resumen del negocio"
        )

        val contenido = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(15), dp(20), dp(20))
        }

        agregarBoton(contenido, "← Volver al inicio") {
            construirPantalla()
        }

        contenido.addView(TextView(this).apply {
            text =
                "Resumen de hoy\n\n" +
                    "Fecha: ${
                        SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale.getDefault()
                        ).format(Date())
                    }\n\n" +
                    "Ventas registradas: ${dinero(ventas)} CUP\n" +
                    "Ganancia bruta registrada: ${dinero(ganancia)} CUP\n" +
                    "Fiados pendientes: ${dinero(fiadosPendientes)} CUP"

            textSize = 18f
            setTextColor(azul)
            setPadding(0, dp(15), 0, dp(15))
        })

        contenido.addView(TextView(this).apply {
            text =
                "La ganancia se calcula usando el precio de venta " +
                    "y el coste guardados al realizar cada venta. " +
                    "No incluye otros gastos."

            textSize = 14f
            setTextColor(Color.DKGRAY)
        })

        raiz.addView(
            contenido,
            LinearLayout.LayoutParams(-1, 0, 1f)
        )

        setContentView(raiz)
    }

    override fun onDestroy() {
        if (::baseDatos.isInitialized) {
            baseDatos.close()
        }

        super.onDestroy()
    }
}
