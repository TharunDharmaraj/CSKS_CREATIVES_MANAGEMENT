package com.example.csks_creatives.presentation.taskDetailScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties

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
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2C2C2E), RoundedCornerShape(10.dp))
                    .clickable(enabled) { expanded = true }
                    .padding(14.dp)
            ) {
                Text(
                    text = selectedItem,
                    color = Color.White
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1C1C1E), shape = RoundedCornerShape(10.dp)),
                offset = DpOffset(x = 0.dp, y = 4.dp),
                properties = PopupProperties(focusable = true)
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = item == selectedItem

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = item,
                                color = if (isSelected) Color(0xFF00E676) else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onItemSelected(item)
                            expanded = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isSelected) Color(0xFF3A3A3C) else Color.Transparent,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(vertical = 4.dp, horizontal = 12.dp)
                    )

                    if (index != items.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            thickness = 0.5.dp,
                            color = Color.LightGray.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}