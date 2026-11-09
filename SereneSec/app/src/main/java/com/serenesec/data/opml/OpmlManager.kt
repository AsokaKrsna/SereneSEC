package com.serenesec.data.opml

import android.util.Xml
import com.serenesec.domain.model.Category
import com.serenesec.domain.model.Source
import com.serenesec.domain.model.SourceType
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlSerializer
import java.io.InputStream
import java.io.StringWriter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

data class OpmlImportResult(
    val sources: List<Source>,
    val totalFound: Int
)

@Singleton
class OpmlManager @Inject constructor() {

    /**
     * Export a list of Sources into standard OPML 2.0 XML string
     */
    fun exportToOpml(sources: List<Source>): String {
        val serializer: XmlSerializer = Xml.newSerializer()
        val writer = StringWriter()
        serializer.setOutput(writer)
        serializer.startDocument("UTF-8", true)
        
        serializer.startTag("", "opml")
        serializer.attribute("", "version", "2.0")

        // Head
        serializer.startTag("", "head")
        serializer.startTag("", "title")
        serializer.text("SereneSec Feeds Export")
        serializer.endTag("", "title")
        serializer.endTag("", "head")

        // Body
        serializer.startTag("", "body")

        // Group sources by category
        val grouped = sources.groupBy { it.category }
        for ((category, categorySources) in grouped) {
            serializer.startTag("", "outline")
            serializer.attribute("", "text", category.name)
            serializer.attribute("", "title", category.name)

            for (source in categorySources) {
                serializer.startTag("", "outline")
                serializer.attribute("", "type", if (source.type == SourceType.GITHUB) "rss" else source.type.name.lowercase())
                serializer.attribute("", "text", source.name)
                serializer.attribute("", "title", source.name)
                serializer.attribute("", "xmlUrl", source.url)
                serializer.attribute("", "htmlUrl", source.url)
                serializer.endTag("", "outline")
            }

            serializer.endTag("", "outline")
        }

        serializer.endTag("", "body")
        serializer.endTag("", "opml")
        serializer.endDocument()

        return writer.toString()
    }

    /**
     * Parse OPML XML from an input stream into a list of Sources
     */
    fun importFromOpml(inputStream: InputStream): OpmlImportResult {
        val parser: XmlPullParser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)

        val sources = mutableListOf<Source>()
        var currentCategory: Category = Category.NEWS

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name.equals("outline", ignoreCase = true)) {
                val xmlUrl = parser.getAttributeValue(null, "xmlUrl")
                val text = parser.getAttributeValue(null, "text") ?: parser.getAttributeValue(null, "title") ?: "Unnamed Source"
                
                if (xmlUrl != null && xmlUrl.isNotBlank()) {
                    // This outline is a feed entry
                    val type = if (xmlUrl.contains("github.com", ignoreCase = true) && xmlUrl.contains("releases.atom")) {
                        SourceType.GITHUB
                    } else if (xmlUrl.contains("atom", ignoreCase = true)) {
                        SourceType.ATOM
                    } else {
                        SourceType.RSS
                    }

                    sources.add(
                        Source(
                            id = "custom-${UUID.randomUUID()}",
                            name = text.trim(),
                            url = xmlUrl.trim(),
                            type = type,
                            category = currentCategory,
                            isEnabled = true,
                            isBuiltIn = false
                        )
                    )
                } else {
                    // This outline is a category/folder
                    val categoryName = text.trim().uppercase()
                    currentCategory = Category.entries.find { it.name == categoryName } ?: Category.NEWS
                }
            }
            eventType = parser.next()
        }

        return OpmlImportResult(sources = sources, totalFound = sources.size)
    }
}
