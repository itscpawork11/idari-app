package com.example.data.local

object LocaleManager {

    private val defaultCategoryNamesAr = mapOf(
        "income" to "راتب ودخل مالي",
        "food" to "طعام وغذاء",
        "transport" to "نقل ومواصلات",
        "home" to "إيجار وفواتير منزلية",
        "shopping" to "تسوق وشراء",
        "education" to "صحة وتعليم",
        "entertainment" to "ترفيه وألعاب",
        "other" to "نفقات أخرى"
    )

    private val defaultCategoryNamesEn = mapOf(
        "income" to "Salary & Income",
        "food" to "Food & Groceries",
        "transport" to "Transportation",
        "home" to "Rent & Bills",
        "shopping" to "Shopping",
        "education" to "Health & Education",
        "entertainment" to "Entertainment",
        "other" to "Other Expenses"
    )

    fun getDefaultCategoryName(iconRes: String, language: String): String {
        return if (language == "ar") {
            defaultCategoryNamesAr[iconRes] ?: iconRes
        } else {
            defaultCategoryNamesEn[iconRes] ?: iconRes
        }
    }

    fun getGeneralBudgetName(language: String): String {
        return if (language == "ar") "الميزانية العامة" else "General Budget"
    }

    fun getDedicatedBudgetName(language: String): String {
        return if (language == "ar") "الميزانية المخصصة" else "Dedicated Budget"
    }

    fun getValidationAmountPositive(language: String): String {
        return if (language == "ar") "القيمة يجب أن تكون أكبر من الصفر" else "Amount must be greater than zero"
    }
}
