package com.achmadichzan.rangkum.presentation.navigation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

//@Serializable
//object HomeRoute

@Serializable
@Parcelize
data class DetailRoute(
    val sessionId: Long
) : Parcelable