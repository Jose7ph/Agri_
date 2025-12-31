package com.jiagu.ags4.utils

import java.util.regex.Pattern

object Validator {

    private val rePhone = Pattern.compile("[1-9][0-9]+")
    private val reEmail =
        Pattern.compile("^\\w[-\\w.+]*@([A-Za-z0-9][-A-Za-z0-9]+\\.)+[A-Za-z]{2,14}$")
    private val reCode = Pattern.compile("[0-9]+")
    private val rePassword =
        Pattern.compile("^(?![0-9]+\$)(?![a-zA-Z]+\$)[0-9A-Za-z]{8,16}\$")   // 简单定，需要修改
    private val reNumerical =
        Pattern.compile("^-?\\d+(([.,])\\d+)?$")
    private val reNumber =
        Pattern.compile("^\\d+$")

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

    fun checkNumerical(s: String): Boolean {
        return reNumerical.matcher(s).matches()
    }
    fun checkNumber(s: String): Boolean {
        return reNumber.matcher(s).matches()
    }

}