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
import com.custock.app.database.ProductDatabase
import com.custock.app.dinero
import com.custock.app.dp
import com.custock.app.fondoRedondeado

class DebtsScreen(
    private val context: Context,
    private val db: ProductDatabase,
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
            text = "📒 Fiados"
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
        })

        textos.addView(TextView(context).apply {
            text = "Control de clientes con deuda"
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

        val buscador = EditText(context).apply {
            hint = "Buscar cliente"
            setSingleLine(true)
            setPadding(context.dp(12), context.dp(10), context.dp(12), context.dp(10))
            background = context.fondoRedondeado(Color.WHITE)
        }
        contenido.addView(buscador)

        val lista = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }

        val scroll = ScrollView(context).apply {
            addView(lista)
        }

        contenido.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

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
                    background = context.fondoRedondeado(Color.rgb(250, 250, 250))
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
                            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
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
                                } else {
                                    AlertDialog.Builder(context)
                                        .setTitle("Importe inválido")
                                        .setMessage("Debe ser un número mayor que 0.")
                                        .setPositiveButton("Aceptar", null)
                                        .show()
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

        raiz.addView(contenido)
        renderizar()
        return raiz
    }
}