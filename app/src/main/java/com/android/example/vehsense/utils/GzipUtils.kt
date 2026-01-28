package com.android.example.vehsense.utils

import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

fun ByteArray.gzip(): ByteArray {
    val bos = ByteArrayOutputStream()
    GZIPOutputStream(bos).use { it.write(this) }
    return bos.toByteArray()
}

fun String.gzip(): ByteArray = this.toByteArray(Charsets.UTF_8).gzip()

fun decodeBase64Gzip(input: String): String {
    val decodedBytes = android.util.Base64.decode(input, android.util.Base64.DEFAULT)

    GZIPInputStream(decodedBytes.inputStream()).use { gzip ->
        return gzip.bufferedReader(Charsets.UTF_8).readText()
    }
}
