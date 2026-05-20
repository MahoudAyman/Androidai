package com.example.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun CalculatorScreen(
    viewModel: VaultViewModel,
    onNavigateToVault: () -> Unit
) {
    val savedPin by viewModel.savedPin.collectAsState()
    val navigateToVault by viewModel.navigateToVault.collectAsState()

    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var isSetupMode by remember { mutableStateOf(false) }
    
    // Setup message visibility
    var showSetupHint by remember { mutableStateOf(false) }

    LaunchedEffect(savedPin) {
        isSetupMode = savedPin == null
        if (isSetupMode) {
            showSetupHint = true
            delay(3000)
            showSetupHint = false
        }
    }

    LaunchedEffect(navigateToVault) {
        if (navigateToVault) {
            onNavigateToVault()
            viewModel.onVaultExited()
            input = ""
            result = ""
        }
    }

    val onButtonClick = { button: String ->
        when (button) {
            "C" -> {
                input = ""
                result = ""
            }
            "⌫" -> {
                if (input.isNotEmpty()) {
                    input = input.dropLast(1)
                }
            }
            "=" -> {
                if (isSetupMode) {
                    if (input.isNotEmpty()) {
                        viewModel.setPin(input)
                        input = ""
                        result = "Set!"
                        isSetupMode = false
                    }
                } else {
                    if (input == savedPin) {
                        viewModel.onPinEntered()
                    } else {
                        try {
                            result = evaluateMath(input)
                        } catch (e: Exception) {
                            result = "Error"
                        }
                    }
                }
            }
            else -> {
                input += button
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .systemBarsPadding(),
        verticalArrangement = Arrangement.Bottom
    ) {
        if (showSetupHint) {
            Text(
                text = "Set your secret PIN and press =",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = input.ifEmpty { "0" },
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.End,
            maxLines = 1
        )
        
        if (result.isNotEmpty()) {
            Text(
                text = result,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End, maxLines = 1
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))

        val buttons = listOf(
            listOf("C", "⌫", "/", "*"),
            listOf("7", "8", "9", "-"),
            listOf("4", "5", "6", "+"),
            listOf("1", "2", "3", "="),
            listOf("0", ".", "")
        )

        buttons.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { btn ->
                    if (btn.isEmpty()) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        val isEquals = btn == "="
                        val isZero = btn == "0"
                        CalculatorButton(
                            text = btn,
                            modifier = Modifier
                                .weight(if (isZero) 2.1f else 1f)
                                .aspectRatio(if (isZero) 2.1f else 1f),
                            color = if (isEquals) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            textColor = if (isEquals) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            onClick = { onButtonClick(btn) }
                        )
                    }
                }
            }
            if (rowIndex < buttons.size - 1) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun CalculatorButton(
    text: String,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 32.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

// Very basic evaluator for demo purposes
// In a real app this would be a proper parser, but evaluates basic operators sequentially.
fun evaluateMath(expression: String): String {
    // This is a naive implementation without precedence checking, just for disguise.
    if (expression.isEmpty()) return ""
    var currentNumber = ""
    var result = 0.0
    var lastOp = '+'
    
    val expr = expression.replace(" ", "")
    
    fun applyOp(value: Double) {
        when (lastOp) {
            '+' -> result += value
            '-' -> result -= value
            '*' -> result *= value
            '/' -> if (value != 0.0) result /= value else throw Exception()
        }
    }
    
    try {
        for (char in expr) {
            if (char.isDigit() || char == '.') {
                currentNumber += char
            } else if (char in setOf('+', '-', '*', '/')) {
                if (currentNumber.isNotEmpty()) {
                    applyOp(currentNumber.toDouble())
                    currentNumber = ""
                }
                lastOp = char
            }
        }
        if (currentNumber.isNotEmpty()) {
            applyOp(currentNumber.toDouble())
        }
        
        val isInt = result == result.toLong().toDouble()
        return if (isInt) result.toLong().toString() else result.toString()
    } catch (e: Exception) {
        return "Error"
    }
}
