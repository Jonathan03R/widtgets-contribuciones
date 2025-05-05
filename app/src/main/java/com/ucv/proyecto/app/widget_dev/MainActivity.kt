package com.ucv.proyecto.app.widget_dev

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.util.Log
import android.widget.Button

import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Ajuste de márgenes por sistema de barras
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Botón que fuerza la actualización del widget
        findViewById<Button>(R.id.updateButton).setOnClickListener {
            val appWidgetManager = AppWidgetManager.getInstance(this)
            val thisWidget = ComponentName(this, GitHubWidgetProvider::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            if (widgetIds.isNotEmpty()) {
                GitHubWidgetProvider().onUpdate(this, appWidgetManager, widgetIds)
            }
        }
    }

}