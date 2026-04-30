package com.example.zybooks.ui.theme.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.zybooks.R


class ShopActivity : ComponentActivity() {
    private val viewModel: ShopViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    ShopScreen(navController = navController, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun ShopScreen(
    navController: NavController,
    viewModel: ShopViewModel = viewModel()
) {
    val rows by viewModel.rows.collectAsState()
    val selectedSlot by viewModel.selectedSlot.collectAsState()
    val userCoins by viewModel.userCoins.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                BottomAppBar {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.coin),
                            contentDescription = "Coins",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Coins: $userCoins",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color.White),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // TOP BOUNDARY: 360px width, 95px height
                Box(
                    modifier = Modifier
                        .width(360.dp)
                        .height(95.dp)
                        .background(Color.Red)
                )

                // MIDDLE AREA: 360px width, 629px height
                Box(
                    modifier = Modifier
                        .width(360.dp)
                        .height(629.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(top = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(40.dp)
                    ) {
                        rows.forEach { row ->
                            Column(
                                horizontalAlignment = Alignment.Start,
                                modifier = Modifier.width(334.dp)
                            ) {
                                Text(
                                    text = row.title.uppercase(),
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.Black,
                                    modifier = Modifier.padding(start = 5.dp, bottom = 4.dp)
                                )
                                ShopRowItem(row, selectedSlot, onSlotSelect = { viewModel.selectSlot(it) })
                            }
                        }
                    }

                    // BUY BUTTON
                    selectedSlot?.let { slot ->
                        val hasEnoughCoins = userCoins >= slot.price
                        Button(
                            onClick = { if (hasEnoughCoins) viewModel.purchaseItem(slot) },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                                .width(200.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasEnoughCoins) Color(0xFFB23A3A) else Color.Gray
                            ),
                            shape = RoundedCornerShape(10.dp),
                            enabled = hasEnoughCoins
                        ) {
                            Text(
                                text = if (hasEnoughCoins) "BUY $${slot.price}" else "NOT ENOUGH",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // BOTTOM BOUNDARY
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.Red)
                )
            }
        }

        // NavBar as a top-level overlay at the very top
        NavBar(navController = navController, selectedIndex = 0)
    }
}

@Composable
fun ShopRowItem(
    row: ShopRow,
    selectedSlot: ShopSlot?,
    onSlotSelect: (ShopSlot) -> Unit
) {
    Row(
        modifier = Modifier
            .width(334.dp)
            .height(IntrinsicSize.Min)
            .background(color = Color(0xFFB23A3A), shape = RoundedCornerShape(15.dp))
            .padding(horizontal = 27.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        row.slots.forEach { slot ->
            // Check selection against the Unique ID
            val isSelected = selectedSlot?.id == slot.id

            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFFE0E0E0), shape = RoundedCornerShape(8.dp))
                    .border(
                        width = if (isSelected) 3.dp else 0.dp,
                        color = if (isSelected) Color.Black else Color.Transparent, // Changed to Black
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSlotSelect(slot) },
                contentAlignment = Alignment.Center
            ) {
                if (slot.imageRes != null) {
                    Image(
                        painter = painterResource(id = slot.imageRes),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}
