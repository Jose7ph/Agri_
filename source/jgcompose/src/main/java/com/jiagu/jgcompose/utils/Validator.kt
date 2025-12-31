package com.jiagu.jgcompose.utils

import java.util.regex.Pattern

object Validator {

    private val reNumerical =
        Pattern.compile("^-?\\d+(([.,])\\d+)?$")
    private val reNumber =
        Pattern.compile("^\\d+$")
    private val reIDNumber =
        Pattern.compile("^[1-9]\\d{5}(19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[Xx\\d]$")
    private val rePhone = Pattern.compile("[1-9][0-9]+")
    private val reEmail =
        Pattern.compile("^\\w[-\\w.+]*@([A-Za-z0-9][-A-Za-z0-9]+\\.)+[A-Za-z]{2,14}$")
    private val reCode = Pattern.compile("[0-9]+")
    private val rePassword =
        Pattern.compile("^(?![0-9]+\$)(?![a-zA-Z]+\$)[0-9A-Za-z]{8,16}\$")   // 简单定，需要修改

    fun checkNumerical(s: String): Boolean {
        return reNumerical.matcher(s).matches()
    }

    fun checkNumber(s: String): Boolean {
        return reNumber.matcher(s).matches()
    }

    fun checkIDNumber(s: String): Boolean {
        return reIDNumber.matcher(s).matches()
    }

    fun checkPhoneNumber(s: String): Boolean {
        return s.length >= 9 && rePhone.matcher(s).matches()
    }

    fun checkEmail(s: String): Boolean {
        return reEmail.matcher(s).matches()
    }

    fun checkVerifyCode(s: String): Boolean {
        return s.length == 4 && reCode.matcher(s).matches()
    }

    fun checkPassword(password: String): Boolean {
        return rePassword.matcher(password).matches()
    }

}