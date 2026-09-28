package com.kuzey.engine

data class Node(val tag:String, val text:String="", val attrs:Map<String,String> = emptyMap(), val children:MutableList<Node> = mutableListOf())

object HtmlParser {
    private val token = Regex("<[^>]+>|[^<]+")
    private val attr = Regex("""([a-zA-Z_:][-a-zA-Z0-9_:.]*)\s*=\s*(?:"([^"]*)"|'([^']*)'|([^\s>]+))""")

    fun parse(html:String): Node {
        val root = Node("document")
        val stack = mutableListOf(root)
        token.findAll(html).forEach { m ->
            val s = m.value
            if (!s.startsWith("<")) {
                val t = s.replace(Regex("\\s+"), " ")
                if (t.isNotBlank()) stack.last().children += Node("#text", t)
            } else if (s.startsWith("</")) {
                if (stack.size > 1) stack.removeAt(stack.lastIndex)
            } else if (!s.startsWith("<!") && !s.startsWith("<?")) {
                val inside = s.removePrefix("<").removeSuffix(">").trim()
                val name = inside.split(Regex("\\s+"), limit = 2)[0].lowercase().trimEnd('/')
                val attrs = attr.findAll(inside).associate {
                    val v = it.groups[2]?.value ?: it.groups[3]?.value ?: it.groups[4]?.value ?: ""
                    it.groups[1]!!.value.lowercase() to v
                }
                val n = Node(name, attrs=attrs)
                stack.last().children += n
                val self = inside.endsWith("/") || name in setOf("br","img","meta","input","hr","link")
                if (!self) stack += n
            }
        }
        return root
    }
}
