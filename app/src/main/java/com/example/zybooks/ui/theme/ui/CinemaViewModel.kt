package com.example.zybooks.ui.theme.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.example.zybooks.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class CinemaViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CinemaUiState())
    val uiState: StateFlow<CinemaUiState> = _uiState.asStateFlow()

    private var adJob: Job? = null
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val ads = listOf(
        R.drawable.ad1,
        R.drawable.ad2,
        R.drawable.ad3,
        R.drawable.ad4,
        R.drawable.ad5,
        R.drawable.ad6,
        R.drawable.ad7
    )

    init {
        listenToUserCoins()
    }

    private fun listenToUserCoins() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .addSnapshotListener { snapshot: DocumentSnapshot?, _: FirebaseFirestoreException? ->
                val serverCoins = snapshot?.getLong("coins")?.toInt() ?: 0
                _uiState.update { it.copy(coins = serverCoins) }
            }
    }

    fun onCinemaClicked() {
        // Handle click
    }

    fun toggleGif() {
        adJob?.cancel()

        _uiState.update {
            val newShowGif = !it.showGif
            val newAd = if (newShowGif) ads[Random.nextInt(ads.size)] else it.currentAd
            it.copy(showGif = newShowGif, currentAd = newAd, showPlusFive = false)
        }

        if (_uiState.value.showGif) {
            adJob = viewModelScope.launch {
                delay(10_000L)
                _uiState.update { it.copy(showGif = false, showPlusFive = true) }
                updateCoinsInFirebase(5)
                delay(3_000L)
                _uiState.update { it.copy(showPlusFive = false) }
            }
        }
    }

    private fun updateCoinsInFirebase(amount: Int) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .update("coins", FieldValue.increment(amount.toLong()))
    }

    fun updateGifPosition(x: Float, y: Float) {
        _uiState.update { it.copy(gifX = x, gifY = y) }
    }
}

data class CinemaUiState(
    val title: String = "Cinema Screen",
    val isLoading: Boolean = false,
    val showGif: Boolean = false,
    val showPlusFive: Boolean = false,
    val currentAd: Int = R.drawable.ad1,
    val gifX: Float = 0f,
    val gifY: Float = 135f,
    val coins: Int = 0
)
