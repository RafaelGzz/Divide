package com.ragl.divide.data.models

import androidx.annotation.StringRes
import com.ragl.divide.R

enum class Method(@StringRes val resId: Int) {
    EQUALLY(R.string.equally),
    PERCENTAGES(R.string.percentages),
    CUSTOM(R.string.custom)
}