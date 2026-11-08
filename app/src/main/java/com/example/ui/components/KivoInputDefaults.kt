package com.example.ui.components

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardBorderColor
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryMuted

/**
 * Standard input styling across KivoPay.
 * Guarantees deep dark slate text (#0F172A) on pure white background
 * regardless of system dark/light theme, ensuring high contrast readability.
 */
@Composable
fun kivoInputTextStyle(fontSize: Int = 15, fontWeight: FontWeight = FontWeight.SemiBold): TextStyle {
    return TextStyle(
        color = TextPrimaryDark,
        fontSize = fontSize.sp,
        fontWeight = fontWeight
    )
}

@Composable
fun kivoBigAmountTextStyle(): TextStyle {
    return TextStyle(
        color = TextPrimaryDark,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun kivoTextFieldColors(
    containerColor: Color = Color.White
): TextFieldColors {
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = TextPrimaryDark,
        unfocusedTextColor = TextPrimaryDark,
        disabledTextColor = TextSecondaryMuted,
        focusedContainerColor = containerColor,
        unfocusedContainerColor = containerColor,
        disabledContainerColor = Color(0xFFF1F5F9),
        cursorColor = PrimaryBlue,
        focusedBorderColor = PrimaryBlue,
        unfocusedBorderColor = CardBorderColor,
        focusedLabelColor = PrimaryBlue,
        unfocusedLabelColor = TextSecondaryMuted,
        focusedPlaceholderColor = TextSecondaryMuted,
        unfocusedPlaceholderColor = TextSecondaryMuted,
        focusedPrefixColor = PrimaryBlue,
        unfocusedPrefixColor = PrimaryBlue,
        focusedLeadingIconColor = PrimaryBlue,
        unfocusedLeadingIconColor = TextSecondaryMuted,
        focusedTrailingIconColor = PrimaryBlue,
        unfocusedTrailingIconColor = TextSecondaryMuted
    )
}
