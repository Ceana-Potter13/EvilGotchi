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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.zybooks.R
import com.example.zybooks.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlin.random.Random

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    val sharedPrefs = remember { context.getSharedPreferences("EvilGotchiPrefs", Context.MODE_PRIVATE) }
    
    // --- PET STATS ---
    var hunger by remember { mutableIntStateOf(100) }
    var hydration by remember { mutableIntStateOf(100) }
    var happiness by remember { mutableIntStateOf(0) }
    var coins by remember { mutableIntStateOf(0) }
    var petStage by remember { mutableStateOf("Baby") }
    var eggId by remember { mutableIntStateOf(1) }

    var isTimeFrozen by remember { mutableStateOf(false) }
    var isFastForwarding by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid ?: return@LaunchedEffect
        val docRef = db.collection("users").document(userId)

        // listener for UI updates
        docRef.addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                coins = snapshot.getLong("coins")?.toInt() ?: 0
                snapshot.getLong("hunger")?.toInt()?.let { hunger = it }
                snapshot.getLong("hydration")?.toInt()?.let { hydration = it }
                snapshot.getLong("happiness")?.toInt()?.let { happiness = it }
                snapshot.getString("petStage")?.let { petStage = it }
                snapshot.getLong("eggId")?.toInt()?.let { eggId = it }
            }
        }
    }

    LaunchedEffect(isFastForwarding, isTimeFrozen) {
        val userId = auth.currentUser?.uid ?: return@LaunchedEffect
        val docRef = db.collection("users").document(userId)

        // Continuous decay loop
        while (true) {
            val tickRate = if (isFastForwarding) 1000L else 60000L
            docRef.get().addOnSuccessListener { snapshot ->
                val currentTime = System.currentTimeMillis()
                
                if (snapshot.exists()) {
                    val savedHunger = snapshot.getLong("hunger")?.toInt() ?: 100
                    val savedHydration = snapshot.getLong("hydration")?.toInt() ?: 100
                    val savedHappiness = snapshot.getLong("happiness")?.toInt() ?: 0
                    val savedStage = snapshot.getString("petStage") ?: "Baby"
                    
                    var lastDecayTime = snapshot.getLong("lastDecayTime") ?: currentTime
                    var lastGrowthTime = snapshot.getLong("lastGrowthTime") ?: currentTime
                    var lastHappinessDecayTime = snapshot.getLong("lastHappinessDecayTime") ?: currentTime

                    // If time is frozen, keep pushing lastDecayTime forward so hunger/thirst never decay.
                    if (isTimeFrozen) {
                        lastDecayTime = currentTime
                    }

                    if (isFastForwarding) {
                        if (!isTimeFrozen) {
                            lastDecayTime -= 59000L
                        }
                        lastGrowthTime -= 59000L
                        lastHappinessDecayTime -= 59000L
                    }

                    val decayMinutes = ((currentTime - lastDecayTime) / 60000).toInt()
                    val growthMinutes = ((currentTime - lastGrowthTime) / 60000).toInt()
                    val happinessDecayMinutes = ((currentTime - lastHappinessDecayTime) / 60000).toInt()

                    var currentHunger = savedHunger
                    var currentHydration = savedHydration
                    var currentHappiness = savedHappiness
                    var currentStage = savedStage
                    var updated = false

                    var newLastDecayTime = lastDecayTime
                    var newLastGrowthTime = lastGrowthTime
                    var newLastHappinessDecayTime = lastHappinessDecayTime

                    if (decayMinutes >= 2) {
                        val decayTicks = decayMinutes / 2
                        currentHunger = (currentHunger - decayTicks).coerceAtLeast(0)
                        currentHydration = (currentHydration - decayTicks).coerceAtLeast(0)
                        newLastDecayTime += (decayTicks * 2 * 60000L)
                        updated = true
                    }

                    if (growthMinutes >= 5) {
                        val growthTicks = growthMinutes / 5
                        if (currentHunger > 50 && currentHydration > 50) {
                            currentHappiness += (growthTicks * 5)
                            
                            while (currentHappiness >= 100 && currentStage != "Elder") {
                                currentHappiness -= 100
                                currentStage = when (currentStage) {
                                    "Baby" -> "Teen"
                                    "Teen" -> "Adult"
                                    "Adult" -> "Elder"
                                    else -> currentStage
                                }
                                
                                val stageList = listOf("Baby", "Teen", "Adult", "Elder")
                                val currentIdx = stageList.indexOf(currentStage)
                                val highestStored = sharedPrefs.getString("highest_stage_${userId}", "Baby") ?: "Baby"
                                val highestIdx = stageList.indexOf(highestStored)
                                if (currentIdx > highestIdx) {
                                    sharedPrefs.edit().putString("highest_stage_${userId}", currentStage).apply()
                                }
                            }
                            
                            if (currentStage == "Elder" && currentHappiness > 100) {
                                currentHappiness = 100 
                            }
                        }
                        newLastGrowthTime += (growthTicks * 5 * 60000L)
                        updated = true
                    }
                    
                    if (happinessDecayMinutes >= 1) {
                        var decayAmount = 0
                        if (currentHunger < 50) decayAmount += happinessDecayMinutes
                        if (currentHydration < 50) decayAmount += happinessDecayMinutes
                        
                        if (decayAmount > 0) {
                            currentHappiness = (currentHappiness - decayAmount).coerceAtLeast(0)
                        }
                        newLastHappinessDecayTime += (happinessDecayMinutes * 60000L)
                        updated = true
                    }

                    if (updated || isFastForwarding) {
                        docRef.update(
                            mapOf(
                                "hunger" to currentHunger,
                                "hydration" to currentHydration,
                                "happiness" to currentHappiness,
                                "petStage" to currentStage,
                                "lastDecayTime" to newLastDecayTime,
                                "lastGrowthTime" to newLastGrowthTime,
                                "lastHappinessDecayTime" to newLastHappinessDecayTime
                            )
                        )
                    } else if (!snapshot.contains("hunger")) {
                        docRef.update(
                            mapOf(
                                "hunger" to 100,
                                "hydration" to 100,
                                "happiness" to 0,
                                "petStage" to "Baby",
                                "lastDecayTime" to currentTime,
                                "lastGrowthTime" to currentTime,
                                "lastHappinessDecayTime" to currentTime
                            )
                        )
                    }
                }
            }
            delay(tickRate)
        }
    }

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

    Row(modifier = Modifier.fillMaxSize()) {
        if (isLandscape) {
            NavBar(navController = navController, selectedIndex = 1)
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight
            
            // --- BARRIER MEASUREMENTS ---
            // Define areas occupied by UI elements that the pet should avoid.
            val navBarTopHeight = if (!isLandscape) 80.dp else 0.dp
            val statsOverlayWidth = if (isLandscape) 332.dp else 0.dp
            val statsOverlayHeight = if (!isLandscape) 260.dp else 0.dp

            // --- DYNAMIC PET SIZE ---
            // Base size is relative to screen width, but capped for large screens.
            val baseSize = (screenWidth * 0.5f).coerceAtMost(350.dp)
            val stageMultiplier = when (petStage) {
                "Baby" -> 0.7f
                "Teen" -> 0.85f
                "Adult" -> 1.0f
                "Elder" -> 1.1f
                else -> 1.0f
            }
            val finalPetSize = baseSize * stageMultiplier

            // --- BACKGROUND IMAGE ---
            Image(
                painter = painterResource(id = R.drawable.training_stage3),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            if (!isLandscape) {
                NavBar(navController = navController, selectedIndex = 1)
            }
            
            // tracking target positions for the pet to wander to
            var targetX by remember { mutableStateOf(screenWidth / 2 - (finalPetSize / 2)) }
            var targetY by remember { mutableStateOf(screenHeight / 2 - (finalPetSize / 2)) }

            //  animations
            val animX by animateDpAsState(
                targetValue = targetX,
                animationSpec = tween(Random.nextInt(3000, 5000), easing = LinearOutSlowInEasing),
                label = "petX"
            )
            val animY by animateDpAsState(
                targetValue = targetY,
                animationSpec = tween(Random.nextInt(3000, 5000), easing = LinearOutSlowInEasing),
                label = "petY"
            )
            
            LaunchedEffect(targetX) {
                if (targetX > animX) {
                    isMovingRight = true
                } else if (targetX < animX) {
                    isMovingRight = false
                }
            }
            
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

            // random wandering logic with dynamic boundaries
            LaunchedEffect(isLandscape, screenWidth, screenHeight, petStage) {
                while (true) {
                    // Walkable area calculation:
                    // Min X is 10dp padding.
                    // Max X excludes the Stats Overlay in Landscape.
                    // Min Y excludes the NavBar in Portrait.
                    // Max Y excludes the Stats Overlay in Portrait.
                    
                    val minXLimit = 10.dp
                    val maxXLimit = (screenWidth - statsOverlayWidth - finalPetSize - 10.dp).coerceAtLeast(minXLimit + 20.dp)
                    
                    val minYLimit = navBarTopHeight + 20.dp
                    val maxYLimit = (screenHeight - statsOverlayHeight - finalPetSize - 10.dp).coerceAtLeast(minYLimit + 20.dp)

                    targetX = Random.nextInt(minXLimit.value.toInt(), maxXLimit.value.toInt()).dp
                    targetY = Random.nextInt(minYLimit.value.toInt(), maxYLimit.value.toInt()).dp
                    
                    delay(Random.nextLong(4000, 8000))
                }
            }

            // depth effect (scaling) based on vertical position
            val topBound = navBarTopHeight + 20.dp
            val bottomBound = screenHeight - statsOverlayHeight - finalPetSize
            val verticalProgress = if (bottomBound > topBound) {
                ((animY - topBound) / (bottomBound - topBound)).coerceIn(0f, 1f)
            } else 0f
            val petScale = 0.5f + (verticalProgress * 0.5f) 

            // --- PET START ---
            EvilGotchiPet(
                eggId = eggId,
                petStage = petStage,
                baseSize = finalPetSize,
                modifier = Modifier
                    .offset(x = animX, y = animY + bobOffset.dp)
                    .rotate(rotationAnimatable.value)
                    .scale(petScale)
                    .graphicsLayer {
                        rotationY = if (isMovingRight) 0f else 180f
                    }
            )

            // --- STATS & INVENTORY OVERLAY ---
            Box(
                modifier = Modifier
                    .align(if (isLandscape) Alignment.TopEnd else Alignment.BottomCenter)
                    .then(if (isLandscape) Modifier.fillMaxHeight().width(332.dp) else Modifier.fillMaxWidth().height(260.dp))
                    .background(MutedCrimson, shape = if (isLandscape) RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp) else RoundedCornerShape(0.dp))
                    .padding(bottom = 16.dp, top = if (isLandscape) 16.dp else 8.dp, start = 16.dp, end = if (isLandscape) 16.dp else 0.dp)
            ) {
                if (isLandscape) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            DevToolsRow(
                                isTimeFrozen = isTimeFrozen,
                                onFreezeToggle = { isTimeFrozen = !isTimeFrozen },
                                onFastForwardChange = { isFastForwarding = it }
                            )
                            Image(
                                painter = painterResource(id = R.drawable.gotchi_progress),
                                contentDescription = "Progress",
                                modifier = Modifier.size(50.dp).clickable { }
                            )
                            Image(
                                painter = painterResource(id = R.drawable.camerabutton),
                                contentDescription = "Camera",
                                modifier = Modifier.size(50.dp).clickable { }
                            )
                            Text(
                                text = "$ $coins",
                                color = HappinessYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }

                        Column(
                            modifier = Modifier.width(300.dp).padding(bottom = 32.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            StatBar(label = "Hunger", value = hunger, barColor = HungerGreen, bgColor = HungerGreenDark, iconRes = R.drawable.hungerbarsymbol)
                            StatBar(label = "Thirst", value = hydration, barColor = HydrationBlue, bgColor = HydrationBlueDark, iconRes = R.drawable.thirstbarsymbol)
                            StatBar(label = "Happiness", value = happiness, barColor = HappinessYellow, bgColor = HappinessYellowDark, iconRes = R.drawable.happinessbarsymbol)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End
                    ) {
                        Box(modifier = Modifier.padding(end = 16.dp, bottom = 8.dp)) {
                            DevToolsRow(
                                isTimeFrozen = isTimeFrozen,
                                onFreezeToggle = { isTimeFrozen = !isTimeFrozen },
                                onFastForwardChange = { isFastForwarding = it }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$ $coins",
                                color = HappinessYellow,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f).padding(start = 32.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatBar(label = "Hunger", value = hunger, barColor = HungerGreen, bgColor = HungerGreenDark, iconRes = R.drawable.hungerbarsymbol)
                                StatBar(label = "Thirst", value = hydration, barColor = HydrationBlue, bgColor = HydrationBlueDark, iconRes = R.drawable.thirstbarsymbol)
                                StatBar(label = "Happiness", value = happiness, barColor = HappinessYellow, bgColor = HappinessYellowDark, iconRes = R.drawable.happinessbarsymbol)
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.gotchi_progress),
                                    contentDescription = "Progress",
                                    modifier = Modifier.size(50.dp).clickable { }
                                )
                                Image(
                                    painter = painterResource(id = R.drawable.camerabutton),
                                    contentDescription = "Camera",
                                    modifier = Modifier.size(50.dp).clickable { }
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
fun StatBar(label: String, value: Int, barColor: Color, bgColor: Color, iconRes: Int) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor)
                    .border(1.dp, Color.Gray, RoundedCornerShape(10.dp)),
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
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun EvilGotchiPet(eggId: Int, petStage: String, baseSize: Dp, modifier: Modifier = Modifier) {
    val petResource = when (petStage) {
        "Teen" -> when (eggId) {
            1 -> R.drawable.babykuna // Payton
            2 -> R.drawable.babykuna // Ceaana
            3 -> R.drawable.willteen // Will
            4 -> R.drawable.babykuna // Kola
            else -> R.drawable.babykuna
        }
        "Adult" -> when (eggId) {
            1 -> R.drawable.babykuna // Payton
            2 -> R.drawable.babykuna // Ceaana
            3 -> R.drawable.willadult // Will
            4 -> R.drawable.babykuna // Kola
            else -> R.drawable.babykuna
        }
        "Elder" -> when (eggId) {
            1 -> R.drawable.babykuna // Payton
            2 -> R.drawable.babykuna // Ceaana
            3 -> R.drawable.willelder // Will
            4 -> R.drawable.babykuna // Kola
            else -> R.drawable.babykuna
        }
        else -> when (eggId) { // "Baby"
            1 -> R.drawable.babykuna // Payton
            2 -> R.drawable.babykuna // Ceaana
            3 -> R.drawable.willbaby // Will
            4 -> R.drawable.babykuna // Kola
            else -> R.drawable.babykuna
        }
    }
    
    Image(
        painter = painterResource(id = petResource),
        contentDescription = "Your EvilGotchi",
        modifier = modifier.size(baseSize)
    )
}
