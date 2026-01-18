package com.kholhang.fynical.utils

object NumberToWordsConverter {
    private val ones = arrayOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
        "Seventeen", "Eighteen", "Nineteen"
    )
    
    private val tens = arrayOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    )
    
    fun convertToWords(amount: Double): String {
        if (amount == 0.0) {
            return "Zero Rupees Only"
        }
        
        val rupees = amount.toLong()
        val paise = ((amount - rupees) * 100).toInt()
        
        val rupeesInWords = convertNumberToWords(rupees)
        val paiseInWords = if (paise > 0) {
            convertNumberToWords(paise.toLong())
        } else {
            ""
        }
        
        return when {
            rupees > 0 && paise > 0 -> {
                "$rupeesInWords Rupees and $paiseInWords Paise Only"
            }
            rupees > 0 -> {
                "$rupeesInWords Rupees Only"
            }
            paise > 0 -> {
                "$paiseInWords Paise Only"
            }
            else -> "Zero Rupees Only"
        }
    }
    
    private fun convertNumberToWords(number: Long): String {
        when {
            number < 20 -> return ones[number.toInt()]
            number < 100 -> {
                val ten = (number / 10).toInt()
                val one = (number % 10).toInt()
                return if (one > 0) {
                    "${tens[ten]} ${ones[one]}"
                } else {
                    tens[ten]
                }
            }
            number < 1000 -> {
                val hundred = (number / 100).toInt()
                val remainder = number % 100
                return if (remainder > 0) {
                    "${ones[hundred]} Hundred ${convertNumberToWords(remainder)}"
                } else {
                    "${ones[hundred]} Hundred"
                }
            }
            number < 100000 -> {
                // Thousands (up to 99,999)
                val thousand = number / 1000
                val remainder = number % 1000
                return if (remainder > 0) {
                    "${convertNumberToWords(thousand)} Thousand ${convertNumberToWords(remainder)}"
                } else {
                    "${convertNumberToWords(thousand)} Thousand"
                }
            }
            number < 10000000 -> {
                // Lakhs (100,000 to 99,99,999) - Indian numbering system
                val lakh = number / 100000
                val remainder = number % 100000
                return if (remainder > 0) {
                    "${convertNumberToWords(lakh)} Lakh ${convertNumberToWords(remainder)}"
                } else {
                    "${convertNumberToWords(lakh)} Lakh"
                }
            }
            number < 1000000000 -> {
                // Crores (1,00,00,000 to 99,99,99,999) - Indian numbering system
                val crore = number / 10000000
                val remainder = number % 10000000
                return if (remainder > 0) {
                    "${convertNumberToWords(crore)} Crore ${convertNumberToWords(remainder)}"
                } else {
                    "${convertNumberToWords(crore)} Crore"
                }
            }
            else -> {
                // Arab (1,00,00,00,000+) - Indian numbering system
                val arab = number / 1000000000
                val remainder = number % 1000000000
                return if (remainder > 0) {
                    "${convertNumberToWords(arab)} Arab ${convertNumberToWords(remainder)}"
                } else {
                    "${convertNumberToWords(arab)} Arab"
                }
            }
        }
    }
}

