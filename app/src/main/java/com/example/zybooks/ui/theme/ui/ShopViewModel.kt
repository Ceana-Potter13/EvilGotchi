package com.example.zybooks.ui.theme.ui

import androidx.lifecycle.ViewModel
import com.example.zybooks.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ShopSlot(
    val id: String,
    val imageRes: Int?,
    val price: Int = 0,
    val amount: Int = 0,
    val statAffected: String? = null // "hunger", "hydration", or null for toys/cosmetics
)

data class ShopRow(
    val id: Int,
    val title: String,
    val slots: List<ShopSlot>
)

class ShopViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _userCoins = MutableStateFlow(0)
    val userCoins: StateFlow<Int> = _userCoins.asStateFlow()

    private val _rows = MutableStateFlow<List<ShopRow>>(
        listOf(
            // Row 0: FOOD
            ShopRow(0, "FOOD", listOf(
                ShopSlot("FOOD_0", R.drawable.food1, price = 5, amount = 10, statAffected = "hunger"),
                ShopSlot("FOOD_1", R.drawable.food2, price = 10, amount = 25, statAffected = "hunger"),
                ShopSlot("FOOD_2", R.drawable.food3, price = 15, amount = 40, statAffected = "hunger"),
                ShopSlot("FOOD_3", R.drawable.food4, price = 20, amount = 60, statAffected = "hunger")
            )),

            // Row 1: HYDRATION
            ShopRow(1, "HYDRATION", listOf(
                ShopSlot("DRINK_0", R.drawable.shopdrink1, price = 5, amount = 10, statAffected = "hydration"),
                ShopSlot("DRINK_1", R.drawable.shopdrink2, price = 10, amount = 25, statAffected = "hydration"),
                ShopSlot("DRINK_2", R.drawable.shopdrink3, price = 15, amount = 40, statAffected = "hydration"),
                ShopSlot("DRINK_3", R.drawable.shopdrink4, price = 20, amount = 60, statAffected = "hydration")
            )),

            // Row 2: TOYS
            ShopRow(2, "TOYS", listOf(
                ShopSlot("TOY_0", R.drawable.shoptoy1, price = 10, amount = 10, statAffected = "happiness"),
                ShopSlot("TOY_1", R.drawable.shoptoy2, price = 10, amount = 10, statAffected = "happiness"),
                ShopSlot("TOY_2", R.drawable.shoptoy3, price = 10, amount = 10, statAffected = "happiness"),
                ShopSlot("TOY_3", R.drawable.shoptoy4, price = 10, amount = 10, statAffected = "happiness")
            ))
        )
    )
    val rows: StateFlow<List<ShopRow>> = _rows.asStateFlow()

    private val _selectedSlot = MutableStateFlow<ShopSlot?>(null)
    val selectedSlot: StateFlow<ShopSlot?> = _selectedSlot.asStateFlow()

    init {
        listenToUserCoins()
    }

    private fun listenToUserCoins() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .addSnapshotListener { snapshot: DocumentSnapshot?, _: FirebaseFirestoreException? ->
                val serverCoins = snapshot?.getLong("coins")?.toInt() ?: 0
                _userCoins.value = serverCoins
            }
    }

    fun selectSlot(slot: ShopSlot) {
        _selectedSlot.value = slot
    }

    fun purchaseItem(slot: ShopSlot, onSuccess: () -> Unit = {}, onFailure: (String) -> Unit = {}) {
        val userId = auth.currentUser?.uid ?: return
        val userDocRef = db.collection("users").document(userId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userDocRef)
            val currentCoins = snapshot.getLong("coins") ?: 0L
            
            if (currentCoins >= slot.price) {
                // Deduct coins
                transaction.update(userDocRef, "coins", currentCoins - slot.price)
                
                // Update hunger, hydration, or happiness if applicable
                slot.statAffected?.let { stat ->
                    val defaultValue = if (stat == "happiness") 0L else 100L
                    val currentStatValue = snapshot.getLong(stat) ?: defaultValue
                    val newValue = (currentStatValue + slot.amount).coerceAtMost(100L)
                    transaction.update(userDocRef, stat, newValue)
                }

                // Future: add to inventory for toys/cosmetics
                // if (slot.statAffected == null) {
                //    transaction.update(userDocRef, "inventory", FieldValue.arrayUnion(slot.id))
                // }
            } else {
                throw FirebaseFirestoreException("Insufficient coins", FirebaseFirestoreException.Code.ABORTED)
            }
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener {
            onFailure(it.message ?: "Purchase failed")
        }
    }
}
