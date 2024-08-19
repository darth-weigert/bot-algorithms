package br.darthweigert.scenes

import korlibs.io.async.launchImmediately
import korlibs.korge.scene.Scene
import korlibs.korge.ui.uiButton
import korlibs.korge.view.SContainer
import korlibs.math.geom.Size

class MainMenuScene: Scene() {

    var center: Double = 0.0
    var middle: Double = 0.0

    override suspend fun SContainer.sceneMain() {
        center = width / 2.0
        middle = height / 2.0

        uiButton("A* for pathfinding", Size(200, 32)) {
            x = center - width / 2.0
            y = middle - height / 2.0
            onPress {
                launchImmediately {
                    sceneContainer.pushTo<DrawDungeonScene>()
                }
            }
        }

    }
}
