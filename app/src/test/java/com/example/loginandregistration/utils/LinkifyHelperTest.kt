package com.example.loginandregistration.utils

import org.junit.Assert.*
import org.junit.Test

/** Unit tests for LinkifyHelper utility class. Tests URL detection and extraction logic. */
class LinkifyHelperTest {

    @Test
    fun `detectLinks finds http URL`() {
        val text = "Check out http://example.com for more info"
        val links = LinkifyHelper.detectLinks(text)

        assertEquals(1, links.size)
        assertTrue(links[0].url.contains("example.com"))
    }

    @Test
    fun `detectLinks finds https URL`() {
        val text = "Visit https://example.com"
        val links = LinkifyHelper.detectLinks(text)

        assertEquals(1, links.size)
        assertTrue(links[0].url.contains("example.com"))
    }

    @Test
    fun `detectLinks finds www URL`() {
        val text = "Go to www.example.com"
        val links = LinkifyHelper.detectLinks(text)

        assertEquals(1, links.size)
        assertTrue(links[0].url.contains("example.com"))
    }

    @Test
    fun `detectLinks finds multiple URLs`() {
        val text = "Check http://example.com and https://google.com"
        val links = LinkifyHelper.detectLinks(text)

        assertEquals(2, links.size)
    }

    @Test
    fun `detectLinks returns empty list for text without URLs`() {
        val text = "This is just plain text without any links"
        val links = LinkifyHelper.detectLinks(text)

        assertTrue(links.isEmpty())
    }

    @Test
    fun `detectLinks finds URL with path`() {
        val text = "Visit https://example.com/path/to/page"
        val links = LinkifyHelper.detectLinks(text)

        assertEquals(1, links.size)
        assertTrue(links[0].url.contains("/path/to/page"))
    }

    @Test
    fun `detectLinks finds URL with query parameters`() {
        val text = "Search https://example.com/search?q=test&page=1"
        val links = LinkifyHelper.detectLinks(text)

        assertEquals(1, links.size)
        assertTrue(links[0].url.contains("search?q="))
    }

    @Test
    fun `detectLinks finds URL with fragment`() {
        val text = "Jump to https://example.com/page#section"
        val links = LinkifyHelper.detectLinks(text)

        assertEquals(1, links.size)
        assertTrue(links[0].url.contains("#section"))
    }

    @Test
    fun `detectLinks handles URL at start of text`() {
        val text = "https://example.com is a great site"
        val links = LinkifyHelper.detectLinks(text)

        assertEquals(1, links.size)
    }

    @Test
    fun `detectLinks handles URL at end of text`() {
        val text = "Visit https://example.com"
        val links = LinkifyHelper.detectLinks(text)

        assertEquals(1, links.size)
    }

    @Test
    fun `detectLinks handles URL with subdomain`() {
        val text = "Check https://blog.example.com"
        val links = LinkifyHelper.detectLinks(text)

        assertEquals(1, links.size)
        assertTrue(links[0].url.contains("blog.example.com"))
    }

    @Test
    fun `detectLinks handles URL with port`() {
        val text = "Connect to http://localhost:8080"
        val links = LinkifyHelper.detectLinks(text)

        assertEquals(1, links.size)
        assertTrue(links[0].url.contains("8080"))
    }

    @Test
    fun `containsUrl returns true when URL present`() {
        val text = "Check out https://example.com"
        assertTrue(LinkifyHelper.containsUrl(text))
    }

    @Test
    fun `containsUrl returns false when no URL present`() {
        val text = "Just plain text"
        assertFalse(LinkifyHelper.containsUrl(text))
    }

    @Test
    fun `extractUrls returns list of URL strings`() {
        val text = "Visit https://example.com and http://google.com"
        val urls = LinkifyHelper.extractUrls(text)

        assertEquals(2, urls.size)
        assertTrue(urls.any { it.contains("example.com") })
        assertTrue(urls.any { it.contains("google.com") })
    }

    @Test
    fun `extractUrls returns empty list for text without URLs`() {
        val text = "No links here"
        val urls = LinkifyHelper.extractUrls(text)

        assertTrue(urls.isEmpty())
    }

    @Test
    fun `detectLinks handles URL with special characters in path`() {
        val text = "Download https://example.com/file-name_v2.0.pdf"
        val links = LinkifyHelper.detectLinks(text)

        assertEquals(1, links.size)
        assertTrue(links[0].url.contains("file-name_v2.0.pdf"))
    }

    @Test
    fun `detectLinks handles multiple URLs in same sentence`() {
        val text = "Compare https://site1.com with https://site2.com and www.site3.com"
        val links = LinkifyHelper.detectLinks(text)

        assertEquals(3, links.size)
    }

    @Test
    fun `detectLinks provides correct start and end positions`() {
        val text = "Visit https://example.com today"
        val links = LinkifyHelper.detectLinks(text)

        assertEquals(1, links.size)
        val link = links[0]
        assertTrue(link.start >= 0)
        assertTrue(link.end > link.start)
        assertTrue(link.end <= text.length)
    }

    @Test
    fun `detectLinks handles empty string`() {
        val links = LinkifyHelper.detectLinks("")
        assertTrue(links.isEmpty())
    }

    @Test
    fun `detectLinks handles URL with uppercase letters`() {
        val text = "Visit HTTPS://EXAMPLE.COM"
        val links = LinkifyHelper.detectLinks(text)

        assertEquals(1, links.size)
    }
}
