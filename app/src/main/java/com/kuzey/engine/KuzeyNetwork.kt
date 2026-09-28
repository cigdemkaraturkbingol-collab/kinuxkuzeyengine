package com.kuzey.engine

import java.net.HttpURLConnection
import java.net.URL

object KuzeyNetwork {
    fun get(url: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 12000
        conn.readTimeout = 15000
        conn.instanceFollowRedirects = true
        conn.setRequestProperty("User-Agent", "KuzeyEngine/0.1 Android")
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml")
        conn.connect()
        val code = conn.responseCode
        val stream = if (code in 200..399) conn.inputStream else conn.errorStream
        return stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }
}
