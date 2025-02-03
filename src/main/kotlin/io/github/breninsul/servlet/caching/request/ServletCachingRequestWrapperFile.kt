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

import io.github.breninsul.servlet.caching.exception.InputStreamReadAlreadyStartedException
import io.github.breninsul.servlet.caching.io.ServletInputStreamDelegate
import jakarta.servlet.ServletInputStream
import jakarta.servlet.http.HttpServletRequest
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.deleteIfExists

/**
 * A wrapper for `HttpServletRequest` that caches the request body in a temporary file.
 * This class allows multiple readings of the request body by storing the input stream content
 * into a temporary file that persists until the `clear` function is called.
 *
 * This is particularly useful in scenarios where middleware or applications need to reuse
 * the request data without consuming the input stream multiple times.
 *
 * It inherits from the `ServletCachingRequestWrapper` interface for consistent behavior and
 * delegates unmodified `HttpServletRequest` methods to the original request instance.
 *
 * @property request the original `HttpServletRequest` being wrapped for caching.
 * @property initReadAtStart a flag to indicate whether the request body should be read and cached
 * immediately during initialization. Defaults to `true`.
 *
 * @constructor Initializes the wrapper around the given `HttpServletRequest`. If `initReadAtStart`
 * is `true`, the request body will be cached during construction.
 */
open class ServletCachingRequestWrapperFile(
    protected open val request: HttpServletRequest,
    protected open val initReadAtStart: Boolean = true
) : ServletCachingRequestWrapper,
    HttpServletRequest by request {
        open var tempFile: Path? = null
    protected open var wrappedInputStream: ServletInputStreamDelegate = ServletInputStreamDelegate(request.inputStream)
    init {
        if (initReadAtStart){
            initRead()
        }
    }


    protected open fun InputStream.toFile(file: File) {
        use { input ->
            file.outputStream().use { input.copyTo(it) }
        }
    }

    override fun initRead() {
        if (wrappedInputStream.isStarted) {
            throw InputStreamReadAlreadyStartedException()
        }
        tempFile = kotlin.io.path.createTempFile("ServletCachingRequestWrapperFile_${request.requestId}_${UUID.randomUUID()}")
        wrappedInputStream.use { it.toFile(tempFile!!.toFile()) }
        reInitInputStream()
    }


    override fun bodyContentByteArray(): ByteArray = getInputStream().readAllBytes()

    override fun clear() {
        tempFile?.deleteIfExists()
    }

    override fun reInitInputStream() {
        wrappedInputStream = ServletInputStreamDelegate(Files.newInputStream(tempFile))
    }

    override fun getInputStream(): ServletInputStream = wrappedInputStream
}
