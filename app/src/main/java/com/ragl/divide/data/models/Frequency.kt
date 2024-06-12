package com.ragl.divide.data.models

import androidx.annotation.StringRes
import com.ragl.divide.R

enum class Frequency(@StringRes val resId: Int) {
    ONCE(R.string.once),
    DAILY(R.string.daily),
    WEEKLY(R.string.weekly),
    BIWEEKLY(R.string.biweekly),
    MONTHLY(R.string.monthly),
    BIMONTHLY(R.string.bimonthly),
    QUARTERLY(R.string.quarterly),
    SEMIANNUALLY(R.string.semiannually),
    ANNUALLY(R.string.annually)
}

fun Frequency.getInMillis(): Long{
    return when(this){
        Frequency.ONCE -> 0L
        Frequency.DAILY -> 86400000L
        Frequency.WEEKLY -> 604800000L
        Frequency.BIWEEKLY -> 1210000000L
        Frequency.MONTHLY -> 2592000000L
        Frequency.BIMONTHLY -> 5184000000L
        Frequency.QUARTERLY -> 7776000000L
        Frequency.SEMIANNUALLY -> 15552000000L
        Frequency.ANNUALLY -> 31104000000L
    }
}