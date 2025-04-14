package com.example.csks_creatives.presentation.taskDetailScreen.components

import android.content.Intent
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.core.net.toUri


@Composable
fun ClickableLinkTextField(
    text: String, onTextChange: (String) -> Unit, readOnly: Boolean
) {
    val context = LocalContext.current
    val annotatedText = remember(text) {
        buildAnnotatedString {
            val regex = "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=]+)".toRegex()
            var lastIndex = 0
            regex.findAll(text).forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1
                append(text.substring(lastIndex, start))
                pushStringAnnotation(tag = "URL", annotation = match.value)
                withStyle(
                    style = SpanStyle(
                        color = Color.Blue, textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(match.value)
                }
                pop()
                lastIndex = end
            }
            append(text.substring(lastIndex))
        }
    }

    OutlinedTextField(
        value = text,
        onValueChange = { if (!readOnly) onTextChange(it) },
        readOnly = readOnly,
        label = { Text("Task Description") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = false,
        maxLines = Int.MAX_VALUE,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Default
        ),
        textStyle = LocalTextStyle.current.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        visualTransformation = {
            TransformedText(annotatedText, OffsetMapping.Identity)
        },
        interactionSource = remember { MutableInteractionSource() }.also { source ->
            LaunchedEffect(source) {
                source.interactions.collect { interaction ->
                    if (interaction is PressInteraction.Release) {
                        val offset = interaction.press.pressPosition.x.toInt()
                        annotatedText.getStringAnnotations(
                            tag = "URL", start = offset, end = offset
                        ).firstOrNull()?.let { annotation ->
                            val intent = Intent(Intent.ACTION_VIEW, annotation.item.toUri())
                            context.startActivity(intent)
                        }
                    }
                }
            }
        })
}