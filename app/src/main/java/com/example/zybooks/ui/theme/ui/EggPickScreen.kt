package com.example.zybooks.ui.theme.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.zybooks.ui.theme.MutedCrimson
import com.example.zybooks.ui.theme.NuniitoFontFamily
import com.example.zybooks.ui.theme.SoftWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EggScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: EggViewModel = EggViewModel()
){
    //used this to keep track of the selected egg id for the hatching animation
    var selectedEggId by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        modifier = modifier,
        containerColor = MutedCrimson,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Select Your EGG.",
                        color = SoftWhite,
                        textAlign = TextAlign.Center,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = NuniitoFontFamily
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MutedCrimson
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ){
            //sets up the 2x2 egg screen
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp)
            ){
                itemsIndexed(listOf(1, 2, 3, 4)){ index, id ->
                    val egg = viewModel.getEgg(id)
                    val isSelected = selectedEggId == id

                    Button(
                        onClick = { selectedEggId = id },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.6f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SoftWhite),
                        border = if (isSelected) BorderStroke(4.dp, Color.Yellow) else null
                    ) {
                        //displays the egg based on its id
                        if (egg.imageId != 0) {
                            AsyncImage(
                                model = egg.imageId,
                                contentDescription = "Egg $id",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
            //this button navigates to the hatchscreen, when the button is clicked,
            //it ensures that the selected egg id is passed to the hatchscreen which
            //in turn, displays the correct hatching animation
            Button(
                onClick = {
                    if (selectedEggId != null) {
                        navController.navigate("hatchscreen/$selectedEggId")
                    }
                },
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(containerColor = SoftWhite),
                enabled = selectedEggId != null
            ){
                Text(
                    text = "CONFIRM YOUR CHOICE.",
                    fontFamily = NuniitoFontFamily,
                    color = if (selectedEggId != null) MutedCrimson else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}