package ringofrings.data.mfa
import android.nfc.Tag
import android.util.Log
import com.ringofrings.sdk.core.data.RingOfRingsDataInterface
import com.ringofrings.sdk.core.data.RingOfRingsMFAChallengeInterface
import com.ringofrings.sdk.core.data.MFAChallengeResponse
import io.jsonwebtoken.Jwts
import javax.crypto.SecretKey


/**
 * MFA Challenge Data with JWT
 */
class JWTMFAChallengeData(tag: Tag?, var key: SecretKey) : RingOfRingsMFAChallengeInterface {
    override var id: Long? = null
    override var data: String? = ""
    override var isSuccess: Boolean? = null
    constructor(id: Long, data: String, isSuccess: Boolean?, key: SecretKey) : this(null, key) {
        this.id = id
        this.data = data
        this.isSuccess = isSuccess
    }

    /**
     * Source is String data
     */
    override fun encrypt(source: Any?): ByteArray {
        if(source !is String) {
            throw Exception("encrypt source is String")
        }

        val jws: String = Jwts.builder().subject(source).signWith(key).compact()
        Log.d("DemoData","encrypted: ${jws}")
        data = jws
        return jws.encodeToByteArray()
    }

    fun encrypt(source: String): ByteArray {
        return encrypt(source as Any?)
    }

    override fun decrypt(source: String?): String {
        if(source == null) {
            throw DecryptFailure()
        }
        var claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(source)
        Log.d("DemoData","decypted1: ${claims.payload}")
        Log.d("DemoData","decypted2: ${claims.payload.id}")
        Log.d("DemoData","decypted3: ${claims.payload.subject}")
        return claims.payload.subject
    }

    /**
     * Compare this DemoMFAData to targetData
     * Must be override this function
     *
     * @param targetData
     */
    override fun challenge(targetData: RingOfRingsDataInterface): MFAChallengeResponse {
        var res: MFAChallengeResponse
        if(decrypt(data) == decrypt(targetData.data)) {
            res = MFAChallengeResponse(targetData.id, targetData.data, true)
        } else {
            res = MFAChallengeResponse(targetData.id, targetData.data, false)
        }
        return res
    }

    companion object {
        fun createData(id: Long, name: String, key: SecretKey): JWTMFAChallengeData {
            return JWTMFAChallengeData(id, name, null, key)
        }

    }
}

class DecryptFailure : Exception("Decrypt Exception")

