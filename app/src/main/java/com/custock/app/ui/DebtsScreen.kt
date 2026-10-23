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
import com.custock.app.Fiado
import com.custock.app.database.ProductDatabase
import com.custock.app.dinero
import com.custock.app.dp

class DebtsScreen(
    private val context: Context,
    private val db: ProductDatabase
) {
    fun buildView(): LinearLayout {
        val raiz = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(context.dp(16), context.dp(12), context.dp(16), context.dp(16))
        }

        val buscador = EditText(context).apply {
            hint = "Buscar cliente"
            setSingleLine(true)
        }
        raiz.addView(buscador)

        val lista = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val scroll = ScrollView(context).apply {
            addView(lista)
        }

        raiz.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        fun renderizar() {
            lista.removeAllViews()
            val fiados = db.listarFiados(buscador.text.toString())

            if (fiados.isEmpty()) {
                lista.addView(TextView(context).apply {
                    text = "No hay fiados registrados."
                    setPadding(0, context.dp(12), 0, context.dp(12))
                })
                return
            }

            fiados.forEach { fiado ->
                val tarjeta = LinearLayout(context).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(context.dp(12), context.dp(10), context.dp(12), context.dp(10))
                    setBackgroundColor(Color.rgb(250, 250, 250))
                }

                tarjeta.addView(TextView(context).apply {
                    text = fiado.cliente
                    textSize = 18f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(Color.rgb(24, 65, 120))
                })

                tarjeta.addView(TextView(context).apply {
                    text = "Total: ${dinero(fiado.total)} ${fiado.moneda}\n" +
                        "Pagado: ${dinero(fiado.pagado)} ${fiado.moneda}\n" +
                        "Saldo: ${dinero(fiado.saldo)} ${fiado.moneda}"
                    textSize = 14f
                    setTextColor(Color.DKGRAY)
                })

                tarjeta.addView(Button(context).apply {
                    text = "Registrar pago"
                    setOnClickListener {
                        val campo = EditText(context).apply {
                            hint = "Importe"
                            setSingleLine(true)
                            inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                        }

                        AlertDialog.Builder(context)
                            .setTitle("Pago del fiado")
                            .setView(campo)
                            .setNegativeButton("Cancelar", null)
                            .setPositiveButton("Aceptar") { _, _ ->
                                val importe = campo.text.toString().replace(',', '.').toDoubleOrNull()
                                if (importe != null && importe > 0) {
                                    db.registrarPago(fiado.id, importe)
                                    renderizar()
                                }
                            }
                            .show()
                    }
                })

                lista.addView(tarjeta)
                lista.addView(TextView(context).apply { height = context.dp(8) })
            }
        }

        buscador.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                renderizar()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        renderizar()
        return raiz
    }
}