package galaxyraiders.core.game

import galaxyraiders.Config
import galaxyraiders.ports.RandomGenerator
import galaxyraiders.ports.ui.Controller
import galaxyraiders.ports.ui.Controller.PlayerCommand
import galaxyraiders.ports.ui.Visualizer
import kotlin.system.measureTimeMillis
import java.io.File
import java.nio.file.Paths
import com.beust.klaxon.Klaxon
import java.util.Locale
import java.util.Calendar
import java.text.SimpleDateFormat


const val MILLISECONDS_PER_SECOND: Int = 1000

class Score(
    var matchId: Int?,
    var startTime: String?,
    var nAsteroidsDestroyed: Int?,
    var score: Int?
  )

var score = Score(0, "", 0, 0)
var updatedThisSecond = 0
var firstRun = 1

object GameEngineConfig {
  private val config = Config(prefix = "GR__CORE__GAME__GAME_ENGINE__")

  val frameRate = config.get<Int>("FRAME_RATE")
  val spaceFieldWidth = config.get<Int>("SPACEFIELD_WIDTH")
  val spaceFieldHeight = config.get<Int>("SPACEFIELD_HEIGHT")
  val asteroidProbability = config.get<Double>("ASTEROID_PROBABILITY")
  val coefficientRestitution = config.get<Double>("COEFFICIENT_RESTITUTION")

  val msPerFrame: Int = MILLISECONDS_PER_SECOND / this.frameRate
}

@Suppress("TooManyFunctions")
class GameEngine(
  val generator: RandomGenerator,
  val controller: Controller,
  val visualizer: Visualizer
) {
  val field = SpaceField(
    width = GameEngineConfig.spaceFieldWidth,
    height = GameEngineConfig.spaceFieldHeight,
    generator = generator
  )

  var playing = true

  fun execute() {
    this.parseScoreBoardJson()
    while (true) {
      val duration = measureTimeMillis { this.tick() }

      Thread.sleep(
        maxOf(0, GameEngineConfig.msPerFrame - duration)
      )
    }
  }

  fun execute(maxIterations: Int) {
    repeat(maxIterations) {
      this.tick()
    }
  }

  fun tick() {
    val segundo = SimpleDateFormat("ss", Locale.getDefault()).format(Calendar.getInstance().time)
    if (segundo.toInt() % 10 == 0 && updatedThisSecond == 0) {
      this.updateScoreBoardJson(1)
      updatedThisSecond = 1
    }
    if (segundo.toInt() % 7 == 0) {
      updatedThisSecond = 0
    }
    this.processPlayerInput()
    this.updateSpaceObjects()
    this.renderSpaceField()
  }

  fun parseScoreBoardJson() {
    var arquivoScore = File("src/main/kotlin/galaxyraiders/core/score/scoreboard.json").readText(Charsets.UTF_8)
    var scoreArray = Klaxon().parseArray<Score>(arquivoScore)

    val firstObj = scoreArray!![0]
    val newMatchId = firstObj.matchId!! + 1
    val newDateString = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().time)

    score.matchId = newMatchId
    score.startTime = newDateString   
    this.updateScoreBoardJson(0)
  }

  fun updateScoreBoardJson(excludeFirst: Int) {
    var filename = "src/main/kotlin/galaxyraiders/core/score/scoreboard.json"
    var arquivoScore = File(filename)
    // var texto = arquivoScore.readText(Charsets.UTF_8)
    var scoreArray = Klaxon().parseArray<Score>(arquivoScore)


    //Atualiza scoreboard
    val items = mutableListOf(score)

    var i = excludeFirst
    while (i < scoreArray.size) {
      items.add(scoreArray[i])
      i = i+1
    } 
    arquivoScore.delete() 
    arquivoScore.createNewFile()
    var json = Klaxon().toJsonString(items)

    arquivoScore.writeText(json)
  }

  fun processPlayerInput() {
    this.controller.nextPlayerCommand()?.also {
      when (it) {
        PlayerCommand.MOVE_SHIP_UP ->
          this.field.ship.boostUp()
        PlayerCommand.MOVE_SHIP_DOWN ->
          this.field.ship.boostDown()
        PlayerCommand.MOVE_SHIP_LEFT ->
          this.field.ship.boostLeft()
        PlayerCommand.MOVE_SHIP_RIGHT ->
          this.field.ship.boostRight()
        PlayerCommand.LAUNCH_MISSILE ->
          this.field.generateMissile()
        PlayerCommand.PAUSE_GAME ->
          this.playing = !this.playing
      }
    }
  }

  fun updateSpaceObjects() {
    if (!this.playing) return
    this.handleCollisions()
    this.moveSpaceObjects()
    this.trimSpaceObjects()
    this.generateAsteroids()
  }

  fun handleCollisions() {
    this.field.spaceObjects.forEachPair {
        (first, second) ->
        if (first.impacts(second)) {
          // INICIO PARTE 2 - EP
          if (first.type == "Missile") {
            this.field.generateExplosion(first.center)
          }
          // FIM PARTE 2 - EP
          first.collideWith(second, GameEngineConfig.coefficientRestitution)
        }
    }
  }

  fun moveSpaceObjects() {
    this.field.moveShip()
    this.field.moveAsteroids()
    this.field.moveMissiles()
  }

  fun trimSpaceObjects() {
    this.field.trimAsteroids()
    this.field.trimMissiles()
    this.field.trimExplosions()
  }

  fun generateAsteroids() {
    val probability = generator.generateProbability()

    if (probability <= GameEngineConfig.asteroidProbability) {
      this.field.generateAsteroid()
    }
  }

  fun renderSpaceField() {
    this.visualizer.renderSpaceField(this.field)
  }
}

fun <T> List<T>.forEachPair(action: (Pair<T, T>) -> Unit) {
  for (i in 0 until this.size) {
    for (j in i + 1 until this.size) {
      action(Pair(this[i], this[j]))
    }
  }
}
