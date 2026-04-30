package com.example.zybooks.ui.theme.ui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.zybooks.R
import com.example.zybooks.ui.theme.*

@Composable
fun NavBar(navController: NavController, selectedIndex: Int) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(80.dp)
                .background(MutedCrimson)
        ) {
            NavButton(
                label = "CINEMA",
                iconRes = R.drawable.cinema_symbol,
                isSelected = selectedIndex == 2,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                onClick = { if (selectedIndex != 2) navController.navigate("cinemascreen") }
            )
            NavButton(
                label = "HOME",
                iconRes = R.drawable.house_symbol,
                isSelected = selectedIndex == 1,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                onClick = { if (selectedIndex != 1) navController.navigate("homescreen") }
            )
            NavButton(
                label = "SHOP",
                iconRes = R.drawable.dollarsign,
                isSelected = selectedIndex == 0,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                onClick = { if (selectedIndex != 0) navController.navigate("shopscreen") }
            )
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(80.dp)
                .background(MutedCrimson)
        ) {
            NavButton(
                label = "SHOP",
                iconRes = R.drawable.dollarsign,
                isSelected = selectedIndex == 0,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onClick = { if (selectedIndex != 0) navController.navigate("shopscreen") }
            )
            NavButton(
                label = "HOME",
                iconRes = R.drawable.house_symbol,
                isSelected = selectedIndex == 1,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onClick = { if (selectedIndex != 1) navController.navigate("homescreen") }
            )
            NavButton(
                label = "CINEMA",
                iconRes = R.drawable.cinema_symbol,
                isSelected = selectedIndex == 2,
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onClick = { if (selectedIndex != 2) navController.navigate("cinemascreen") }
            )
        }
    }
}

@Composable
fun NavButton(
    label: String,
    iconRes: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(if (isSelected) DarkRed else MutedCrimson)
            .border(0.5.dp, Color.Black)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = label,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
