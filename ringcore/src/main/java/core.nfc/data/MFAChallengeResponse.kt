package com.ringofrings.sdk.core.data

class MFAChallengeResponse(override var id: Long?, override var data: String?, override var isSuccess: Boolean?) : RingOfRingsMFAChallengeInterface {
    override fun encrypt(source: Any?): ByteArray {
        TODO("JWT")
    }

    override fun decrypt(source: String?): Any {
        TODO("JWT")
    }

    override fun challenge(targetData: RingOfRingsDataInterface): MFAChallengeResponse {
        TODO("Not yet implemented")
    }
}