package com.vertex.silentskip.domain.contract

interface NoInputInteractor<out O> {
    suspend fun execute(): O
}
