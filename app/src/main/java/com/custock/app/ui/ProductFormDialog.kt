package com.custock.app.ui

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.custock.app.Producto
import com.custock.app.database.ProductDatabase
import com.custock.app.dp

class ProductFormDialog(
    private val context: Context,
    private val categoria: String,
    private val existente: Producto?,
    private val onSave: (Producto, Boolean) -> Unit
) {

    fun show() {
        val contenedor = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(context.dp(20), context.dp(5), context.dp(20), context.dp(5))
        }

        fun campo(texto: String, valor: String = ""): EditText {
            val edit = EditText(context).apply {
                hint = texto
                setSingleLine(true)
                setText(valor)
            }
            contenedor.addView(edit)
            return edit
        }

        val nombre = campo("Nombre del producto", existente?.nombre.orEmpty())
        val cantidad = campo("Existencias", existente?.cantidad?.toString().orEmpty())
        cantidad.inputType = InputType.TYPE_CLASS_NUMBER

        val coste = campo("Precio de coste", existente?.coste?.toString().orEmpty())
        val venta = campo("Precio de venta", existente?.venta?.toString().orEmpty())

        val referencia = campo("Referencia (opcional)", existente?.referencia.orEmpty())
        val descripcion = campo("Descripción (opcional)", existente?.descripcion.orEmpty())

        val monedas = listOf(
            "CUP" to "Peso cubano (CUP)",
            "USD" to "Dólar estadounidense (USD)",
            "EUR" to "Euro (EUR)",
            "MXN" to "Peso mexicano (MXN)",
            "COP" to "Peso colombiano (COP)",
            "ARS" to "Peso argentino (ARS)",
            "PEN" to "Sol peruano (PEN)",
            "BRL" to "Real brasileño (BRL)"
        )

        val selectorMoneda = Spinner(context)
        selectorMoneda.adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_dropdown_item,
            monedas.map { it.second }
        )
        selectorMoneda.setSelection(
            monedas.indexOfFirst { it.first == (existente?.moneda ?: "CUP") }.coerceAtLeast(0)
        )

        contenedor.addView(TextView(context).apply {
            text = "Moneda del producto"
            textSize = 14f
            setTextColor(Color.rgb(24, 65, 120))
        })

        contenedor.addView(selectorMoneda)

        val dialog = AlertDialog.Builder(context)
            .setTitle(if (existente == null) "Nuevo producto" else "Editar producto")
            .setView(contenedor)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val n = nombre.text.toString().trim()
                val q = cantidad.text.toString().toIntOrNull()
                val c = coste.text.toString().replace(',', '.').toDoubleOrNull()
                val v = venta.text.toString().replace(',', '.').toDoubleOrNull()

                when {
                    n.isBlank() -> {
                        Toast.makeText(context, "El nombre del producto es obligatorio.", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                    q == null || q < 0 -> {
                        Toast.makeText(context, "Las existencias deben ser un número válido y no negativo.", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                    c == null || c < 0 || !c.isFinite() -> {
                        Toast.makeText(context, "El coste debe ser un número válido y no negativo.", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                    v == null || v < 0 || !v.isFinite() -> {
                        Toast.makeText(context, "La venta debe ser un número válido y no negativo.", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                }

                val db = ProductDatabase(context)
                val producto = Producto(
                    id = existente?.id ?: 0L,
                    nombre = n,
                    categoria = categoria,
                    cantidad = q,
                    coste = c,
                    venta = v,
                    referencia = referencia.text.toString().trim(),
                    descripcion = descripcion.text.toString().trim(),
                    moneda = monedas[selectorMoneda.selectedItemPosition].first
                )

                try {
                    val id = db.guardar(producto)
                    if (id > 0) {
                        dialog.dismiss()
                        onSave(producto, true)
                        Toast.makeText(context, "Producto guardado correctamente.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "No se pudo guardar el producto.", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message ?: "No se pudo guardar."}", Toast.LENGTH_LONG).show()
                }
            }
        }

        dialog.show()
    }
}