package com.custock.app

object AppConstants {
    const val APP_NAME = "CuStock"
    const val PREFS_NAME = "CuStock"
    const val DB_NAME = "custock.db"
    const val DB_VERSION = 5

    const val KEY_TEMA_OSCURO = "tema_oscuro"
    const val KEY_CATEGORIAS = "categorias"
    const val DEFAULT_CURRENCY = "CUP"

    val DEFAULT_CATEGORIES = listOf(
        "Ropa",
        "Alimentos",
        "Lencería",
        "Calzado",
        "Higiene",
        "Bebidas",
        "Electrodomésticos",
        "Accesorios",
        "Otros"
    )
}