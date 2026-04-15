package com.example.zybooks.data


import com.example.zybooks.R


class EggDataSource {

private val eggs = listOf(
    Egg(
        id = 1,
        imageId = R.drawable.egg
    ),
    Egg(
        id = 2,
        imageId = R.drawable.adultgotchi
    ),
    Egg(
        id = 3,
        imageId = R.drawable.teengotchi
),
    Egg(
    id = 4,
    imageId = R.drawable.babygotchi
    )
)


infix fun getEgg(id: Int): Egg? {
    return eggs.find { it.id == id }}
}