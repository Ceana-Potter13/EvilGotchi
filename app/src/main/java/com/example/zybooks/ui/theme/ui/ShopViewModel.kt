package com.example.zybooks.ui.theme.ui

import androidx.lifecycle.ViewModel
import com.example.zybooks.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ShopSlot(
    val id: String, // Changed to String for unique naming like "FOOD_0"
    val imageRes: Int? = null,
    val price: Int = 0
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
            // Row 0: FOOD (Unique IDs: FOOD_0, FOOD_1, etc.)
            ShopRow(0, "FOOD", List(4) { ShopSlot("FOOD_$it", price = (it + 1) * 5) }),

            // Row 1: HYDRATION
            ShopRow(1, "HYDRATION", listOf(
                ShopSlot("DRINK_0", R.drawable.shopdrink1, 5),
                ShopSlot("DRINK_1", R.drawable.shopdrink2, 10),
                ShopSlot("DRINK_2", R.drawable.shopdrink3, 15),
                ShopSlot("DRINK_3", R.drawable.shopdrink4, 20)
            )),

            // Row 2: TOYS
            ShopRow(2, "TOYS", listOf(
                ShopSlot("TOY_0", R.drawable.shoptoy1, 5),
                ShopSlot("TOY_1", R.drawable.shoptoy2, 10),
                ShopSlot("TOY_2", R.drawable.shoptoy3, 15),
                ShopSlot("TOY_3", price = 20)
            )),

            // Row 3: COSMETICS
            ShopRow(3, "COSMETICS", listOf(
                ShopSlot("COSM_0", R.drawable.shopcosmetic1, 5),
                ShopSlot("COSM_1", R.drawable.shopcosmetic2, 10),
                ShopSlot("COSM_2", price = 15),
                ShopSlot("COSM_3", price = 20)
            ))
        )
    )
    val rows: StateFlow<List<ShopRow>> = _rows

    private val _selectedSlot = MutableStateFlow<ShopSlot?>(null)
    val selectedSlot: StateFlow<ShopSlot?> = _selectedSlot

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
        if (_userCoins.value < slot.price) {
            onFailure("Not enough coins!")
            return
        }

        db.collection("users").document(userId)
            .update("coins", FieldValue.increment(-slot.price.toLong()))
            .addOnSuccessListener {
                onSuccess()
                _selectedSlot.value = null // Deselect after purchase
            }
            .addOnFailureListener {
                onFailure(it.message ?: "Purchase failed")
            }
    }
}