package vin.pam.closetly.feature.inspiration.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vin.pam.closetly.feature.inspiration.model.SortOrder
import vin.pam.closetly.feature.inspiration.ui.InspirationPalette

@Composable
fun TagChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(CircleShape)
            .background(if (selected) InspirationPalette.Golden else InspirationPalette.CardWhite)
            .border(1.dp, if (selected) InspirationPalette.Golden else InspirationPalette.Border, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            label, fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) InspirationPalette.Heading else InspirationPalette.Muted,
        )
    }
}

/** Clickable sort control. Changing the option reorders the list immediately. */
@Composable
fun SortDropdown(sortOrder: SortOrder, onSelect: (SortOrder) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        Text(
            "${if (sortOrder == SortOrder.RecentlyAdded) "Terbaru" else "Terlama"} ▾",
            Modifier.clickable { expanded = true }.padding(vertical = 4.dp),
            color = InspirationPalette.Muted, fontSize = 12.sp,
        )
        DropdownMenu(expanded, { expanded = false }, containerColor = InspirationPalette.CardWhite) {
            SortOrder.entries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            option.label, fontSize = 13.sp, color = InspirationPalette.Heading,
                            fontWeight = if (option == sortOrder) FontWeight.Bold else FontWeight.Normal,
                        )
                    },
                    onClick = { expanded = false; onSelect(option) },
                )
            }
        }
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick, modifier.height(52.dp), enabled = enabled, shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = InspirationPalette.Golden, contentColor = InspirationPalette.Heading,
            disabledContainerColor = InspirationPalette.Golden.copy(alpha = 0.4f),
            disabledContentColor = InspirationPalette.Heading.copy(alpha = 0.5f),
        ),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) { Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp) }
}

@Composable
fun DangerButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick, modifier.height(52.dp), shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = InspirationPalette.Danger, contentColor = Color.White),
    ) { Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp) }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick, modifier.height(52.dp), shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, InspirationPalette.Border),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = InspirationPalette.CardWhite, contentColor = InspirationPalette.Muted),
    ) { Text(text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp) }
}
