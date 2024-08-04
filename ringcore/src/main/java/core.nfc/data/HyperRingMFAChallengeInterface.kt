package com.ringofrings.sdk.core.data

/**
 * Default RingOfRings MFA, Challenge Data Interface
 */
interface RingOfRingsMFAChallengeInterface : RingOfRingsDataInterface {
    var isSuccess: Boolean?

    override var id: Long?
    override var data : String?

    override fun encrypt(source: Any?) : ByteArray

    override fun decrypt(source: String?) :Any

    /**
     * Parts you need to implement yourself (MFA Challenge)
     */
    fun challenge(targetData: RingOfRingsDataInterface): MFAChallengeResponse
}