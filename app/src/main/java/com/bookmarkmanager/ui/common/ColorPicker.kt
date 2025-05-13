package com.bookmarkmanager.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bookmarkmanager.data.model.Category

/**
 * A color picker component that displays a grid of color options.
 * 
 * @param selectedColor The currently selected color
 * @param onColorSelected Callback when a color is selected
 * @param modifier Modifier for customizing the layout
 */
@Composable
fun ColorPicker(
    selectedColor: Long,
    onColorSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Select Category Color",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 56.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(120.dp)
        ) {
            items(Category.COLOR_PALETTE) { color ->
                ColorOption(
                    color = Color(color),
                    selected = color == selectedColor,
                    onClick = { onColorSelected(color) }
                )
            }
        }
    }
}

/**
 * A single color option in the color picker.
 * 
 * @param color The color to display
 * @param selected Whether this color is currently selected
 * @param onClick Callback when this color is clicked
 */
@Composable
fun ColorOption(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                border = BorderStroke(
                    width = if (selected) 3.dp else 0.dp,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = if (isDarkColor(color)) Color.White else Color.Black
            )
        }
    }
}

/**
 * Check if a color is considered "dark" for deciding whether to use white or black text.
 * 
 * This uses a simple luminance calculation (perceived brightness).
 * 
 * @param color The color to check
 * @return True if the color is considered dark
 */
fun isDarkColor(color: Color): Boolean {
    // Calculate perceived brightness using the formula:
    // (0.299*R + 0.587*G + 0.114*B)
    val luminance = (0.299f * color.red + 0.587f * color.green + 0.114f * color.blue)
    return luminance < 0.5f
}