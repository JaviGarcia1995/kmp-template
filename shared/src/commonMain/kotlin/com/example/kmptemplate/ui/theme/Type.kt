package com.example.kmptemplate.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

fun appTypography(): Typography = Typography()

object TypeScale {
    val Headline2: TextStyle
        @Composable get() = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 30.sp
        )

    val Title2: TextStyle
        @Composable get() = TextStyle(
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 26.sp
        )

    val Body1: TextStyle
        @Composable get() = TextStyle(
            fontSize = 17.sp,
            lineHeight = 26.sp
        )

    val Body2: TextStyle
        @Composable get() = TextStyle(
            fontSize = 15.sp,
            lineHeight = 22.sp
        )

    val Label1: TextStyle
        @Composable get() = TextStyle(
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
}
