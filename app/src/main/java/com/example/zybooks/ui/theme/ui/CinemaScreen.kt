package com.example.zybooks.ui.theme.ui

import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.zybooks.R
import com.example.zybooks.ui.theme.ProjectFirstGoTheme

/**
 * CinemaScreen is the entry point called from MainActivity.
 * It connects the UI to the ViewModel and handles navigation.
 */
@Composable
fun CinemaScreen(
    navController: NavController,
    viewModel: CinemaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    CinemaContent(
        uiState = uiState,
        navController = navController,
        onImageClick = { viewModel.onCinemaClicked() },
        onPlayClick = { viewModel.toggleGif() }
    )
}

/**
 * CinemaContent is the "UI only" part.
 * It is stateless and decoupled from navigation and ViewModel.
 */
@Composable
fun CinemaContent(
    uiState: CinemaUiState,
    navController: NavController,
    onImageClick: () -> Unit,
    modifier: Modifier = Modifier,
    onPlayClick: () -> Unit = {}
) {
    // Wrap everything in a Box to allow NavBar to overlay the entire screen,
    // ensuring its size calculations (based on screen constraints) match HomeScreen.
    Box(modifier = modifier.fillMaxSize()) {
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
                            text = "Coins: ${uiState.coins}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                // Background Image
                Image(
                    painter = painterResource(R.drawable.cinema),
                    contentDescription = "Cinema Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onImageClick() }
                )

                // Exact positioning for the GIF - Aligned to TopCenter for easy horizontal centering
                if (uiState.showGif) {
                    GifImage(
                        data = uiState.currentAd,
                        modifier = Modifier
                            .size(293.dp)
                            .align(Alignment.TopCenter)
                            .offset(x = uiState.gifX.dp, y = uiState.gifY.dp)
                    )
                }

                // Show +5 Coins reward in the exact same spot as the ad
                if (uiState.showPlusFive) {
                    Image(
                        painter = painterResource(id = R.drawable.plusfive),
                        contentDescription = "+5 Coins",
                        modifier = Modifier
                            .size(293.dp)
                            .align(Alignment.TopCenter)
                            .offset(x = uiState.gifX.dp, y = uiState.gifY.dp)
                    )
                }

                // Watch Ad button - only visible if neither an ad nor the reward is showing
                if (!uiState.showGif && !uiState.showPlusFive) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = onPlayClick,
                            modifier = Modifier.offset(y = 50.dp)
                        ) {
                            Text("Watch Ad")
                        }
                    }
                }
            }
        }
        
        // NavBar as a top-level overlay, matching HomeScreen's placement and sizing logic
        NavBar(navController = navController, selectedIndex = 2)
    }
}

@Composable
fun GifImage(
    data: Int,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(data)
            .build(),
        contentDescription = null,
        imageLoader = imageLoader,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

@Preview(showBackground = true)
@Composable
fun CinemaScreenPreview() {
    val navController = rememberNavController()
    ProjectFirstGoTheme {
        CinemaContent(
            uiState = CinemaUiState(title = "Preview Cinema"),
            navController = navController,
            onImageClick = {}
        )
    }
}
