package me.mumin.android.files.compat

import android.system.ErrnoException
import me.mumin.android.files.hiddenapi.RestrictedHiddenApi
import me.mumin.android.files.util.lazyReflectedField

@RestrictedHiddenApi
private val functionNameField by lazyReflectedField(ErrnoException::class.java, "functionName")

val ErrnoException.functionNameCompat: String
    get() = functionNameField.get(this) as String
