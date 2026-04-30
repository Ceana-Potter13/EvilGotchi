package com.example.zybooks.ui.theme.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.example.zybooks.R
import com.example.zybooks.ui.theme.MutedCrimson
import com.example.zybooks.ui.theme.NuniitoFontFamily
import com.example.zybooks.ui.theme.SoftWhite
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

//this is the screen to show the egg hatching and confirming your choice. once you press the button,
//the egg you picked is stored in the cloud on firebase so on different devices your information is
//saved
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EggHatch(
    navController: NavController,
    eggId: Int,
    modifier: Modifier = Modifier,
    viewModel: EggViewModel = EggViewModel()
) {
    val egg = viewModel.getEgg(eggId)

    Scaffold(
        modifier = modifier,
        containerColor = MutedCrimson,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Hatching...",
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
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.65f)
                        .background(SoftWhite)
                        .border(2.dp, Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (egg.id == 1) {
                        AsyncImage(
                            model = R.drawable.newhatch,
                            contentDescription = "Selected Egg",
                            modifier = Modifier.size(200.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else if (egg.id == 2) {
                        AsyncImage(
                            model = R.drawable.ceannahatch1,
                            contentDescription = "Selected Egg",
                            modifier = Modifier.size(200.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else if (egg.id == 3) {
                        AsyncImage(
                            model = R.drawable.willhatch2,
                            contentDescription = "Selected Egg",
                            modifier = Modifier.size(600.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else if (egg.id == 4) {
                        AsyncImage(
                            model = R.drawable.eggkunahatch,
                            contentDescription = "Selected Egg",
                            modifier = Modifier.size(200.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else if (egg.imageId != 0) {
                        AsyncImage(
                            model = egg.imageId,
                            contentDescription = "Selected Egg",
                            modifier = Modifier.size(200.dp),
                            contentScale = ContentScale.Fit
                        )
                    }else {
                        Text(text = "Egg not found", color = Color.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            //this is the button i used to navigate to the homescreen
            //this button also saves your sprite choice in firebase
            //**** WHOEVER CODES THE HOMESCREEN, MAKE SURE THE ROUTE NAMES MATCH SO
            //THE APP DOESN'T CRASH!!***
            Button(
                onClick = {
                    val auth = FirebaseAuth.getInstance()
                    val db = FirebaseFirestore.getInstance()
                    val userId = auth.currentUser?.uid

                    if (userId != null) {
                        val userData = hashMapOf(
                            "eggId" to eggId,
                            "petStage" to "Baby"
                        )
                        db.collection("users").document(userId)
                            .set(userData)
                            .addOnSuccessListener {
                                Log.d("Firestore", "Egg ID successfully saved!")
                                navController.navigate("homescreen")
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error saving Egg ID", e)
                                // Still navigate or show error
                                navController.navigate("homescreen")
                            }
                    } else {
                        navController.navigate("homescreen")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(bottom = 32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SoftWhite)
            ) {
                Text(
                    text = "READY TO CARE FOR YOUR PET?",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = NuniitoFontFamily,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
