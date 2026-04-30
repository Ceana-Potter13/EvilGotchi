package com.example.zybooks.ui.theme.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.zybooks.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun EvolutionScreen(navController: NavController) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: ""
    
    val sharedPrefs = remember { context.getSharedPreferences("EvilGotchiPrefs", Context.MODE_PRIVATE) }
    val highestStage = remember { mutableStateOf(sharedPrefs.getString("highest_stage_$userId", "Baby") ?: "Baby") }
    val eggId = remember { mutableIntStateOf(1) }

    LaunchedEffect(Unit) {
        if (userId.isNotEmpty()) {
            db.collection("users").document(userId).get().addOnSuccessListener { snapshot ->
                snapshot.getLong("eggId")?.toInt()?.let { eggId.intValue = it }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background - Crop ensures it covers without warping
        Image(
            painter = painterResource(id = R.drawable.progressbackground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Back Button
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(48.dp)
                .background(Color.Black.copy(alpha = 0.3f), shape = CircleShape)
                .clickable { navController.popBackStack() }
                .align(Alignment.TopStart),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.nextstage),
                contentDescription = "Back",
                modifier = Modifier.size(32.dp).rotate(180f)
            )
        }

        // Evolution Stages
        val stages = listOf("Egg", "Baby", "Teen", "Adult", "Elder")
        val reachedIndex = when (highestStage.value) {
            "Baby" -> 1
            "Teen" -> 2
            "Adult" -> 3
            "Elder" -> 4
            else -> 1
        }

        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 40.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                stages.forEachIndexed { index, stage ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        EvolutionItem(
                            stage = stage,
                            eggId = eggId.intValue,
                            isReached = index <= reachedIndex,
                            isLandscape = true
                        )
                    }
                    if (index < stages.size - 1) {
                        Image(
                            painter = painterResource(id = R.drawable.nextstage),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 100.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                stages.forEachIndexed { index, stage ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        EvolutionItem(
                            stage = stage,
                            eggId = eggId.intValue,
                            isReached = index <= reachedIndex,
                            isLandscape = false
                        )
                    }
                    if (index < stages.size - 1) {
                        Image(
                            painter = painterResource(id = R.drawable.nextstage),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp).rotate(90f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EvolutionItem(stage: String, eggId: Int, isReached: Boolean, isLandscape: Boolean) {
    val context = LocalContext.current
    val resId = when (stage) {
        "Egg" -> when (eggId) {
            1 -> R.drawable.paytonegg
            2 -> R.drawable.ceanaegg
            3 -> R.drawable.willegg
            4 -> R.drawable.kolaegg
            else -> R.drawable.paytonegg
        }
        "Baby" -> getPetResource(eggId, "Baby")
        "Teen" -> getPetResource(eggId, "Teen")
        "Adult" -> getPetResource(eggId, "Adult")
        "Elder" -> getPetResource(eggId, "Elder")
        else -> R.drawable.babykuna
    }

    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    val sizeMod = if (isLandscape) {
        Modifier.fillMaxWidth(0.9f)
    } else {
        Modifier.fillMaxHeight(0.9f)
    }

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(resId)
            .build(),
        contentDescription = stage,
        imageLoader = imageLoader,
        modifier = sizeMod,
        colorFilter = if (isReached) null else ColorFilter.tint(Color.Black),
        contentScale = ContentScale.Fit
    )
}
