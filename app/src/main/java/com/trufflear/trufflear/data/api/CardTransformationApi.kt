package com.trufflear.trufflear.data.api

import android.content.Context
import com.trufflear.trufflear.Api.API_BASE_URL
import com.trufflear.trufflear.Api.API_ENDPOINT_PORT
import com.trufflear.trufflear.Api.API_REQUEST_PLATFORM
import com.trufflear.trufflear.CardTransformationGrpcKt
import com.trufflear.trufflear.GetCardTransformationDataResponse
import com.trufflear.trufflear.getCardTransformationDataRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import io.grpc.android.AndroidChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.io.Closeable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardTransformationApi @Inject constructor(@ApplicationContext context: Context) : Closeable {

    private val channel = let {
        AndroidChannelBuilder
            .forAddress(API_BASE_URL, API_ENDPOINT_PORT)
            .context(context)
            .useTransportSecurity()
            .executor(Dispatchers.IO.asExecutor()).build()
    }

    private val cardTransformationService = CardTransformationGrpcKt.CardTransformationCoroutineStub(channel)

    suspend fun getCardTransformationData(): Result<GetCardTransformationDataResponse> = runCatching {
        val request = getCardTransformationDataRequest {
            platform = API_REQUEST_PLATFORM
        }
        cardTransformationService.getCardTransformationData(request)
    }

    override fun close() {
        channel.shutdownNow()
    }
}