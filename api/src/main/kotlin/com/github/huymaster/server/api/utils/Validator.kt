package com.github.huymaster.server.api.utils

sealed class ValidationResult {
    object Pass : ValidationResult()
    object Fail : ValidationResult()
}

inline fun ValidationResult.onPass(block: () -> Unit): ValidationResult {
    if (this is ValidationResult.Pass) block()
    return this
}

inline fun ValidationResult.onFail(block: () -> Unit): ValidationResult {
    if (this is ValidationResult.Fail) block()
    return this
}

fun interface Validator<T> {
    fun validate(t: T): ValidationResult
}

fun <T> T.validateWith(
    validator: Validator<T>
): ValidationResult = validator.validate(this)

open class StringValidator(
    private val regex: Regex
) : Validator<String> {
    constructor(regex: String) : this(Regex(regex))
    constructor(regex: String, options: RegexOption) : this(Regex(regex, options))

    final override fun validate(t: String): ValidationResult {
        return if (regex.matches(t))
            ValidationResult.Pass
        else
            ValidationResult.Fail
    }
}

object UsernameValidator : StringValidator(Regex("^[a-zA-Z0-9_]{3,16}$"))
object PasswordValidator : StringValidator(Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,32}$"))