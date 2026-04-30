package com.example.zybooks.ui.theme.ui

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.zybooks.ui.theme.*

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun NavBar(navController: NavController, selectedIndex: Int) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val barSize = if (isLandscape) screenWidth * 0.1f else screenHeight * 0.1f
        val boxSize = barSize * 0.85f

        if (isLandscape) {
            LazyHorizontalGrid(
                rows = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxHeight()
                    .width(barSize + 32.dp),
                contentPadding = PaddingValues(vertical = 16.dp, horizontal = 8.dp)
            ) {
                item(span = { GridItemSpan(3) }) {
                    Column(
                        modifier = Modifier.fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        repeat(3) { index ->
                            NavBox(
                                isSelected = index == selectedIndex,
                                size = boxSize,
                                onClick = {
                                    if (index == 0 && selectedIndex != 0) navController.navigate("shopscreen")
                                    if (index == 1 && selectedIndex != 1) navController.navigate("homescreen")
                                    if (index == 2 && selectedIndex != 2) navController.navigate("cinemascreen")
                                }
                            )
                        }
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(modifier = Modifier.height(15.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(barSize + 32.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    item(span = { GridItemSpan(3) }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(3) { index ->
                                NavBox(
                                    isSelected = index == selectedIndex,
                                    size = boxSize,
                                    onClick = {
                                        if (index == 0 && selectedIndex != 0) navController.navigate("shopscreen")
                                        if (index == 1 && selectedIndex != 1) navController.navigate("homescreen")
                                        if (index == 2 && selectedIndex != 2) navController.navigate("cinemascreen")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NavBox(isSelected: Boolean, size: androidx.compose.ui.unit.Dp, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .size(size)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isSelected) {
                        Modifier.border(6.dp, Purple40)
                    } else {
                        Modifier
                    }
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isSelected) 6.dp else 0.dp)
                .border(2.dp, SoftWhite)
                .background(MutedCrimson)
        )
    }
}
