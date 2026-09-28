package com.kuzey.engine

import android.content.Context
import android.graphics.*
import android.view.*
import kotlin.math.max

class BrowserView(context: Context): View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var lines: List<Pair<String,Float>> = listOf("KuzeyEngine" to 32f, "Bir adres yaz ve Git'e bas." to 18f)
    var currentUrl = "kuzey://home"
    var onLink: ((String)->Unit)? = null

    fun showDocument(root: Node) {
        val out = mutableListOf<Pair<String,Float>>()
        fun walk(n:Node, inherited:Float=18f) {
            val size = when(n.tag) {
                "h1" -> 32f; "h2" -> 26f; "h3" -> 22f; "small" -> 14f; else -> inherited
            }
            if (n.tag=="#text") out += n.text.trim() to size
            n.children.forEach { walk(it,size) }
        }
        walk(root)
        lines = out.filter { it.first.isNotBlank() }.take(500)
        invalidate()
    }

    fun showError(msg:String) {
        lines = listOf("KuzeyEngine hata" to 28f, msg to 17f)
        invalidate()
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        c.drawColor(Color.WHITE)
        var y = 34f
        val left = 20f
        lines.forEach { (text,size) ->
            paint.color = Color.rgb(32,33,36)
            paint.textSize = size * resources.displayMetrics.scaledDensity
            paint.typeface = if(size>=26) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            val maxWidth = width - 40f
            val words = text.split(" ")
            var line = ""
            for (w in words) {
                val test = if(line.isEmpty()) w else "$line $w"
                if(paint.measureText(test) > maxWidth && line.isNotEmpty()) {
                    c.drawText(line,left,y,paint)
                    y += paint.fontSpacing
                    line = w
                } else line = test
            }
            if(line.isNotEmpty()) {
                c.drawText(line,left,y,paint)
                y += max(paint.fontSpacing, 24f)
            }
            y += 8f
        }
    }
}
