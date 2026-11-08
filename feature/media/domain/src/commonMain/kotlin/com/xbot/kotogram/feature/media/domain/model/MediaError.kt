package com.xbot.kotogram.feature.media.domain.model

sealed interface MediaError {
    data object DownloadFailed : MediaError

    data class Unknown(val message: String) : MediaError
}
