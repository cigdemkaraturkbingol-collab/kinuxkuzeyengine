package com.kuzey.engine

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*

class MainActivity: Activity() {
    private lateinit var address: EditText
    private lateinit var browser: BrowserView
    private val history = mutableListOf<String>()
    private var histPos = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }

        val warning = TextView(this).apply {
            text = "DİKKAT: BU TARAYICI EKİPLERLE YAPILMAMIŞTIR, HATA OLABİLİR"
            textSize = 12f
            setTextColor(Color.rgb(90,70,0))
            setBackgroundColor(Color.rgb(255,243,205))
            gravity = Gravity.CENTER
            setPadding(8,10,8,10)
        }
        root.addView(warning, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(6,6,6,6)
        }

        fun btn(t:String, action:()->Unit)=Button(this).apply {
            text=t
            setOnClickListener { action() }
        }

        bar.addView(btn("‹"){ back() })
        bar.addView(btn("›"){ forward() })
        bar.addView(btn("↻"){ load(address.text.toString(), false) })

        address = EditText(this).apply {
            setSingleLine(true)
            hint = "Ara veya adres yaz"
            textSize = 15f
            setOnEditorActionListener { _,_,_ -> load(text.toString(), true); true }
        }
        bar.addView(address, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        bar.addView(btn("Git"){ load(address.text.toString(), true) })
        root.addView(bar)

        browser = BrowserView(this)
        root.addView(browser, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1f))

        setContentView(root)
        home()
    }

    private fun home() {
        address.setText("kuzey://home")
        val html = """
            <h1>KuzeyEngine</h1>
            <p>Kendi ağ katmanımız, HTML parserımız ve Canvas rendererımız.</p>
            <h2>Başlangıç</h2>
            <p>Yukarıya bir HTTPS adresi yaz.</p>
            <p>Örnek: https://example.com</p>
        """.trimIndent()
        browser.showDocument(HtmlParser.parse(html))
    }

    private fun normalize(input:String):String {
        val s=input.trim()
        if(s=="kuzey://home") return s
        return if(s.startsWith("http://")||s.startsWith("https://")) s
        else "https://www.google.com/search?q=" + java.net.URLEncoder.encode(s,"UTF-8")
    }

    private fun load(raw:String, addHistory:Boolean) {
        val url=normalize(raw)
        address.setText(url)
        if(url=="kuzey://home") { home(); return }
        if(addHistory) {
            while(history.size>histPos+1) history.removeAt(history.lastIndex)
            history += url; histPos = history.lastIndex
        }
        browser.showError("Yükleniyor: $url")
        Thread {
            try {
                val html = KuzeyNetwork.get(url)
                val doc = HtmlParser.parse(html)
                runOnUiThread { browser.showDocument(doc) }
            } catch(e:Exception) {
                runOnUiThread { browser.showError(e.javaClass.simpleName + ": " + (e.message ?: "Bilinmeyen hata")) }
            }
        }.start()
    }

    private fun back() {
        if(histPos>0) { histPos--; load(history[histPos], false) }
        else home()
    }
    private fun forward() {
        if(histPos<history.lastIndex) { histPos++; load(history[histPos], false) }
    }
}
