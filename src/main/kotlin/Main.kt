import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.lang.IllegalStateException
import java.net.URL

@Serializable
data class Post(
    val id: Int,
    val textContent: String,
    val userId: Int,
    val audioId: String?,
    val squadId: String?,
    val linkedDiscussionId: Int?,
    val linkedCommentId: Int?,
    val date: String?,
    val privacy: String
) {
}


fun main() {
    println("Hello World!")
    val posts = parseXml(getData())

    print(Json.encodeToString(posts))
}

fun getData(): InputStream {
    val url = URL("https://prueba.agenciareforma.com/AppIphone/Android/xml/posts.xml")
    val connection  = url.openConnection()

    connection.connect()

    return connection.getInputStream()
}

fun skip(parser: XmlPullParser) {
    if(parser.eventType != XmlPullParser.START_TAG) {
        throw IllegalStateException()
    }
    var depth = 1
    while(depth != 0){
        when(parser.next()){
            XmlPullParser.END_TAG -> depth--
            XmlPullParser.START_TAG -> depth++
        }
    }
}

fun stringNullOrNullishToNullableString(string: String?): String? {
    return if(string == null || string == "null") null else string
}

fun parseXml(inputStream: InputStream): List<Post> {
    inputStream.use {
        val parser: XmlPullParser = XmlPullParserFactory.newInstance().newPullParser()

        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)
        parser.nextTag()

        return readPosts(parser)
    }
}

fun readPosts(parser: XmlPullParser): List<Post> {
    val posts = mutableListOf<Post>()

    parser.require(XmlPullParser.START_TAG, null, "Posts")
    while(parser.next() != XmlPullParser.END_TAG) {
        if(parser.eventType != XmlPullParser.START_TAG){
            continue
        }

        if(parser.name == "Post"){
            posts.add(readPost(parser))
        } else {
            skip(parser)
        }
    }

    return posts
}


fun readPost(parser: XmlPullParser): Post {
    var id = 0
    var userId = 0
    var audioId: String? = null
    var squadId: String? = null
    var linkedDiscussionId: Int? = null
    var linkedCommentId: Int? = null
    var date: String? = null
    var privacy = "No seteado"
    var textContent = "No seteado"

    while(parser.next() != XmlPullParser.END_TAG) {
        if(parser.eventType != XmlPullParser.START_TAG) {
            continue
        }

        when(parser.name) {
            "id" -> id = readId(parser)
            "text-content" -> textContent = readTextContent(parser)
            "user-id" -> userId = readUserId(parser)
            "audio-id" -> audioId = readAudioId(parser)
            "squad-id" -> squadId = readSquadId(parser)
            "linked-discussion-id" -> linkedDiscussionId = readLinkedDiscussionId(parser)
            "linked-comment-id" -> linkedCommentId = readLinkedCommentId(parser)
            "date" -> date = readDate(parser)
            "privacy" -> privacy = readPrivacy(parser)
        }
    }

    return Post(id, textContent, userId, audioId, squadId, linkedDiscussionId, linkedCommentId, date, privacy)
}

fun readLinkedCommentId(parser: XmlPullParser): Int? {
    parser.require(XmlPullParser.START_TAG, null, "linked-comment-id")
    val linkedCommentId = parser.nextText()
    parser.require(XmlPullParser.END_TAG, null, "linked-comment-id")

    return linkedCommentId.toIntOrNull()
}

fun readPrivacy(parser: XmlPullParser): String {
    parser.require(XmlPullParser.START_TAG, null, "privacy")
    val privacy = parser.nextText()
    parser.require(XmlPullParser.END_TAG, null, "privacy")

    return when(privacy.toIntOrNull()){
        1 -> "Privado"
        2 -> "Usuarios"
        3 -> "Publico"
        else -> "Desconocido"
    }
}

fun readDate(parser: XmlPullParser): String? {
    parser.require(XmlPullParser.START_TAG, null, "date")
    val date = parser.nextText()
    parser.require(XmlPullParser.END_TAG, null, "date")

    return date
}

fun readLinkedDiscussionId(parser: XmlPullParser): Int? {
    parser.require(XmlPullParser.START_TAG, null, "linked-discussion-id")
    val linkedDiscussionId = parser.nextText()
    parser.require(XmlPullParser.END_TAG, null, "linked-discussion-id")

    return linkedDiscussionId.toIntOrNull()
}

fun readSquadId(parser: XmlPullParser): String? {
    parser.require(XmlPullParser.START_TAG, null, "squad-id")
    val squadId = parser.nextText()
    parser.require(XmlPullParser.END_TAG, null, "squad-id")

    return stringNullOrNullishToNullableString(squadId)
}

fun readAudioId(parser: XmlPullParser): String? {
    parser.require(XmlPullParser.START_TAG, null, "audio-id")
    val audioId = parser.nextText()
    parser.require(XmlPullParser.END_TAG, null, "audio-id")

    return stringNullOrNullishToNullableString(audioId)
}

fun readUserId(parser: XmlPullParser): Int {
    parser.require(XmlPullParser.START_TAG, null, "user-id")
    val userId = parser.nextText()
    parser.require(XmlPullParser.END_TAG, null, "user-id")

    return userId.toInt()
}

fun readTextContent(parser: XmlPullParser): String {
    parser.require(XmlPullParser.START_TAG, null, "text-content")
    val textContent = parser.nextText()
    parser.require(XmlPullParser.END_TAG, null, "text-content")

    return textContent
}

fun readId(parser: XmlPullParser): Int {
    parser.require(XmlPullParser.START_TAG, null, "id")
    val id = parser.nextText()
    parser.require(XmlPullParser.END_TAG, null, "id")

    return id.toInt()
}
