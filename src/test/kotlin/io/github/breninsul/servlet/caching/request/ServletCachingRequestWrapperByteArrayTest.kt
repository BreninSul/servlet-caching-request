package io.github.breninsul.servlet.caching.request

import jakarta.servlet.ServletInputStream
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.mock.web.DelegatingServletInputStream
import java.io.ByteArrayInputStream

class ServletCachingRequestWrapperByteArrayTest {

    @Test
    fun `getBody should return the correct byte array from the request input stream`() {
        val testData = "test data".toByteArray()
        val mockRequest = Mockito.mock(HttpServletRequest::class.java)
        Mockito.`when`(mockRequest.inputStream).thenReturn(DelegatingServletInputStream(ByteArrayInputStream(testData)))

        val wrapper = ServletCachingRequestWrapperByteArray(mockRequest)
        val body = wrapper.bodyContentByteArray()

        assertArrayEquals(testData, body)
    }

    @Test
    fun `bodyContentByteArray should return the correct byte array`() {
        val testData = "test content".toByteArray()
        val mockRequest = Mockito.mock(HttpServletRequest::class.java)
        Mockito.`when`(mockRequest.inputStream).thenReturn(DelegatingServletInputStream(ByteArrayInputStream(testData)))

        val wrapper = ServletCachingRequestWrapperByteArray(mockRequest)
        val bodyContent = wrapper.bodyContentByteArray()

        assertArrayEquals(testData, bodyContent)
    }

    @Test
    fun `clear should reset the body to an empty byte array`() {
        val testData = "clear test".toByteArray()
        val mockRequest = Mockito.mock(HttpServletRequest::class.java)
        Mockito.`when`(mockRequest.inputStream).thenReturn(DelegatingServletInputStream(ByteArrayInputStream(testData)))

        val wrapper = ServletCachingRequestWrapperByteArray(mockRequest)
        wrapper.clear()
        val clearedBody = wrapper.bodyContentByteArray()

        assertArrayEquals(ByteArray(0), clearedBody)
    }

    @Test
    fun `getInputStream should return a ServletInputStream for the cached body`() {
        val testData = "stream test".toByteArray()
        val mockRequest = Mockito.mock(HttpServletRequest::class.java)
        Mockito.`when`(mockRequest.inputStream).thenReturn(DelegatingServletInputStream(ByteArrayInputStream(testData)))

        val wrapper = ServletCachingRequestWrapperByteArray(mockRequest)
        val inputStream: ServletInputStream = wrapper.getInputStream()
        assertArrayEquals(testData, inputStream.use {  it.readAllBytes()})
        wrapper.reInitInputStream()
        assertArrayEquals(testData, inputStream.use {  it.readAllBytes()})

    }
}