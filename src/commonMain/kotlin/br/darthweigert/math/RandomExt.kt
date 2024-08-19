package br.darthweigert.math

import korlibs.image.color.RGBA
import kotlin.random.Random

fun Random.nextRandomGray(): RGBA {
    return this.nextInt(255).let { value -> RGBA(value, value, value, 0xFF) }
}


