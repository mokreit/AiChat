package com.aichat.data.ai.tool

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder

data class SearchResult(
    val title: String,
    val url: String,
    val snippet: String,
)

class WebSearchService(
    private val httpClient: HttpClient,
) {
    /**
     * Search via DuckDuckGo Lite (no API key needed).
     */
    suspend fun search(query: String, maxResults: Int = 5): List<SearchResult> {
        return withContext(Dispatchers.Default) {
            try {
                val encoded = URLEncoder.encode(query, "UTF-8")
                val html = httpClient.get("https://lite.duckduckgo.com/lite/") {
                    parameter("q", query)
                }.bodyAsText()
                parseDdgLiteResults(html, maxResults)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /**
     * Fetch and extract readable text from a web page.
     * Returns first ~8000 chars of clean text.
     */
    suspend fun extractContent(url: String, maxChars: Int = 8000): String {
        return withContext(Dispatchers.Default) {
            try {
                val html = httpClient.get(url).bodyAsText()
                val text = stripHtml(html)
                    .replace(Regex("\\s+"), " ")
                    .trim()
                if (text.length > maxChars) text.take(maxChars) + "\n... (truncated)" else text
            } catch (e: Exception) {
                "Failed to fetch content: ${e.message}"
            }
        }
    }

    /**
     * Full pipeline: search → extract top N results.
     */
    suspend fun searchAndExtract(query: String, maxResults: Int = 3): String {
        val results = search(query, maxResults)
        if (results.isEmpty()) return "No search results found."

        val sb = StringBuilder()
        sb.appendLine("Search results for: $query")
        sb.appendLine()

        for ((i, r) in results.withIndex()) {
            sb.appendLine("--- Result ${i + 1} ---")
            sb.appendLine("Title: ${r.title}")
            sb.appendLine("URL: ${r.url}")
            sb.appendLine("Snippet: ${r.snippet}")
            sb.appendLine()
            sb.appendLine("Content:")
            val content = extractContent(r.url)
            sb.appendLine(content.take(2000))
            sb.appendLine()
        }

        return sb.toString()
    }

    // ── DuckDuckGo Lite HTML parser ──

    private fun parseDdgLiteResults(html: String, max: Int): List<SearchResult> {
        val results = mutableListOf<SearchResult>()

        // DDG Lite structure: <a rel="nofollow" href="URL">TITLE</a>
        // followed by <td class="result-snippet">SNIPPET</td>
        val linkPattern = Regex(
            """<a\s+rel="nofollow"\s+href="([^"]+)"[^>]*>\s*([\s\S]*?)\s*</a>""",
        )
        val snippetPattern = Regex(
            """<td\s+class="result-snippet">([\s\S]*?)</td>""",
        )

        val links = linkPattern.findAll(html).toList()
        val snippets = snippetPattern.findAll(html).toList()

        for (i in links.indices) {
            if (results.size >= max) break
            val url = links[i].groupValues[1]
            val title = stripHtml(links[i].groupValues[2]).trim()
            val snippet = if (i < snippets.size) {
                stripHtml(snippets[i].groupValues[1]).trim()
            } else ""

            if (title.isNotBlank() && url.isNotBlank()) {
                results.add(SearchResult(title, url, snippet))
            }
        }

        return results
    }

    private fun stripHtml(html: String): String {
        return html
            .replace(Regex("<[^>]*>"), "")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")
    }
}
