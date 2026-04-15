package com.example.zybooks.ui.theme.ui

import androidx.lifecycle.ViewModel
import com.example.zybooks.data.Egg
import com.example.zybooks.data.EggDataSource

class EggViewModel: ViewModel() {

    fun getEgg(eggId: Int): Egg = EggDataSource().getEgg(eggId) ?: Egg()
}