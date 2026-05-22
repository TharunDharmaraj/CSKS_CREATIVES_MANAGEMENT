package com.example.csks_creatives.presentation.taskDetailScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.example.csks_creatives.presentation.components.*

@Composable
fun DropdownMenuWithSelection(
    label: String,
    selectedItem: String,
    items: List<String>,
    onItemSelected: (String) -> Unit,
    enabled: Boolean = true,
    isVisible: Boolean = true
) {
    if (isVisible) {
        var expanded by remember { mutableStateOf(false) }

        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (enabled) silverGrey else silverGrey.copy(alpha = 0.4f),
                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (enabled) charCoalPurple else charCoalPurple.copy(alpha = 0.5f), 
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = when {
                            expanded -> vividCerulean
                            enabled -> silverGrey.copy(alpha = 0.2f)
                            else -> silverGrey.copy(alpha = 0.05f)
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(enabled) { expanded = true }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedItem,
                        color = if (enabled) white else white.copy(alpha = 0.4f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (enabled) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = vividCerulean,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (enabled) {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(charCoal, shape = RoundedCornerShape(12.dp))
                        .border(1.dp, silverGrey.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    offset = DpOffset(x = 0.dp, y = 4.dp),
                    properties = PopupProperties(focusable = true)
                ) {
                    items.forEachIndexed { index, item ->
                        val isSelected = item == selectedItem

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = item,
                                    color = if (isSelected) vividCerulean else white,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            onClick = {
                                onItemSelected(item)
                                expanded = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected) vividCerulean.copy(alpha = 0.1f) else Color.Transparent
                                )
                        )

                        if (index != items.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                thickness = 0.5.dp,
                                color = silverGrey.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
