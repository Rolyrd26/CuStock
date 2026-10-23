package com.custock.app

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.custock.app.database.ProductDatabase
import com.custock.app.ui.CategoryScreen
import com.custock.app.ui.DebtsScreen
import com.custock.app.ui.HomeScreen
import com.custock.app.ui.ReportsScreen
import com.custock.app.ui.SalesScreen
import com.custock.app.ui.SettingsScreen

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var db: ProductDatabase

    private val categorias = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences(AppConstants.PREFS_NAME, MODE_PRIVATE)
        db = ProductDatabase(this)
        if (prefs.getBoolean(AppConstants.KEY_TEMA_OSCURO, false)) {
    window.statusBarColor = android.graphics.Color.rgb(18, 18, 18)
    window.navigationBarColor = android.graphics.Color.rgb(18, 18, 18)
} else {
    window.statusBarColor = android.graphics.Color.rgb(24, 65, 120)
    window.navigationBarColor = android.graphics.Color.rgb(24, 65, 120)
        }

        val guardadas = prefs.getString(AppConstants.KEY_CATEGORIAS, null)
        if (guardadas == null) {
            categorias.addAll(AppConstants.DEFAULT_CATEGORIES)
        } else {
            val json = org.json.JSONArray(guardadas)
            for (i in 0 until json.length()) {
                categorias.add(json.getString(i))
            }
            AppConstants.DEFAULT_CATEGORIES.forEach { nombre ->
                if (categorias.none { it.equals(nombre, true) }) {
                    categorias.add(nombre)
                }
            }
        }

        guardarCategorias()
        mostrarHome()
    }

    private fun guardarCategorias() {
        val json = org.json.JSONArray()
        categorias.forEach { json.put(it) }
        prefs.edit().putString(AppConstants.KEY_CATEGORIAS, json.toString()).apply()
    }

    private fun mostrarHome() {
        val screen = HomeScreen(
            context = this,
            categorias = categorias,
            onOpenCategory = { categoria ->
                mostrarCategoria(categoria)
            },
            onOpenSales = {
                mostrarVentas()
            },
            onOpenDebts = {
                mostrarFiados()
            },
            onOpenReports = {
                mostrarInformes()
            },
            onOpenSettings = {
                mostrarAjustes()
            },
            onAddCategory = { nombre ->
                categorias.add(nombre)
                guardarCategorias()
                mostrarHome()
            }
        )
        setContentView(screen.buildView())
    }

    private fun mostrarCategoria(categoria: String) {
        val screen = CategoryScreen(
            context = this,
            db = db,
            categoria = categoria,
            onBack = {
                mostrarHome()
            }
        )
        setContentView(screen.buildView())
    }

    private fun mostrarVentas() {
        val screen = SalesScreen(
            context = this,
            db = db,
            categorias = categorias,
            onBack = {
                mostrarHome()
            }
        )
        setContentView(screen.buildView())
    }

    private fun mostrarFiados() {
        val screen = DebtsScreen(
            context = this,
            db = db,
            onBack = {
                mostrarHome()
            }
        )
        setContentView(screen.buildView())
    }

    private fun mostrarInformes() {
        val screen = ReportsScreen(
            context = this,
            db = db,
            onBack = {
                mostrarHome()
            }
        )
        setContentView(screen.buildView())
    }

    private fun mostrarAjustes() {
        val screen = SettingsScreen(
            context = this,
            prefs = prefs,
            onBack = {
                mostrarHome()
            }
        )
        setContentView(screen.buildView())
    }
}
