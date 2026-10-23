val desplazable = ScrollView(context)
val contenido = LinearLayout(context).apply {
    orientation = LinearLayout.VERTICAL
    setPadding(context.dp(16), context.dp(16), context.dp(16), context.dp(24))
}

contenido.addView(grid)
desplazable.addView(contenido)
raiz.addView(desplazable, LinearLayout.LayoutParams(-1, 0, 1f))