package com.custock.app.database

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class BackupManager(private val context: Context) {

    private val dbName = "custock.db"

    fun exportDatabase(targetFile: File): File {
        val source = context.getDatabasePath(dbName)
        if (!source.exists()) {
            throw IllegalStateException("La base de datos no existe en el almacenamiento interno.")
        }

        if (targetFile.exists()) {
            targetFile.delete()
        }

        val parent = targetFile.parentFile
        if (parent != null && !parent.exists()) {
            parent.mkdirs()
        }

        FileInputStream(source).use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
        }

        return targetFile
    }

    fun restoreDatabase(sourceFile: File) {
        if (!sourceFile.exists()) {
            throw IllegalStateException("El archivo de respaldo no existe.")
        }

        val destination = context.getDatabasePath(dbName)
        val parent = destination.parentFile
        if (parent != null && !parent.exists()) {
            parent.mkdirs()
        }

        // Cerrar base de datos antes de reemplazarla si está abierta
        val db = context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null)
        db.close()

        FileInputStream(sourceFile).use { input ->
            FileOutputStream(destination).use { output ->
                input.copyTo(output)
            }
        }
    }

    fun getBackupDir(): File {
        val dir = File(context.getExternalFilesDir(null), "custock_backups")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun createBackupFile(name: String = "custock_backup.db"): File {
        val dir = getBackupDir()
        return File(dir, name)
    }
}