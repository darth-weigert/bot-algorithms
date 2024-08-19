package br.darthweigert.component

import br.darthweigert.math.nextRandomGray
import br.darthweigert.service.RoomService
import korlibs.image.color.Colors
import korlibs.korge.input.mouse
import korlibs.korge.render.RenderContext
import korlibs.korge.ui.UIText
import korlibs.korge.view.Container
import korlibs.korge.view.SolidRect
import korlibs.korge.view.View
import korlibs.korge.view.addTo
import korlibs.korge.view.circle
import korlibs.korge.view.solidRect
import korlibs.math.geom.Size
import korlibs.math.geom.Vector2D
import korlibs.math.geom.Vector2I
import korlibs.math.geom.toDouble
import korlibs.math.geom.toFloat
import kotlin.random.Random

class DrawComponent(
    private val random: Random,
    private val roomService: RoomService,
    private val status: UIText,
    private val toolContainer: Container,
    private val shapeContainer: Container,
    private val startContainer: Container,
    private val endContainer: Container,
): View() {

    enum class Tool {
        NONE,
        DRAW_ROOM,
        DRAW_START,
        DRAW_END
    }

    override fun onParentChanged() {
        parent?.let {
            this.size = it.size
            drawComponent.size = it.size
            startComponent.size = it.size
            endComponent.size = it.size
        }
    }

    override fun renderInternal(ctx: RenderContext) {
        // do nothing
    }

    private var currentTool: View? = null
    var startPoint: Vector2D? = null
        private set
    var endPoint: Vector2D? = null
        private set

    fun clear() {
        changeTool(Tool.NONE)
        startPoint = null
        endPoint = null
    }

    private val drawComponent = SolidRect(Size.ZERO).apply {
        val component = this
        color = Colors.CORNFLOWERBLUE

        mouse {
            var drawing = false
            lateinit var start: Vector2I
            lateinit var end: Vector2I
            lateinit var box: SolidRect
            down {
                drawing = true
                start = currentMouseLocation(component)
                status.text = "Status: Drawing"
                val color = random.nextRandomGray()
                box = shapeContainer.solidRect(0, 0, color) {
                    x = start.x.toDouble()
                    y = start.y.toDouble()
                }
            }
            move {
                if (drawing) {
                    end = currentMouseLocation(component)
                    box.width = (end.x - start.x).toDouble()
                    box.height = (end.y - start.y).toDouble()
                }
            }
            up {
                if (drawing) {
                    drawing = false
                    status.text = "Status: Draw Selected"
                    end = currentMouseLocation(component)
                    if ((end - start).toFloat().lengthSquared < 10) {
                        return@up
                    }
                    box.width = (end.x - start.x).toDouble()
                    box.height = (end.y - start.y).toDouble()
                    roomService.addRoom(start, end)
                }
            }
        }
    }
    private val startComponent = SolidRect(Size.ZERO).apply {
        val component = this
        color = Colors.SEAGREEN

        mouse {
            onClick {
                startPoint = currentMouseLocation(component).toDouble()
//            pathContainer.removeChildren()
//            path = null
//            debugContainer.removeChildren()
                startContainer.removeChildren()
                startContainer.circle(6) {
                    x = startPoint!!.x - 6
                    y = startPoint!!.y - 6
                    color = Colors.SEAGREEN
                }
            }
        }
    }

    private val endComponent = SolidRect(Size.ZERO).apply {
        val component = this
        color = Colors.DARKRED

        mouse {
            onClick {
                endPoint = currentMouseLocation(component).toDouble()
//                pathContainer.removeChildren()
//                path = null
//                debugContainer.removeChildren()
                endContainer.removeChildren()
                endContainer.circle(6) {
                    x = endPoint!!.x - 6
                    y = endPoint!!.y - 6
                    color = Colors.DARKRED
                }
            }
        }
    }

    fun changeTool(tool: Tool) {
        currentTool?.removeFromParent()
        currentTool = when(tool) {
            Tool.DRAW_ROOM -> drawComponent
            Tool.DRAW_START -> startComponent
            Tool.DRAW_END -> endComponent
            Tool.NONE -> null
        }
        currentTool?.addTo(toolContainer)
    }

    private fun currentMouseLocation(component: SolidRect) =
        Vector2I(component.mouse.currentPosLocal.x.toInt(), component.mouse.currentPosLocal.y.toInt())
}
