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

package io.github.breninsul.servlet.caching.io

import jakarta.servlet.ReadListener
import jakarta.servlet.ServletInputStream
import java.io.InputStream

open class ServletInputStreamDelegate(protected open val delegate: InputStream, protected open val onCloseFunction: Function<Unit> = {}) : ServletInputStream() {
    protected var isFinishedValue = false
        private set
    protected var isStartedValue = false
    val isStarted: Boolean
        get() = isFinishedValue

    override fun read(): Int {
        isStartedValue = true
        val data = this.delegate.read()
        if (data == -1) {
            this.isFinishedValue = true
        }
        return data
    }

    override fun available(): Int {
        return this.delegate.available()
    }

    override fun close() {
        this.delegate.close()
        this.isFinishedValue = true
        onCloseFunction.apply { }
    }

    override fun isFinished(): Boolean {
        return this.isFinishedValue
    }

    override fun isReady(): Boolean {
        return (delegate as? ServletInputStream)?.isReady ?: true;
    }

    override fun setReadListener(readListener: ReadListener) {
        return (delegate as? ServletInputStream)?.setReadListener(readListener) ?: throw UnsupportedOperationException()
    }
}