package com.example.zybooks.ui.theme.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.zybooks.R

// --- DEV TOOLS COMPOSABLE (Remove for production) ---
@Composable
fun DevToolsRow(
    isTimeFrozen: Boolean,
    onFreezeToggle: () -> Unit,
    onFastForwardChange: (Boolean) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Freeze Time Button
        Image(
            painter = painterResource(id = R.drawable.dev_frozentime),
            contentDescription = "Freeze Time",
            modifier = Modifier
                .size(40.dp)
                .clickable { onFreezeToggle() },
            colorFilter = ColorFilter.tint(if (isTimeFrozen) Color.Green else Color.Red)
        )
        // Fast Forward Button
        Image(
            painter = painterResource(id = R.drawable.dev_fastforwardbutton),
            contentDescription = "Fast Forward",
            modifier = Modifier
                .size(40.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            onFastForwardChange(true)
                            tryAwaitRelease()
                            onFastForwardChange(false)
                        }
                    )
                }
        )
    }
}
// ----------------------------------------------------
