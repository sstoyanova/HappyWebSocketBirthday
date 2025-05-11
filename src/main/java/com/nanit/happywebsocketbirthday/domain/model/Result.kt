package com.nanit.happywebsocketbirthday.domain.model


/**
 * A sealed class representing the outcome of an operation that can either be successful,
 * encounter an error, or be in a loading state.
 *
 * This class is commonly used to represent asynchronous operations, such as API calls
 * or database queries, where the result is not immediately available.
 *
 * The sealed class ensures that all possible outcomes are handled explicitly when using
 * a `when` expression.
 *
 * @param T The type of the data returned on success.
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Throwable? = null) : Result<Nothing>()
    data object Loading : Result<Nothing>() // Optional: if you want to indicate loading in the Flow
}