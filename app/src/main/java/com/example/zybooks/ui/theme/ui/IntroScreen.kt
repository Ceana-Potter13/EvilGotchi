package com.example.zybooks.ui.theme.ui

import android.content.Context
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.navigation.NavController
import com.example.zybooks.R
import com.example.zybooks.ui.theme.LuckiestGuyFontFamily
import com.example.zybooks.ui.theme.MutedCrimson
import com.example.zybooks.ui.theme.SoftWhite
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

//first screen to run, loads for 10 seconds before navigating to the login screen
@Composable
fun LoadingScreen(navController: NavController) {
    val progress = remember { Animatable(0f) }

    //LaunchedEffect is used to start the animation when the composable is first displayed
    //and navigate to the login screen after the animation completes
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 10000,
                easing = LinearEasing
            )
        )
        //navigates to the login screen after the 10 seconds is up
        navController.navigate("loginscreen") {
            popUpTo("loadingscreen") { inclusive = true }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.loadingbackground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Text(
            text = "Evil\nGotchi",
            color = MutedCrimson,
            style = TextStyle(
                fontFamily = LuckiestGuyFontFamily,
                fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(2f, 2f),
                    blurRadius = 5f
                )
            ),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 40.dp, top = 16.dp)
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(bottom = 120.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //displays the percentage of the loading progress
            Text(
                text = "Loading... ${(progress.value * 100).toInt()}%",
                color = SoftWhite,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.background(
                    color = Color.Black.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp)
                ).padding(8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            //the bar that updates as the percentage increases
            LinearProgressIndicator(
                progress = { progress.value },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

//LogInScreen is the second screen and is displayed after the loading percentage hits 100
@Composable
fun LogInScreen(
    navController: NavController,
    labelText: String = "Email",
    labelText1: String = "Password",
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val sharedPreferences = remember { context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE) }
    val auth = FirebaseAuth.getInstance()

    //most of these were used to find out why firebase wasn't working lol
    var emailInput by remember { mutableStateOf("") }
    var passInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var showOtherOptions by remember { mutableStateOf(false) }
    var authenticatedEmail by remember { mutableStateOf<String?>(null) }

    // Navigation logic handled via LaunchedEffect when authenticatedEmail is set
    LaunchedEffect(authenticatedEmail) {
        authenticatedEmail?.let { email ->
            sharedPreferences.edit().putString("current_user", email).apply()
            val savedEggId = sharedPreferences.getInt("${email}_eggId", -1)
            
            val destination = if (savedEggId != -1) "homescreen" else "eggscreen"
            Log.d("Auth", "Navigation Triggered: $destination for user: $email")
            
            navController.navigate(destination) {
                popUpTo("loginscreen") { inclusive = true }
            }
        }
    }

    //this function handled the sign in with google feature. the server cloud id is used to
    //connect the user to the firebase database
    fun onGoogleSignIn() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("135525350330-ojve6kb2811dhlo3d6m25lruiqkfv4fo.apps.googleusercontent.com")
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        //this is where the magic happened
        // basically, if the users email successfully is authenticated by google then it will allow
        //them to sign in with google
        scope.launch {
            try {
                Log.d("Auth", "Requesting credentials...")
                val result = credentialManager.getCredential(context = context, request = request)
                val credential = result.credential
                
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                    
                    auth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userEmail = auth.currentUser?.email ?: googleIdTokenCredential.id
                                Log.d("Auth", "Firebase Login Success: $userEmail")
                                authenticatedEmail = userEmail
                            } else {
                                //if the authentification fails, an error message occurs
                                errorMessage = "Firebase Auth Failed."
                                Log.e("Auth", "Firebase error: ${task.exception?.message}")
                            }
                        }
                } else {
                    errorMessage = "Unexpected login result."
                }
            } catch (e: GetCredentialException) {
                errorMessage = "Google login failed."
                Log.e("Auth", "CredentialManager Error: ${e.message}")
            } catch (e: Exception) {
                errorMessage = "An unexpected error occurred."
                Log.e("Auth", "General error: ${e.message}")
            }
        }
    }
//if the showOtherOptions textbutton is clicked, this is what appears on the screen
    if (showOtherOptions) {
        AlertDialog(
            onDismissRequest = { showOtherOptions = false },
            title = { Text(text = "Other Login Options") },
            text = {
                Column {
                    Text(text = "Choose a service to log in with:")
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            showOtherOptions = false
                            onGoogleSignIn()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Log in with Google")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOtherOptions = false }) {
                    Text("Close")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.loadingbackground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Text(
            text = "Evil\nGotchi",
            color = MutedCrimson,
            style = TextStyle(
                fontFamily = LuckiestGuyFontFamily,
                fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(2f, 2f),
                    blurRadius = 5f
                )
            ),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 40.dp, top = 16.dp)
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(bottom = 80.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Log in",
                color = SoftWhite,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.background(
                    color = Color.Black.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp)
                ).padding(8.dp)
            )
            
            if (errorMessage.isNotBlank()) {
                Text(
                    text = errorMessage, 
                    color = Color.Red, 
                    modifier = Modifier.background(
                        color = Color.Black.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp)
                    ).padding(8.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = emailInput,
                onValueChange = { emailInput = it },
                shape = RoundedCornerShape(16.dp),
                label = { Text(labelText) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = passInput,
                onValueChange = { passInput = it },
                shape = RoundedCornerShape(16.dp),
                label = { Text(labelText1) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
            )

            Spacer(modifier = Modifier.height(24.dp))

            //this is if the user wants to manually log in instead of using google
            //used sharedPreferences to store the users email and password
            Button(
                onClick = {
                    val registeredPassword = sharedPreferences.getString(emailInput, null)
                    if (emailInput.isNotBlank() && passInput.isNotBlank()) {
                        if (registeredPassword == null) {
                            errorMessage = "Email not recognized."
                        } else if (registeredPassword != passInput) {
                            errorMessage = "Incorrect password."
                        } else {
                            authenticatedEmail = emailInput
                        }
                    } else {
                        errorMessage = "Please fill in all fields."
                    }
                },
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Text(text = "Login", color = SoftWhite)
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { navController.navigate("signupscreen") },
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Text(
                    text = "Sign Up",
                    color = SoftWhite,
                    style = TextStyle(textDecoration = TextDecoration.Underline)
                )
            }

            TextButton(
                onClick = { showOtherOptions = true },
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Text(
                    text = "Other Options",
                    color = SoftWhite,
                    style = TextStyle(textDecoration = TextDecoration.Underline)
                )
            }
        }
    }
}

//this screen is for users to register their accounts
//a user must register their account before accessing the app
@Composable
fun SignUpScreen(
    navController: NavController,
    labelText: String = "Email",
    labelText1: String = "Password",
) {
    //once again used sharedPreferences to store the users email and password associated with their account
    val context = LocalContext.current
    val sharedPreferences = remember { context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE) }

    var emailInput by remember { mutableStateOf("") }
    var passInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.loadingbackground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Text(
            text = "Evil\nGotchi",
            color = MutedCrimson,
            style = TextStyle(
                fontFamily = LuckiestGuyFontFamily,
                fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(2f, 2f),
                    blurRadius = 5f
                )
            ),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 40.dp, top = 16.dp)
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(bottom = 120.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Sign Up",
                color = SoftWhite,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.background(
                    color = Color.Black.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp)
                ).padding(8.dp)
            )

            if (errorMessage.isNotBlank()) {
                Text(text = errorMessage, color = Color.Red, modifier = Modifier.padding(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = emailInput,
                onValueChange = { emailInput = it },
                shape = RoundedCornerShape(16.dp),
                label = { Text(labelText) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email
                ),
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = passInput,
                onValueChange = { passInput = it },
                shape = RoundedCornerShape(16.dp),
                label = { Text(labelText1) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                ),
            )

            Spacer(modifier = Modifier.height(24.dp))

            //button to register and store an email and its password
            //returns to the login screen once pressed
            Button(
                onClick = {
                    if (emailInput.isNotBlank() && passInput.isNotBlank()) {
                        sharedPreferences.edit().apply {
                            putString(emailInput, passInput)
                            apply()
                        }
                        navController.navigate("loginscreen")
                    } else {
                        errorMessage = "Please fill in all fields."
                    }
                },
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Text(text = "Register", color = SoftWhite)
            }
            Spacer(modifier = Modifier.height(8.dp))
            //this button just returns the user back to the loginscreen
            TextButton(
                onClick = { navController.navigate("loginscreen") },
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Text(
                    text = "Back to Login",
                    color = SoftWhite,
                    style = TextStyle(textDecoration = TextDecoration.Underline)
                )
            }
        }
    }
}
