package com.ragl.divide.data.models

import androidx.annotation.StringRes
import com.ragl.divide.R

enum class Frequency(@StringRes val resId: Int) {
    DAILY(R.string.daily),
    WEEKLY(R.string.weekly),
    BIWEEKLY(R.string.biweekly),
    MONTHLY(R.string.monthly),
    BIMONTHLY(R.string.bimonthly),
    QUARTERLY(R.string.quarterly),
    SEMIANNUALLY(R.string.semiannually),
    ANNUALLY(R.string.annually)
}