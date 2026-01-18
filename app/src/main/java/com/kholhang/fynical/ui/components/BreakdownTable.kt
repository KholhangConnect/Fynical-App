package com.kholhang.fynical.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kholhang.fynical.utils.CurrencyFormatter

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float,
    header: Boolean = false
) {
    Text(
        text = text,
        modifier = Modifier
            .weight(weight)
            .padding(horizontal = 4.dp),
        fontWeight = if (header) FontWeight.Bold else FontWeight.Normal,
        style = if (header) MaterialTheme.typography.labelSmall else MaterialTheme.typography.bodySmall,
        color = if (header) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
    )
}

