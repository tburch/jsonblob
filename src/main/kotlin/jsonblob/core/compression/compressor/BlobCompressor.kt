package jsonblob.core.compression.compressor

import java.io.InputStream
import java.io.OutputStream

interface BlobCompressor {
    fun handles(fileExtension: String) : Boolean
    fun getFileExtension() : String
    fun getInputStream(inputStream: InputStream) : InputStream
    fun getOutputStream(outputStream: OutputStream) : OutputStream
}