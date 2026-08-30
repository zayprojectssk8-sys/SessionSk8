package com.zaysk8.core.utils

sealed interface OperationResult<out T> {
    data object Loading : OperationResult<Nothing>
    data object Success : OperationResult<Nothing>
    data class Error(val message: String, val throwable: Throwable? = null) :
        OperationResult<Nothing>
}