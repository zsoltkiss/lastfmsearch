package hu.zsoltkiss.lsfms.parser

import android.util.Log
import hu.zsoltkiss.lsfms.model.SimpleArtist
import java.util.regex.Pattern

/**
 * Workaround!
 *
 * Az artist.search valaszt JSON es XML alapu parser-rel sem sikerult hibamentesen feldolgozni!
 * Egy custom JSON deserializerhez tobb ido kellett volna, a SAX es a DOM parse-olas meg kivetelt dobott.
 */
object ArtistRegexParser {

    private val artistPattern = Pattern.compile("<artist>(.+?)</artist>", Pattern.DOTALL)
    private val namePattern = Pattern.compile("<name>(.+?)</name>", Pattern.DOTALL)
    private val listenersPattern = Pattern.compile("<listeners>(.+?)</listeners>", Pattern.DOTALL)
    private val imagePattern = Pattern.compile("<image size=\"medium\">(.+?)</image>", Pattern.DOTALL)

    private const val TAG = "ArtistRegexParser"


    fun retrieveArtistNodes(input: String): List<SimpleArtist> {

        val artists = mutableListOf<SimpleArtist>()

        val matcher = artistPattern.matcher(input)

        while (matcher.find()) {
            val artistNode = matcher.group(0)
            if (artistNode != null) {
                val name = retrieveTagContent(artistNode, "name")
                val listeners = retrieveTagContent(artistNode, "listeners")
                val mediumImage = retrieveTagContent(artistNode, "image")


                if (name != null && listeners != null && mediumImage != null) {
                    val sa = SimpleArtist(name, listeners.toInt(), mediumImage)
                    Log.d(TAG, "Added: $sa")
                    artists.add(sa)
                }
            }
        }

        return artists
    }

    private fun retrieveTagContent(input: String, tagName: String): String? {
        var textContent: String? = null
        val pattern = when(tagName) {
            "name" -> namePattern
            "listeners" -> listenersPattern
            "image" -> imagePattern
            else -> null
        }

        if (pattern != null) {

//            Log.d(TAG, "Looking for '$tagName' in \n$input")


            val matcher = pattern.matcher(input)
            if (matcher.find()) {
                textContent = matcher.group(1)
            }
        }

        return textContent
    }

}