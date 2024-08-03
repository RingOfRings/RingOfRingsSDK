package com.hyperring.sdk.core.data

class MFAChallengeResponse(override var id: Long?, override var data: String?, override var isSuccess: Boolean?) : HyperRingMFAChallengeInterface {
    override fun encrypt(source: Any?): ByteArray {
        TODO("JWT")
    }

    override fun decrypt(source: String?): Any {
        TODO("JWT")
    }

    override fun challenge(targetData: HyperRingDataInterface): MFAChallengeResponse {
        TODO("Not yet implemented")
    }
}