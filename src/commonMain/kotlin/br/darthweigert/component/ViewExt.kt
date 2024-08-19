package br.darthweigert.component

import korlibs.korge.view.View

fun View.setCenter(x: Double, y: Double) {
    this.x = x - width / 2.0
    this.y = y - height / 2.0
}
