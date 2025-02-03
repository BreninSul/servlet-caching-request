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

import io.github.breninsul.servlet.caching.io.ServletInputStreamDelegate
import io.github.breninsul.servlet.caching.exception.InputStreamReadAlreadyStartedException
import io.github.breninsul.servlet.caching.exception.ReadInitiationIsNotStartedException
import jakarta.servlet.ServletInputStream
import jakarta.servlet.http.HttpServletRequest

/**
 * A wrapper for `HttpServletRequest` that caches the request body internally as a byte array.
 * This allows repeated access to the request body without re-reading the input stream.
 *
 * This class is useful when the request body needs to be consumed multiple times, such as in
 * scenarios with filters or interceptors that must inspect the content without blocking downstream processing.
 *
 * The caching is performed by reading the input stream into a byte array during initialization or
 * on-demand depending on the `initReadAtStart` parameter.
 *
 * @property request the original `HttpServletRequest` being wrapped for caching functionality.
 * @property initReadAtStart flag indicating whether the input stream should be read immediately at the time of construction.
 * By default, this is set to `true`.
 *
 * @constructor Initializes this wrapper around the given `HttpServletRequest`,
 * reading the input stream immediately if `initReadAtStart` is set to `true`.
 *
 * Inherits from `ServletCachingRequestWrapper` for consistent caching methods and behavior,
 * and delegates unmodified `HttpServletRequest` methods to the original request instance.
 */
open class ServletCachingRequestWrapperByteArray(
    protected open val request: HttpServletRequest,
    protected open val initReadAtStart: Boolean = true
) : ServletCachingRequestWrapper,
    HttpServletRequest by request {
    constructor(request: HttpServletRequest) : this(request, true)
    protected open var bodyValue: ByteArray? = null
    protected open var wrappedInputStream: ServletInputStreamDelegate = ServletInputStreamDelegate(request.inputStream)

    init {
        if (initReadAtStart){
            initRead()
        }
    }

    override fun initRead() {
        if (wrappedInputStream.isStarted){
            throw InputStreamReadAlreadyStartedException()
        }
        bodyValue = request.inputStream.use {  it.readAllBytes()}
        reInitInputStream()
    }

    override fun readIsInited(): Boolean =bodyValue!=null

    override fun bodyContentByteArray(): ByteArray {
        if (!readIsInited()){
            throw ReadInitiationIsNotStartedException()
        }
        return bodyValue?:throw ReadInitiationIsNotStartedException()
    }

    override fun clear() {
        bodyValue = ByteArray(0)
    }

    override fun reInitInputStream() {
        wrappedInputStream = ServletInputStreamDelegate((bodyValue?:throw ReadInitiationIsNotStartedException()).inputStream())
    }

    override fun getInputStream(): ServletInputStream = wrappedInputStream

    override fun getContentLength(): Int {
        return bodyValue?.size?:request.contentLength
    }
    override fun getContentLengthLong(): Long {
        return bodyValue?.size?.toLong()?:request.contentLengthLong
    }
}
