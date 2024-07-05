package com.ragl.divide.ui.utils

fun validateQuantity(input: String, updateInput: (String) -> Unit) {
    if (input.isEmpty()) updateInput("") else if(!input.contains(',')){
        val parsed = input.toDoubleOrNull()
        parsed?.let {
            val decimalPart = input.substringAfter(".", "")
            if (decimalPart.length <= 2 && parsed <= 999999999.99) {
                updateInput(input)
            }
        }
    }
}