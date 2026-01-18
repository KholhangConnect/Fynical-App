package com.kholhang.fynical.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kholhang.fynical.R

@Composable
fun ShareOptionsDialog(
    onDismiss: () -> Unit,
    onSharePDF: () -> Unit,
    onShareImage: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.share_schedule)) },
        text = { Text(stringResource(R.string.choose_share_format)) },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onSharePDF) {
                    Text(stringResource(R.string.pdf))
                }
                Button(onClick = onShareImage) {
                    Text(stringResource(R.string.image))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

