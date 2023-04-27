package com.example.radiostationapp

import retrofit2.http.GET
import retrofit2.http.Query

interface RadioBrowserApi {
    @GET("stations/search")
    suspend fun searchStations(
        @Query("limit") limit: Int = 10,
        @Query("hidebroken") hidebroken: Boolean = true,
        @Query("has_extended_info") hasExtendedInfo: Boolean = true,
        @Query("order") order: String = "clickcount",
        @Query("reverse") reverse: Boolean = true
    ): List<RadioStationsResponse>
}
