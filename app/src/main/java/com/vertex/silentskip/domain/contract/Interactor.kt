package com.vertex.silentskip.domain.contract

interface Interactor<in I, out O> {
    suspend fun execute(input: I): O
}
