package ringofrings.data.nfc
import android.nfc.Tag
import android.util.Log
import com.ringofrings.sdk.core.nfc.RingOfRingsData
import io.jsonwebtoken.Jwts
import javax.crypto.SecretKey

/**
 * JWT Encrypt Demo
 *
 */
class JWTHRData(tag: Tag?, var key: SecretKey) : RingOfRingsData(tag) {
    override var id: Long? = null
    override var data: String? = ""

    constructor(id: Long?, data: String?, key: SecretKey) : this(null, key) {
        this.id = id
        this.data = data
    }

    override fun encrypt(source: Any?): ByteArray {
        if(source is String) {
            if(source !is String) {
                throw Exception("encrypt source is String")
            }

            val jws: String = Jwts.builder().subject(source).signWith(key).compact()
            Log.d("DemoData","encrypted: ${jws}")
            data = jws
            return jws.encodeToByteArray()
        }
        return "".toByteArray()
    }

    override fun decrypt(source: String?): String {
        Log.d("DemoData", "decrypt(source: $source) [${source?.length}]")
        if(source == null) {
            throw DecryptFailure()
        }
        var claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(source)
        Log.d("DemoData","decypted: ${claims.payload.subject}")
        return claims.payload.subject
    }

    companion object {
        fun createData(id: Long, name: String, key: SecretKey): JWTHRData {
            val jwtData = JWTHRData(id, name, key)
            jwtData.encrypt(name)
            return jwtData
        }
    }

    class DecryptFailure : Exception("Decrypt Exception")
}

