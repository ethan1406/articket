package com.trufflear.trufflear.data

import com.trufflear.trufflear.data.api.CardTransformationApi
import com.trufflear.trufflear.mappers.toArImageMap
import com.trufflear.trufflear.models.CardTransformation
import com.trufflear.trufflear.modules.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArImageRepository @Inject constructor(
    private val cardTransformationApi: CardTransformationApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    private val TAG = ArImageRepository::class.java.simpleName

    suspend fun getCardTransformations(): Result<Map<Int, CardTransformation>> {
        return withContext(ioDispatcher) {
            val response = cardTransformationApi.getCardTransformationData()
            response.map { it.toArImageMap() }
        }
    }
}