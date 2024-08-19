package br.darthweigert.scenes

import br.darthweigert.algorithm.AStar
import br.darthweigert.algorithm.DebugAStar
import br.darthweigert.algorithm.DebugFunnel
import br.darthweigert.algorithm.DebugTriangulation
import br.darthweigert.algorithm.Funnel
import br.darthweigert.algorithm.NavMeshPathFinder
import br.darthweigert.component.DrawComponent
import br.darthweigert.geometry.Triangle
import br.darthweigert.math.nextRandomGray
import br.darthweigert.service.JobService
import br.darthweigert.service.RoomService
import korlibs.event.Key
import korlibs.image.color.Colors
import korlibs.image.color.MaterialColors
import korlibs.io.async.launchImmediately
import korlibs.korge.input.keys
import korlibs.korge.scene.Scene
import korlibs.korge.ui.UIButton
import korlibs.korge.ui.UIText
import korlibs.korge.ui.uiButton
import korlibs.korge.ui.uiHorizontalStack
import korlibs.korge.ui.uiText
import korlibs.korge.ui.uiVerticalStack
import korlibs.korge.view.SContainer
import korlibs.korge.view.addTo
import korlibs.korge.view.container
import korlibs.korge.view.line
import korlibs.korge.view.outline
import korlibs.korge.view.solidTriangle
import korlibs.math.geom.Size
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

class DrawDungeonScene(
    private val roomService: RoomService,
    private val random: Random
) : Scene() {

    override suspend fun SContainer.sceneMain() {
        val jobService = JobService(this@DrawDungeonScene)

        val toolContainer = container { }
        val shapeContainer = container { }
        val pathContainer = container { }
        val debugContainer = container { }
        val startContainer = container { }
        val endContainer = container { }

        var navMesh: List<Triangle>? = null
        var path: List<Triangle>? = null
        val status: UIText = uiText("Status: None") {
            x = 5.0
            y = this@sceneMain.height - 25.0
        }
        val toolBox = DrawComponent(
            random, roomService, status, toolContainer, shapeContainer, startContainer, endContainer
        ).addTo(this)
        val closeScene = uiButton("X", Size(32, 32)) {
            x = this@sceneMain.width - width
            y = 0.0
            bgColorOut = MaterialColors.RED_700
            bgColorOver = MaterialColors.RED_800
            bgColorSelected = MaterialColors.RED_900
            onPress {
                launchImmediately {
                    sceneContainer.back()
                }
            }
        }
        val drawTool = UIButton(UIButton.DEFAULT_SIZE, "Draw")
        val merge = UIButton(UIButton.DEFAULT_SIZE, "Merge")
        val poly2Triangle = UIButton(UIButton.DEFAULT_SIZE, "Triangulate")
        val startTool = UIButton(UIButton.DEFAULT_SIZE, "Set Start")
        val endTool = UIButton(UIButton.DEFAULT_SIZE, "Set End")
        val pathfinder = UIButton(UIButton.DEFAULT_SIZE, "Pathfinder")
        val funnel = UIButton(UIButton.DEFAULT_SIZE, "Funnel")
        uiVerticalStack(padding = 5.0) {
            x = 5.0
            y = 5.0
            uiHorizontalStack(padding = 5) {
                addChild(drawTool)
                addChild(merge)
                addChild(poly2Triangle)
                addChild(startTool)
                addChild(endTool)
            }
            uiHorizontalStack(padding = 5) {
                addChild(pathfinder)
                addChild(funnel)
            }
        }

        val debugTriangulation = DebugTriangulation(status, debugContainer, earCandidateDelay = 250.milliseconds)
        val debugAStar = DebugAStar(status, debugContainer)
        val debugFunnel = DebugFunnel(status, debugContainer, 500.milliseconds)

        keys {
            up(Key.DELETE) {
                shapeContainer.removeChildren()
                pathContainer.removeChildren()
                debugContainer.removeChildren()
                startContainer.removeChildren()
                endContainer.removeChildren()
                roomService.clear()
                toolBox.clear()
                navMesh = null
                path = null
            }
        }

        drawTool.onPress {
            if (jobService.currentJob != null) {
                return@onPress
            }
            toolBox.changeTool(DrawComponent.Tool.DRAW_ROOM)
            navMesh = null
            shapeContainer.removeChildren()
            roomService.draw(shapeContainer)
        }

        merge.onPress {
            if (jobService.currentJob != null) {
                return@onPress
            }

            shapeContainer.removeChildren()
            for (vectorPath in roomService.roomsOutlines()) {
                shapeContainer.outline(vectorPath)
            }
        }

        poly2Triangle.onPress {
            if (jobService.currentJob != null) {
                if (jobService.currentTask == JobService.Task.TRIANGULATE) {
                    debugTriangulation.speedUp *= 2.0
                }
                return@onPress
            }
            shapeContainer.removeChildren()
            jobService.launch(JobService.Task.TRIANGULATE) {
                toolBox.changeTool(DrawComponent.Tool.NONE)
                debugTriangulation.reset()
                navMesh = roomService.createNavMesh(debugTriangulation)
                debugContainer.removeChildren()
                for (triangle in navMesh!!) {
                    shapeContainer.solidTriangle(triangle.a, triangle.b, triangle.c, random.nextRandomGray())
                }
            }
        }

        startTool.onPress {
            if (jobService.currentJob != null) {
                return@onPress
            }
            toolBox.changeTool(DrawComponent.Tool.DRAW_START)
        }

        endTool.onPress {
            if (jobService.currentJob != null) {
                return@onPress
            }
            toolBox.changeTool(DrawComponent.Tool.DRAW_END)
        }

        pathfinder.onPress {
            if (jobService.currentJob != null) {
                if (jobService.currentTask == JobService.Task.PATH_FIND) {
                    debugAStar.speedUp *= 2.0
                }
                return@onPress
            }
            val navMesh = navMesh
            val startPoint = toolBox.startPoint
            val endPoint = toolBox.endPoint
            if (navMesh == null) {
                status.text = "Status: Missing nav mesh!"
                return@onPress
            }
            if (startPoint == null) {
                status.text = "Status: Missing start point!"
                return@onPress
            }
            if (endPoint == null) {
                status.text = "Status: Missing end point!"
                return@onPress
            }
            status.text = "Status: Pathfinding"

            val startTriangle = navMesh.find { startPoint in it }!!
            val endTriangle = navMesh.find { endPoint in it }!!

            jobService.launch(JobService.Task.PATH_FIND) {
                toolBox.changeTool(DrawComponent.Tool.NONE)
                debugAStar.reset()
                debugContainer.removeChildren()
                pathContainer.removeChildren()
                path = listOf(startTriangle) + AStar(NavMeshPathFinder(navMesh), debugAStar)
                    .find(startTriangle, endTriangle).map { it.second }
                debugContainer.removeChildren()
                for (triangle in path!!) {
                    pathContainer.solidTriangle(triangle.a, triangle.b, triangle.c, Colors.DARKBLUE.withA(127))
                    delay(250.milliseconds)
                }
            }
        }

        funnel.onPress {
            if (jobService.currentJob != null) {
                if (jobService.currentTask == JobService.Task.FUNNEL) {
                    debugFunnel.speedUp *= 2
                }
                return@onPress
            }
            val startPoint = toolBox.startPoint
            val endPoint = toolBox.endPoint
            val path = path
            if (path == null) {
                status.text = "Status: Missing path!"
                return@onPress
            }
            if (startPoint == null) {
                status.text = "Status: Missing start point!"
                return@onPress
            }
            if (endPoint == null) {
                status.text = "Status: Missing end point!"
                return@onPress
            }
            jobService.launch(JobService.Task.FUNNEL) {
                toolBox.changeTool(DrawComponent.Tool.NONE)
                debugFunnel.reset()
                val optimizedPath = Funnel(debugFunnel).find(path, startPoint, endPoint)
                debugContainer.removeChildren()
                optimizedPath.zipWithNext().forEach { (a, b) -> debugContainer.line(a, b, Colors.WHITE) }
            }
        }
    }
}
