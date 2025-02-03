package io.github.breninsul.servlet.caching.request

import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.mock.web.DelegatingServletInputStream
import java.io.ByteArrayInputStream
import java.nio.file.Files

class ServletCachingRequestWrapperFileTest {

    /**
     * Test class for ServletCachingRequestWrapperFile.
     *
     * Testing the functionality of creating, accessing, and clearing the
     * temporary file, which caches the request input stream for efficient
     * reuse.
     */

    @Test
    fun `test tempFile is created on initialization`() {
        val mockRequest = mock(HttpServletRequest::class.java)
        val testInputStream = "test data".byteInputStream()
        `when`(mockRequest.inputStream).thenReturn(createMockServletInputStream(testInputStream))
        `when`(mockRequest.requestId).thenReturn("12345")

        val wrapper = ServletCachingRequestWrapperFile(mockRequest)

        assertTrue(Files.exists(wrapper.tempFile))
        assertTrue(Files.size(wrapper.tempFile) > 0)

        wrapper.clear()
    }

    @Test
    fun `test bodyContentByteArray returns correct data`() {
        val mockRequest = mock(HttpServletRequest::class.java)
        val testData = "test data".toByteArray()
        val testInputStream = ByteArrayInputStream(testData)
        `when`(mockRequest.inputStream).thenReturn(createMockServletInputStream(testInputStream))
        `when`(mockRequest.requestId).thenReturn("12345")

        val wrapper = ServletCachingRequestWrapperFile(mockRequest)
        val result = wrapper.bodyContentByteArray()

        assertNotNull(result)
        assertArrayEquals(testData, result)

        wrapper.clear()
    }

    @Test
    fun `test getInputStream returns an input stream from temp file`() {
        val mockRequest = mock(HttpServletRequest::class.java)
        val testData = "test data".toByteArray()
        val testInputStream = ByteArrayInputStream(testData)
        `when`(mockRequest.inputStream).thenReturn(createMockServletInputStream(testInputStream))
        `when`(mockRequest.requestId).thenReturn("12345")

        val wrapper = ServletCachingRequestWrapperFile(mockRequest)
        assertNotNull(wrapper.inputStream)
        assertArrayEquals(testData, wrapper.inputStream.use { it.readAllBytes() })
        wrapper.reInitInputStream()
        assertArrayEquals(testData, wrapper.inputStream.use { it.readAllBytes() })
        wrapper.clear()
    }

    @Test
    fun `test clear deletes temp file`() {
        val mockRequest = mock(HttpServletRequest::class.java)
        val testInputStream = "test data".byteInputStream()
        `when`(mockRequest.inputStream).thenReturn(createMockServletInputStream(testInputStream))
        `when`(mockRequest.requestId).thenReturn("12345")

        val wrapper = ServletCachingRequestWrapperFile(mockRequest)
        val tempFilePath = wrapper.tempFile

        assertTrue(Files.exists(tempFilePath))

        wrapper.clear()

        assertFalse(Files.exists(tempFilePath))
    }

    private fun createMockServletInputStream(inputStream: ByteArrayInputStream) = DelegatingServletInputStream(inputStream)
}