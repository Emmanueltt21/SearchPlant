package com.kottland.searchplant.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.IOException
import java.io.InputStream

/**
 * Utility class for camera and image operations
 */
object CameraUtils {
    
    /**
     * Convert URI to Bitmap with proper orientation
     */
    fun uriToBitmap(context: Context, uri: Uri): Bitmap {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        
        return rotateImageIfRequired(context, bitmap, uri)
    }
    
    /**
     * Rotate image based on EXIF orientation data
     */
    private fun rotateImageIfRequired(context: Context, img: Bitmap, selectedImage: Uri): Bitmap {
        val input: InputStream? = context.contentResolver.openInputStream(selectedImage)
        val ei: ExifInterface?
        
        try {
            ei = if (input != null) {
                ExifInterface(input)
            } else {
                return img
            }
            
            return when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270f)
                else -> img
            }
        } catch (e: IOException) {
            return img
        } finally {
            input?.close()
        }
    }
    
    /**
     * Rotate bitmap by specified degrees
     */
    private fun rotateImage(img: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }
    
    /**
     * Resize bitmap to maximum dimensions while maintaining aspect ratio
     */
    fun resizeBitmap(bitmap: Bitmap, maxWidth: Int = 1024, maxHeight: Int = 1024): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }
        
        val aspectRatio = width.toFloat() / height.toFloat()
        
        val (newWidth, newHeight) = if (aspectRatio > 1) {
            // Landscape
            maxWidth to (maxWidth / aspectRatio).toInt()
        } else {
            // Portrait
            (maxHeight * aspectRatio).toInt() to maxHeight
        }
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    /**
     * Compress bitmap to reduce file size
     */
    fun compressBitmap(bitmap: Bitmap, quality: Int = 85): Bitmap {
        // For now, just return the original bitmap
        // In a real implementation, you might want to compress to JPEG and back
        return bitmap
    }
    
    /**
     * Create a square crop of the bitmap from the center
     */
    fun cropToSquare(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2
        
        return Bitmap.createBitmap(bitmap, x, y, size, size)
    }
    
    /**
     * Check if the bitmap is valid for processing
     */
    fun isValidBitmap(bitmap: Bitmap?): Boolean {
        return bitmap != null && !bitmap.isRecycled && bitmap.width > 0 && bitmap.height > 0
    }
    
    /**
     * Get optimal bitmap size for API processing
     */
    fun getOptimalBitmapForApi(bitmap: Bitmap): Bitmap {
        var processedBitmap = bitmap
        
        // Resize if too large
        processedBitmap = resizeBitmap(processedBitmap, 1024, 1024)
        
        // Compress to reduce memory usage
        processedBitmap = compressBitmap(processedBitmap, 85)
        
        return processedBitmap
    }
}