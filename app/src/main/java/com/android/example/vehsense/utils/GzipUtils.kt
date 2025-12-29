package com.android.example.vehsense.utils

import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

fun ByteArray.gzip(): ByteArray {
    val bos = ByteArrayOutputStream()
    GZIPOutputStream(bos).use { it.write(this) }
    return bos.toByteArray()
}

fun String.gzip(): ByteArray = this.toByteArray(Charsets.UTF_8).gzip()
