package com.kottland.searchplant.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Error types for different scenarios
 */
enum class ErrorType {
    NETWORK_ERROR,
    API_ERROR,
    CAMERA_ERROR,
    PERMISSION_ERROR,
    GENERAL_ERROR
}

/**
 * Beautiful error dialog with retry functionality
 */
@Composable
fun ErrorDialog(
    isVisible: Boolean,
    errorMessage: String,
    errorType: ErrorType = ErrorType.GENERAL_ERROR,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            ErrorDialogContent(
                errorMessage = errorMessage,
                errorType = errorType,
                onDismiss = onDismiss,
                onRetry = onRetry,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun ErrorDialogContent(
    errorMessage: String,
    errorType: ErrorType,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Error Icon
            Icon(
                imageVector = getErrorIcon(errorType),
                contentDescription = "Error",
                modifier = Modifier.size(48.dp),
                tint = getErrorColor(errorType)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Error Title
            Text(
                text = getErrorTitle(errorType),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Error Message
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (onRetry != null) {
                    Arrangement.spacedBy(12.dp)
                } else {
                    Arrangement.Center
                }
            ) {
                // Retry Button (if available)
                if (onRetry != null) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onRetry()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retry")
                    }
                }
                
                // Dismiss Button
                Button(
                    onClick = onDismiss,
                    modifier = if (onRetry != null) {
                        Modifier.weight(1f)
                    } else {
                        Modifier
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (onRetry != null) "Cancel" else "OK",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

/**
 * Get appropriate icon for error type
 */
private fun getErrorIcon(errorType: ErrorType): ImageVector {
    return when (errorType) {
        ErrorType.NETWORK_ERROR -> Icons.Default.Warning
        ErrorType.API_ERROR -> Icons.Default.Warning
        ErrorType.CAMERA_ERROR -> Icons.Default.Warning
        ErrorType.PERMISSION_ERROR -> Icons.Default.Warning
        ErrorType.GENERAL_ERROR -> Icons.Default.Warning
    }
}

/**
 * Get appropriate color for error type
 */
@Composable
private fun getErrorColor(errorType: ErrorType): androidx.compose.ui.graphics.Color {
    return when (errorType) {
        ErrorType.NETWORK_ERROR -> MaterialTheme.colorScheme.tertiary
        ErrorType.API_ERROR -> MaterialTheme.colorScheme.error
        ErrorType.CAMERA_ERROR -> MaterialTheme.colorScheme.error
        ErrorType.PERMISSION_ERROR -> MaterialTheme.colorScheme.tertiary
        ErrorType.GENERAL_ERROR -> MaterialTheme.colorScheme.error
    }
}

/**
 * Get appropriate title for error type
 */
private fun getErrorTitle(errorType: ErrorType): String {
    return when (errorType) {
        ErrorType.NETWORK_ERROR -> "Network Error"
        ErrorType.API_ERROR -> "Service Error"
        ErrorType.CAMERA_ERROR -> "Camera Error"
        ErrorType.PERMISSION_ERROR -> "Permission Required"
        ErrorType.GENERAL_ERROR -> "Something Went Wrong"
    }
}

/**
 * Simple error snackbar for less critical errors
 */
@Composable
fun ErrorSnackbar(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = modifier
    ) { snackbarData ->
        Snackbar(
            snackbarData = snackbarData,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            actionColor = MaterialTheme.colorScheme.error,
            shape = RoundedCornerShape(8.dp)
        )
    }
}

/**
 * Inline error message component
 */
@Composable
fun InlineErrorMessage(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            
            if (onRetry != null) {
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = onRetry,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Retry")
                }
            }
        }
    }
}