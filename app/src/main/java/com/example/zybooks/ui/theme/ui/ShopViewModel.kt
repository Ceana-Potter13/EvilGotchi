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
                ShopSlot("TOY_3", R.drawable.shoptoy4, price = 20)
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
        val userRef = db.collection("users").document(userId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(userRef)
            val currentCoins = snapshot.getLong("coins") ?: 0L

            if (currentCoins < slot.price) {
                throw Exception("Not enough coins!")
            }

            val updates = mutableMapOf<String, Any>(
                "coins" to (currentCoins - slot.price)
            )

            when {
                slot.id.startsWith("FOOD_") -> {
                    val currentHunger = snapshot.getLong("hunger") ?: 0L
                    updates["hunger"] = (currentHunger + 20L).coerceAtMost(100L)
                }
                slot.id.startsWith("DRINK_") -> {
                    val currentHydration = snapshot.getLong("hydration") ?: 0L
                    updates["hydration"] = (currentHydration + 20L).coerceAtMost(100L)
                }
                slot.id.startsWith("TOY_") || slot.id.startsWith("COSM_") -> {
                    val currentHappiness = snapshot.getLong("happiness") ?: 0L
                    updates["happiness"] = (currentHappiness + 20L).coerceAtMost(100L)
                }
            }

            transaction.update(userRef, updates)
            null
        }.addOnSuccessListener {
            onSuccess()
            _selectedSlot.value = null
        }.addOnFailureListener {
            onFailure(it.message ?: "Purchase failed")
        }
    }
}