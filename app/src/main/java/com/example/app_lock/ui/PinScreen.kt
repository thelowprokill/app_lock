package com.example.app_lock.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PinScreen(
    isSetup: Boolean,
    onPinConfirmed: (String) -> Boolean,
    title: String = if (isSetup) "Set your PIN" else "Enter PIN",
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirming by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .padding(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (isConfirming) "Confirm your PIN" else title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(modifier = Modifier.height(24.dp), contentAlignment = Alignment.Center) {
                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val currentPin = if (isConfirming) confirmPin else pin
                repeat(4) { index ->
                    PinDot(filled = index < currentPin.length)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            NumberPad(
                onNumberClick = { num ->
                    errorText = null
                    if (isConfirming) {
                        if (confirmPin.length < 4) {
                            confirmPin += num
                            if (confirmPin.length == 4) {
                                if (confirmPin == pin) {
                                    if (onPinConfirmed(pin)) {
                                        // Success
                                    } else {
                                        errorText = "Invalid PIN"
                                        confirmPin = ""
                                        pin = ""
                                        isConfirming = false
                                    }
                                } else {
                                    errorText = "PINs do not match"
                                    confirmPin = ""
                                }
                            }
                        }
                    } else {
                        if (pin.length < 4) {
                            pin += num
                            if (pin.length == 4) {
                                if (isSetup) {
                                    isConfirming = true
                                } else {
                                    if (onPinConfirmed(pin)) {
                                        // PIN correct
                                    } else {
                                        errorText = "Incorrect PIN"
                                        pin = ""
                                    }
                                }
                            }
                        }
                    }
                },
                onDeleteClick = {
                    errorText = null
                    if (isConfirming) {
                        if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                    } else {
                        if (pin.isNotEmpty()) pin = pin.dropLast(1)
                    }
                }
            )
        }
    }
}

@Composable
fun PinDot(filled: Boolean) {
    val size by animateDpAsState(if (filled) 20.dp else 16.dp, label = "size")
    val color by animateColorAsState(if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, label = "color")
    
    Box(
        modifier = Modifier.size(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(size),
            shape = CircleShape,
            color = color,
            tonalElevation = if (filled) 8.dp else 0.dp
        ) {}
    }
}

@Composable
fun NumberPad(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    val numbers = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "back")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        numbers.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                row.forEach { cell ->
                    when (cell) {
                        "" -> Spacer(modifier = Modifier.size(80.dp))
                        "back" -> {
                            IconButton(
                                onClick = onDeleteClick,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Backspace,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        else -> {
                            NumberButton(
                                number = cell,
                                onClick = { onNumberClick(cell) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NumberButton(
    number: String,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.size(80.dp),
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = number,
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp)
        )
    }
}
