package com.example.flashcardapp.network

import retrofit2.http.GET
import retrofit2.http.Query

interface DictionaryApiService {

    @GET("translate_a/single")
    suspend fun translateWord(
        @Query("client") client: String = "gtx",
        @Query("sl") sl: String = "en",
        @Query("tl") tl: String = "vi",
        @Query("dt") dt: String = "t",
        @Query("q") word: String
    ): String
}