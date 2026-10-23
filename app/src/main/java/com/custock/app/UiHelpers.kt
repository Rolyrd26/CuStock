package com.custock.app

import android.content.Context
import android.graphics.drawable.GradientDrawable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Context.dp(value: Int): Int =
    (value * resources.displayMetrics.density).toInt()

fun dinero(valor: Double): String =
    String.format(Locale("es", "CU"), "%,.2f", valor)

fun fechaHora(ms: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(ms))

fun Context.fondoRedondeado(color: Int, radioDp: Int = 16): GradientDrawable =
    GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radioDp).toFloat()
    }