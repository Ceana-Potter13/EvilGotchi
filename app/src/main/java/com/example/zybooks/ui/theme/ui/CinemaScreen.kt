package com.example.zybooks.ui.theme.ui

import android.content.res.Configuration
import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val navBarSize = 80.dp

    Box(modifier = modifier.fillMaxSize()) {
        val contentModifier = if (isLandscape) {
            Modifier.padding(start = navBarSize)
        } else {
            Modifier.padding(top = navBarSize)
        }

        Scaffold(
            bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    color = Color(0xFFEAEAEA),
                    border = BorderStroke(1.dp, Color.Black)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.align(Alignment.CenterStart).padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.coin),
                                contentDescription = "Coins",
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Coins: ${uiState.coins}",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            },
            modifier = contentModifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Background Image with chairs
                Image(
                    painter = painterResource(R.drawable.cinema),
                    contentDescription = "Cinema Image",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onImageClick() }
                )

                // Screen measurements
                val adWidthScale = if (isLandscape) 0.95f else 0.9f
                val topPadding = if (isLandscape) 10.dp else 110.dp

                // Watch Ad area (Cinema Screen + Button)
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(topPadding))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(adWidthScale)
                            .then(if (isLandscape) Modifier.fillMaxHeight(0.8f) else Modifier.height(280.dp))
                            .background(if (isLandscape) Color.Black else Color.White)
                            .then(if (isLandscape) Modifier.border(4.dp, Color.Gray) else Modifier),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.showGif) {
                            GifImage(
                                data = uiState.currentAd,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (uiState.showPlusFive) {
                            Image(
                                painter = painterResource(id = R.drawable.plusfive),
                                contentDescription = "+5 Coins",
                                modifier = Modifier.size(150.dp)
                            )
                        }
                    }

                    if (!uiState.showGif && !uiState.showPlusFive) {
                        Spacer(modifier = Modifier.height(if (isLandscape) 20.dp else 30.dp))
                        Button(
                            onClick = onPlayClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C6BC0))
                        ) {
                            Text("Watch Ad", color = Color.White)
                        }
                    }
                }
            }
        }

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
