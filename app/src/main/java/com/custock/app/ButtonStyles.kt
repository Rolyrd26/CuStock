
package com.custock.app

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.widget.Button

fun Button.applyCuStockStyle(primary: Boolean = true) {
    val azul = Color.rgb(35, 103, 177)
    val fondo = if (primary) azul else Color.rgb(235, 242, 250)
    val texto = if (primary) Color.WHITE else Color.rgb(35, 76, 125)

    background = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = context.dp(14).toFloat()
        setColor(fondo)
        if (!primary) {
            setStroke(context.dp(1), Color.rgb(210, 224, 240))
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
