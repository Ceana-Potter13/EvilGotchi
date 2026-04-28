package com.example.zybooks.ui.theme.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.zybooks.R
import com.example.zybooks.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    val sharedPreferences = remember { context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE) }
    val userEmail = sharedPreferences.getString("current_user", "")
    val eggId = sharedPreferences.getInt("${userEmail}_eggId", 1)

    // --- PET STATS ---
    val hunger = sharedPreferences.getInt("${userEmail}_hunger", 100)
    val hydration = sharedPreferences.getInt("${userEmail}_hydration", 100)
    val happiness = sharedPreferences.getInt("${userEmail}_happiness", 0)

    var isMovingRight by remember { mutableStateOf(true) }

    // --- ROTATION  ---
    val rotationAnimatable = remember { Animatable(0f) }
    var lastOrientation by remember { mutableIntStateOf(configuration.orientation) }

    LaunchedEffect(configuration.orientation) {
        if (lastOrientation != configuration.orientation) {
            val snapAngle = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) -90f else 90f
            rotationAnimatable.snapTo(snapAngle)
            rotationAnimatable.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessVeryLow
                )
            )
            lastOrientation = configuration.orientation
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val barSize = if (isLandscape) screenWidth * 0.1f else screenHeight * 0.1f
        val actionBoxSize = barSize * 0.4f

        val statsAreaHeight = if (!isLandscape) screenHeight * 0.25f else 0.dp
        val statsAreaWidth = if (isLandscape) screenWidth * 0.35f else 0.dp

        // --- EVILGOTCHI SIZE & ANIMATION ---
        val petBaseSize = 400.dp
        
        // tracking target positions for the pet to wander to
        var targetX by remember { mutableStateOf(screenWidth / 2 - 125.dp) }
        var targetY by remember { mutableStateOf(screenHeight / 2) }

        //  animations
        val animX by animateDpAsState(
            targetValue = targetX,
            animationSpec = tween(Random.nextInt(2000, 4000), easing = LinearOutSlowInEasing),
            label = "petX"
        )
        val animY by animateDpAsState(
            targetValue = targetY,
            animationSpec = tween(Random.nextInt(2000, 4000), easing = LinearOutSlowInEasing),
            label = "petY"
        )
        
        // checking the direction based on next movement
        LaunchedEffect(targetX) {
            if (targetX > animX) {
                isMovingRight = true
            } else if (targetX < animX) {
                isMovingRight = false
            }
        }
        
        // the bobbing effect
        val infiniteTransition = rememberInfiniteTransition(label = "bob")
        val bobOffset by infiniteTransition.animateFloat(
            initialValue = -5f,
            targetValue = 5f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bobOffset"
        )

        // random wandering logic
        LaunchedEffect(isLandscape) {
            while (true) {
                // bings gotchi to within viewable area

                // Left margin (navbar in landscape or just edge in portrait)
                val minX = if (isLandscape) barSize + 10.dp else 10.dp
                // Right margin (stats area in landscape or just edge in portrait)
                val maxX = if (isLandscape) screenWidth - statsAreaWidth - 200.dp else screenWidth - 200.dp
                
                // Top margin (navbar in portrait or just edge in landscape)
                val minY = if (isLandscape) 10.dp else barSize + 60.dp
                // Bottom margin (stats area in portrait or just edge in landscape)
                val maxY = if (isLandscape) screenHeight - 200.dp else screenHeight - statsAreaHeight - 200.dp

                val startX = minX.value.toInt()
                val endX = maxX.value.toInt().coerceAtLeast(startX + 50)
                val startY = minY.value.toInt()
                val endY = maxY.value.toInt().coerceAtLeast(startY + 50)

                targetX = Random.nextInt(startX, endX).dp
                targetY = Random.nextInt(startY, endY).dp
                
                delay(Random.nextLong(3000, 6000))
            }
        }

        // depth effect i.e. gets smaller as it goes "up" (smaller Y)
        val topLimit = if (isLandscape) 0.dp else barSize + 50.dp
        val bottomLimit = if (isLandscape) screenHeight else screenHeight - statsAreaHeight
        val verticalProgress = ((animY - topLimit) / (bottomLimit - topLimit)).coerceIn(0f, 1f)
        val petScale = 0.4f + (verticalProgress * 0.6f) 

        // --- PET START ---
        EvilGotchiPet(
            eggId = eggId,
            modifier = Modifier
                .offset(x = animX, y = animY + bobOffset.dp)
                .rotate(rotationAnimatable.value)
                .scale(petScale)
                .graphicsLayer {
                    // flip horizontally based on direction
                    rotationY = if (isMovingRight) 0f else 180f
                }
                .size(petBaseSize)
        )

        // --- STATS & INVENTORY OVERLAY ---
        Row(
            modifier = Modifier
                .then(
                    if (isLandscape) {
                        Modifier
                            .align(Alignment.CenterEnd)
                            .width(statsAreaWidth)
                            .fillMaxHeight()
                    } else {
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(statsAreaHeight)
                    }
                )
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                StatBar(label = "Hunger", value = hunger, barColor = HungerGreen, bgColor = HungerGreenDark, isLandscape = isLandscape)
                StatBar(label = "Hydration", value = hydration, barColor = HydrationBlue, bgColor = HydrationBlueDark, isLandscape = isLandscape)
                StatBar(label = "Happiness", value = happiness, barColor = HappinessYellow, bgColor = HappinessYellowDark, isLandscape = isLandscape)
            }

            // The 3 function boxes that touch the bottom right edge
            Column(
                modifier = Modifier
                    .width(actionBoxSize)
                    .fillMaxHeight(),
                verticalArrangement = if (isLandscape) Arrangement.Center else Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                repeat(3) {
                    ActionBox(size = actionBoxSize)
                }
            }
        }

        // --- NAV OVERLAY ---
        NavBar(navController = navController, selectedIndex = 1)
    }
}

@Composable
fun StatBar(label: String, value: Int, barColor: Color, bgColor: Color, isLandscape: Boolean) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = if (isLandscape) 10.sp else 12.sp
        )
        if (isLandscape) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$value/100",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.width(60.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(bgColor)
                        .border(1.dp, Color.Gray, RoundedCornerShape(6.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(value / 100f)
                            .fillMaxHeight()
                            .background(barColor)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .border(1.dp, Color.Gray, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(value / 100f)
                        .fillMaxHeight()
                        .align(Alignment.CenterStart)
                        .background(barColor)
                )
                Text(
                    text = "$value/100",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ActionBox(size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .border(1.dp, PlainBlack)
            .background(PlainWhite)
            .clickable { /* logic later */ }
    )
}

@Composable
fun EvilGotchiPet(eggId: Int, modifier: Modifier = Modifier) {
    val petResource = when (eggId) {
        1 -> R.drawable.babykuna // Payton
        2 -> R.drawable.babykuna // Ceaana
        3 -> R.drawable.babykuna // Will
        4 -> R.drawable.babykuna // Kola
        else -> R.drawable.babykuna
    }
    Image(
        painter = painterResource(id = petResource),
        contentDescription = "Your EvilGotchi",
        modifier = modifier
    )
}
