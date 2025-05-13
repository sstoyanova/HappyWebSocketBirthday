package com.nanit.happywebsocketbirthday.presentation.utils // Replace with your actual package name

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

object FileProviderUtils {
    // Function to create a temporary file for the camera output
    fun createTempImageFile(context: Context): File? {
        // Create an image file name
        val timestamp = System.currentTimeMillis()
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return try {
            File.createTempFile(
                "JPEG_${timestamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
            )
        } catch (e: IOException) {
            Log.e("FileProviderUtils", "Error creating temporary image file", e)
            null
        }
    }

    // Function to get a content:// Uri for a file using FileProvider
    fun getUriForFile(context: Context, file: File): Uri? {
        return try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider", // Make sure this matches the authority in your Manifest
                file
            )
        } catch (e: IllegalArgumentException) {
            Log.e("FileProviderUtils", "The selected file can't be shared", e)
            null
        }
    }
}