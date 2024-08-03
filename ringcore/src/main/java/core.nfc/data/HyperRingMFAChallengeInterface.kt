package com.hyperring.sdk.core.data

/**
 * Default HyperRing MFA, Challenge Data Interface
 */
interface HyperRingMFAChallengeInterface : HyperRingDataInterface {
    var isSuccess: Boolean?

    override var id: Long?
    override var data : String?

    override fun encrypt(source: Any?) : ByteArray

    override fun decrypt(source: String?) :Any

    /**
     * Parts you need to implement yourself (MFA Challenge)
     */
    fun challenge(targetData: HyperRingDataInterface): MFAChallengeResponse
}