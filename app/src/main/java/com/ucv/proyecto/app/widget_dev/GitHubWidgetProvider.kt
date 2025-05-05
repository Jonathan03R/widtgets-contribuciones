package com.ucv.proyecto.app.widget_dev

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.widget.RemoteViews
import com.caverock.androidsvg.SVG
import com.ucv.proyecto.app.widget_dev.R
import java.net.HttpURLConnection
import java.net.URL

/**
 * AppWidgetProvider que muestra el gráfico de contribuciones de GitHub
 * de un usuario dado, descargándolo como SVG o bitmap y recortando
 * el margen superior.
 */
class GitHubWidgetProvider : AppWidgetProvider() {

    /**
     * Se llama cuando es necesario actualizar uno o más widgets.
     *
     * @param context            Contexto de la aplicación.
     * @param appWidgetManager   Gestor de widgets.
     * @param appWidgetIds       IDs de los widgets a actualizar.
     */
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        val username = "Jonathan03R"
        val imageUrl = "https://ghchart.rshah.org/$username"

        // Para cada instancia de widget, inicia la carga y renderizado de la imagen
        for (widgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.github_widget)
            views.setTextViewText(R.id.debugText, "Actualizando…")
            loadImageIntoWidget(context, views, widgetId, imageUrl)
        }
    }

    /**
     * Descarga la imagen de GitHub (SVG o PNG/JPEG), la convierte a Bitmap,
     * recorta un margen superior y actualiza el RemoteViews del widget.
     *
     * @param context     Contexto de la aplicación.
     * @param views       RemoteViews del widget a actualizar.
     * @param widgetId    ID de la instancia del widget.
     * @param imageUrl    URL de la imagen a descargar.
     */
    private fun loadImageIntoWidget(
        context: Context,
        views: RemoteViews,
        widgetId: Int,
        imageUrl: String
    ) {
        val appWidgetManager = AppWidgetManager.getInstance(context)

        Thread {
            try {
                // Conexión HTTP a la URL de la imagen
                val connection = (URL(imageUrl).openConnection() as HttpURLConnection).apply {
                    doInput = true
                    connect()
                }

                // Determina el tipo de contenido devuelto
                val contentType = connection.contentType
                val input = connection.inputStream

                // Si es SVG, parsea con AndroidSVG; si no, decodifica como bitmap estándar
                val fullBitmap: Bitmap? = if (contentType.contains("svg")) {
                    val svg = SVG.getFromInputStream(input)
                    val picture = svg.renderToPicture()
                    Bitmap.createBitmap(picture.width, picture.height, Bitmap.Config.ARGB_8888)
                        .also { bmp ->
                            Canvas(bmp).drawPicture(picture)
                        }
                } else {
                    BitmapFactory.decodeStream(input)
                }

                if (fullBitmap != null) {
                    // Recorta un margen superior (opcional)
                    val cropStartY = 50
                    val croppedBitmap = Bitmap.createBitmap(
                        fullBitmap,
                        0,
                        cropStartY,
                        fullBitmap.width,
                        fullBitmap.height - cropStartY
                    )

                    // Pinta el bitmap en el ImageView del widget y actualiza
                    views.setImageViewBitmap(R.id.githubHeatmap, croppedBitmap)
                    views.setTextViewText(
                        R.id.debugText,
                        "Días de Contribución Jonathan Roque 2025"
                    )
                } else {
                    // En caso de fallo, muestra mensaje de error
                    views.setTextViewText(R.id.debugText, "Error al cargar imagen")
                }

                appWidgetManager.updateAppWidget(widgetId, views)

            } catch (e: Exception) {
                // Si hay error de red u otro, refleja el mensaje en el widget
                views.setTextViewText(R.id.debugText, "Error de red")
                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }.start()
    }
}
