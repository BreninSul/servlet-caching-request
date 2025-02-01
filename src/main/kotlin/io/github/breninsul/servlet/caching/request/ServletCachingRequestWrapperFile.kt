/*
 * MIT License
 *
 * Copyright (c) 2024 BreninSul
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.breninsul.servlet.caching.request

import io.github.breninsul.servlet.caching.ServletInputStreamDelegate
import jakarta.servlet.ServletInputStream
import jakarta.servlet.http.HttpServletRequest
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.deleteIfExists

open class ServletCachingRequestWrapperFile(
    protected open val request: HttpServletRequest,
) : ServletCachingRequestWrapper,
    HttpServletRequest by request {
    val tempFile: Path

    init {
        tempFile = kotlin.io.path.createTempFile("ServletCachingRequestWrapperFile_${request.requestId}_${UUID.randomUUID()}")
        request.inputStream.toFile(tempFile.toFile())
    }

    protected open fun InputStream.toFile(file: File) {
        use { input ->
            file.outputStream().use { input.copyTo(it) }
        }
    }

    override fun bodyContentByteArray(): ByteArray? = getInputStream().readAllBytes()

    override fun clear() {
        tempFile.deleteIfExists()
    }

    override fun getInputStream(): ServletInputStream {
        val inputStream = Files.newInputStream(tempFile)
        return ServletInputStreamDelegate(inputStream)
    }
}
