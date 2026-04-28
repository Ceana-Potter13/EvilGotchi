package com.example.zybooks.data


import com.example.zybooks.R


class EggDataSource {

private val eggs = listOf(
    Egg(
        id = 1,
        imageId = R.drawable.paytonegg
    ),
    Egg(
        id = 2,
        imageId = R.drawable.ceanaegg
    ),
    Egg(
        id = 3,
        imageId = R.drawable.willegg
),
    Egg(
    id = 4,
    imageId = R.drawable.kolaegg
    ),


)


infix fun getEgg(id: Int): Egg? {
    return eggs.find { it.id == id }}
}