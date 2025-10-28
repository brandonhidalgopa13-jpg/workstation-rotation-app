package com.workstation.rotation.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 📷 UTILIDADES DE IMAGEN - CAPTURA Y DESCARGA DE VISTAS
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 * 
 * 🎯 FUNCIONES PRINCIPALES:
 * 
 * 📸 1. CAPTURA DE VISTAS
 *    - Convierte cualquier View a Bitmap de alta calidad
 *    - Maneja vistas con scroll y contenido dinámico
 *    - Optimiza la calidad de imagen para impresión
 * 
 * 💾 2. GUARDADO DE IMÁGENES
 *    - Guarda en galería del dispositivo automáticamente
 *    - Compatible con Android 10+ (Scoped Storage)
 *    - Nombres de archivo únicos con timestamp
 * 
 * 📤 3. COMPARTIR IMÁGENES
 *    - Genera URI para compartir con otras apps
 *    - Compatible con FileProvider para seguridad
 *    - Soporte para email, WhatsApp, etc.
 * 
 * ═══════════════════════════════════════════════════════════════════════════════════════════════
 */
object ImageUtils {
    
    /**
     * Captura una vista como Bitmap de alta calidad.
     * 
     * @param view Vista a capturar
     * @param backgroundColor Color de fondo (por defecto blanco)
     * @return Bitmap de la vista capturada
     */
    fun captureView(view: View, backgroundColor: Int = Color.WHITE): Bitmap {
        // Asegurar que la vista esté medida y posicionada
        view.measure(
            View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(view.height, View.MeasureSpec.EXACTLY)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        
        // Crear bitmap con alta calidad
        val bitmap = Bitmap.createBitmap(
            view.width,
            view.height,
            Bitmap.Config.ARGB_8888
        )
        
        // Dibujar la vista en el canvas
        val canvas = Canvas(bitmap)
        canvas.drawColor(backgroundColor)
        view.draw(canvas)
        
        return bitmap
    }
    
    /**
     * Captura una vista scrolleable completa como Bitmap.
     * 
     * @param scrollView Vista scrolleable a capturar
     * @param backgroundColor Color de fondo
     * @return Bitmap de todo el contenido scrolleable
     */
    fun captureScrollView(scrollView: View, backgroundColor: Int = Color.WHITE): Bitmap {
        val totalHeight = scrollView.getChildAt(0).height
        val totalWidth = scrollView.width
        
        // Crear bitmap del tamaño completo del contenido
        val bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(backgroundColor)
        
        // Dibujar todo el contenido
        scrollView.draw(canvas)
        
        return bitmap
    }
    
    /**
     * Guarda un bitmap en la galería del dispositivo.
     * 
     * @param context Contexto de la aplicación
     * @param bitmap Bitmap a guardar
     * @param filename Nombre base del archivo (sin extensión)
     * @return URI del archivo guardado o null si falló
     */
    suspend fun saveBitmapToGallery(
        context: Context,
        bitmap: Bitmap,
        filename: String = "rotacion"
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val displayName = "${filename}_$timestamp.png"
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ - Usar MediaStore
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/RotacionInteligente")
                }
                
                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                
                uri?.let { imageUri ->
                    context.contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                    imageUri
                }
            } else {
                // Android 9 y anteriores - Usar almacenamiento externo
                val picturesDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "RotacionInteligente"
                )
                
                if (!picturesDir.exists()) {
                    picturesDir.mkdirs()
                }
                
                val imageFile = File(picturesDir, displayName)
                FileOutputStream(imageFile).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
                
                // Notificar a la galería
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DATA, imageFile.absolutePath)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                }
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Guarda un bitmap en el almacenamiento interno de la app para compartir.
     * 
     * @param context Contexto de la aplicación
     * @param bitmap Bitmap a guardar
     * @param filename Nombre del archivo
     * @return URI del archivo para compartir
     */
    suspend fun saveBitmapForSharing(
        context: Context,
        bitmap: Bitmap,
        filename: String = "rotacion_temp"
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "${filename}_$timestamp.png"
            
            // Crear directorio temporal
            val imagesDir = File(context.cacheDir, "images")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }
            
            val imageFile = File(imagesDir, fileName)
            FileOutputStream(imageFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            
            // Generar URI con FileProvider
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Muestra un mensaje de éxito al usuario.
     * 
     * @param context Contexto de la aplicación
     * @param message Mensaje a mostrar
     */
    fun showSuccessMessage(context: Context, message: String) {
        Toast.makeText(context, "✅ $message", Toast.LENGTH_LONG).show()
    }
    
    /**
     * Muestra un mensaje de error al usuario.
     * 
     * @param context Contexto de la aplicación
     * @param message Mensaje de error
     */
    fun showErrorMessage(context: Context, message: String) {
        Toast.makeText(context, "❌ $message", Toast.LENGTH_LONG).show()
    }
    
    /**
     * Genera un nombre de archivo único para rotaciones.
     * 
     * @param prefix Prefijo del nombre
     * @return Nombre de archivo único
     */
    fun generateRotationFilename(prefix: String = "rotacion"): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "${prefix}_inteligente_$timestamp"
    }
    
    /**
     * Calcula el tamaño óptimo para la imagen según el contenido.
     * 
     * @param view Vista a analizar
     * @return Par de ancho y alto óptimos
     */
    fun calculateOptimalSize(view: View): Pair<Int, Int> {
        val maxWidth = 2048 // Máximo ancho para evitar OutOfMemoryError
        val maxHeight = 2048 // Máximo alto
        
        val originalWidth = view.width
        val originalHeight = view.height
        
        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return Pair(originalWidth, originalHeight)
        }
        
        // Calcular escala manteniendo proporción
        val scaleWidth = maxWidth.toFloat() / originalWidth
        val scaleHeight = maxHeight.toFloat() / originalHeight
        val scale = minOf(scaleWidth, scaleHeight)
        
        return Pair(
            (originalWidth * scale).toInt(),
            (originalHeight * scale).toInt()
        )
    }
}