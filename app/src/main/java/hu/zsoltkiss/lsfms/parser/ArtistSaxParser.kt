package hu.zsoltkiss.lsfms.parser

import hu.zsoltkiss.lsfms.model.SimpleArtist
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import javax.xml.parsers.SAXParserFactory

class ArtistSaxParser {

    fun parse(xml: String) {
        val parser = SAXParserFactory.newInstance().newSAXParser()
        parser.parse(xml, handler)
    }

    var artists = mutableListOf<SimpleArtist>()

    private val handler = object: DefaultHandler() {

        var isCollecting = false
        private var currentValue = ""
        private var name: String? = null
        private var listeners: Int? = null
        private var image: String? = null

        override fun startElement(
            uri: String?,
            localName: String?,
            qName: String?,
            attributes: Attributes?
        ) {
            currentValue = ""
            isCollecting = true

            when(localName) {
                "artist" -> {
                    name = null
                    listeners = null

                }
            }
        }

        override fun endElement(uri: String?, localName: String?, qName: String?) {

            isCollecting = false

            when(localName) {
                "artist" -> {
                    if (name != null && listeners != null && image != null) {
                        artists.add(SimpleArtist(name!!, listeners!!, image!!))
                    }
                }

                "name" -> {
                    name = currentValue
                }

                "listeners" -> {
                    listeners = currentValue.toInt()
                }

                "image" -> {
                    image = currentValue
                }
            }


        }


        override fun characters(ch: CharArray?, start: Int, length: Int) {
            if (isCollecting) {
                if (ch != null) {
                    currentValue = currentValue + String(ch, start, length)
                }

            }
        }
    }

}