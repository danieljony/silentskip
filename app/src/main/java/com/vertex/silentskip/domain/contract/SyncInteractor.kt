package com.vertex.silentskip.domain.contract

interface SyncInteractor<in I, out O> {
    fun execute(input: I): O
}
