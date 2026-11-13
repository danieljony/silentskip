package com.vertex.silentskip.common

sealed interface DataWrapper<out T> {
    data class Success<out T>(val value: T) : DataWrapper<T>
    data class Error(val error: Throwable) : DataWrapper<Nothing>

    fun onSuccess(action: (T) -> Unit): DataWrapper<T> {
        if (this is Success) action(value)
        return this
    }

    fun onError(action: (Throwable) -> Unit): DataWrapper<T> {
        if (this is Error) action(error)
        return this
    }

    fun getOrNull(): T? = when (this) {
        is Success -> value
        is Error -> null
    }

    fun <R> mapData(transform: (T) -> R): DataWrapper<R> = when (this) {
        is Success -> Success(transform(value))
        is Error -> Error(error)
    }

    fun <R> mapDataWrapper(transform: (T) -> DataWrapper<R>): DataWrapper<R> = when (this) {
        is Success -> transform(value)
        is Error -> Error(error)
    }
}

inline fun <T> safeCall(block: () -> T): DataWrapper<T> {
    return try {
        DataWrapper.Success(block())
    } catch (throwable: Throwable) {
        DataWrapper.Error(throwable)
    }
}
