import br.darthweigert.scenes.DrawDungeonScene
import br.darthweigert.scenes.MainMenuScene
import br.darthweigert.service.RoomService
import korlibs.image.color.Colors
import korlibs.korge.Korge
import korlibs.korge.scene.sceneContainer
import korlibs.math.geom.Size
import kotlin.random.Random

suspend fun main() = Korge(
    windowSize = Size(640, 512),
    virtualSize = Size(640, 512),
    clipBorders = false,
    backgroundColor = Colors["#2b2b2b"]) {

    injector.mapSingleton<Random> { Random(1234) }
    injector.mapSingleton<RoomService> { RoomService(get()) }

    injector.mapPrototype<MainMenuScene> { MainMenuScene() }
    injector.mapPrototype<DrawDungeonScene> { DrawDungeonScene(get(), get()) }

    val sceneContainer = sceneContainer()
    sceneContainer.changeTo<MainMenuScene>()
}

