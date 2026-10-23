
package com.custock.app

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.widget.Button

fun Button.applyCuStockStyle(primary: Boolean = true) {
    
val oscuro = context.getSharedPreferences(
    AppConstants.PREFS_NAME,
    android.content.Context.MODE_PRIVATE
).getBoolean(AppConstants.KEY_TEMA_OSCURO, false)

val azul = if (oscuro) Color.rgb(100, 150, 220) else Color.rgb(35, 103, 177)
val fondo = when {
    primary && oscuro -> Color.rgb(35, 76, 125)
    primary -> azul
    oscuro -> Color.rgb(45, 45, 45)
    else -> Color.rgb(235, 242, 250)
}
val texto = if (primary) Color.WHITE else if (oscuro) Color.WHITE else Color.rgb(35, 76, 125)


    background = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = context.dp(14).toFloat()
        setColor(fondo)
        if (!primary) {
    setStroke(
        context.dp(1),
        if (oscuro) Color.rgb(70, 70, 70)
        else Color.rgb(210, 224, 240)
    )
        }
    }

    setTextColor(texto)
    textSize = 15f
    typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    isAllCaps = false
    minHeight = context.dp(50)
    minimumHeight = context.dp(50)
    setPadding(
        context.dp(18),
        context.dp(12),
        context.dp(18),
        context.dp(12)
    )
    elevation = context.dp(2).toFloat()
}
