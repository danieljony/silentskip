package com.vertex.silentskip.domain.contract

interface SyncNoInputInteractor<out O> {
    fun execute(): O
}
