package com.example.loginandregistration.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * A button that displays a loading indicator when processing
 *
 * @param text The button text
 * @param onClick Callback when button is clicked
 * @param modifier Modifier for the button
 * @param isLoading Whether the button is in loading state
 * @param enabled Whether the button is enabled
 * @param contentPadding Padding for button content
 */
@Composable
fun LoadingButton(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        isLoading: Boolean = false,
        enabled: Boolean = true,
        contentPadding: PaddingValues = ButtonDefaults.ContentPadding
) {
    Button(
            onClick = onClick,
            modifier =
                    modifier.semantics {
                        contentDescription = if (isLoading) "$text, Loading" else text
                    },
            enabled = enabled && !isLoading,
            contentPadding = contentPadding
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                )
            } else {
                Text(text = text)
            }
        }
    }
}

/** A secondary variant of LoadingButton with outlined style */
@Composable
fun LoadingOutlinedButton(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        isLoading: Boolean = false,
        enabled: Boolean = true,
        contentPadding: PaddingValues = ButtonDefaults.ContentPadding
) {
    androidx.compose.material3.OutlinedButton(
            onClick = onClick,
            modifier =
                    modifier.semantics {
                        contentDescription = if (isLoading) "$text, Loading" else text
                    },
            enabled = enabled && !isLoading,
            contentPadding = contentPadding
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 2.dp
                )
            } else {
                Text(text = text)
            }
        }
    }
}
