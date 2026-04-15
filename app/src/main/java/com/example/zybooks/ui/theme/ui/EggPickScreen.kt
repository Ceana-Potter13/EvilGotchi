package com.example.zybooks.ui.theme.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.zybooks.ui.theme.Charcoal
import com.example.zybooks.ui.theme.DarkRed
import com.example.zybooks.ui.theme.MutedCrimson
import com.example.zybooks.ui.theme.MutedGray
import com.example.zybooks.ui.theme.SoftBlack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EggScreen(
    modifier: Modifier = Modifier,
    viewModel: EggViewModel = EggViewModel()
){
    Scaffold(
        modifier = modifier,
        containerColor = SoftBlack,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Select Your EGG.",
                        color = MutedCrimson,
                        textAlign = TextAlign.Center,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Charcoal
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ){
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
                    Button(
                        onClick = { /* TODO */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.6f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkRed)
                    ) {
                        if (egg.imageId != 0) {
                            AsyncImage(
                                model = egg.imageId,
                                contentDescription = "Egg $id",
                                modifier = Modifier.size(100.dp)
                            )
                        }
                    }
                }
            }
            Button(
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(containerColor = MutedGray)
            ){
                Text(
                    text = "CONFIRM YOUR CHOICE.",
                    fontFamily = FontFamily.Serif,
                    color = MutedCrimson,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}