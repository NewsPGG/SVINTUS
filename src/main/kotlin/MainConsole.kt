import Application.GameAdministrator
import Application.PlayerProfile
import Cards.Types.WildCard
import Game.Color
import Game.InGamePlayer
import Validator.TurnActions.Turn
import Validator.TurnActions.DrawCardTurn
import Validator.TurnActions.PlayCardTurn
import java.util.Scanner
import java.util.UUID

class ConsoleGameClient {
    private val admin = GameAdministrator()
    private val scanner = Scanner(System.`in`)

    fun start() {
        println("=== СВИНТУС-АДМИН ===")

        val profiles = setupPlayers()
        admin.startGame(profiles)

        while (admin.gameState.gameStatus) {
            admin.printGameState()

            val turn = readTurnAction() ?: continue

            val error = admin.processTurn(turn)
            if (error != null) {
                println("\nОШИБКА ВАЛИДАЦИИ: $error")
            } else {
                println("\nДействие обработано.")
            }
        }

        println("Партия завершена. Спасибо за игру!")
    }

    private fun setupPlayers(): List<PlayerProfile> {
        println("Сколько игроков будет в партии?")
        val count = scanner.nextLine().toIntOrNull() ?: 2
        val profiles = mutableListOf<PlayerProfile>()

        repeat(count) { i ->
            println("Введите имя игрока ${i + 1}:")
            val name = scanner.nextLine().trim()
            profiles.add(PlayerProfile(UUID.randomUUID(), name, 1000, 0, 0))
        }
        return profiles
    }

    private fun readTurnAction(): Turn? {
        println("ДЕЙСТВИЕ:")
        println("Введите индекс карты (например: 3)")
        println("Добавьте ' s' если игрок кричит Свинтус (например: 3 s)")
        println("Введите -1 для взятия карты")

        val rawInput = scanner.nextLine().trim().split(" ")
        val input = rawInput[0].toIntOrNull() ?: -2
        val shouted = rawInput.size > 1 && rawInput[1].lowercase() == "s"

        val state = admin.gameState
        val currentPlayer = state.players[state.currentPlayerIndex]

        return when {
            input == -1 -> {
                DrawCardTurn(state.gameId, state.turnNumber, currentPlayer, 1, false)
            }
            input in 0 until currentPlayer.hand.size -> {
                buildPlayCardTurn(currentPlayer, input, shouted)
            }
            else -> {
                println("Неверный ввод! Повторите попытку.")
                null
            }
        }
    }

    private fun buildPlayCardTurn(player: InGamePlayer, cardIndex: Int, shouted: Boolean): PlayCardTurn {
        val card = player.hand[cardIndex]
        val state = admin.gameState

        val chosenColor = if (card is WildCard) {
            askForWildCardColor()
        } else {
            card.color
        }

        return PlayCardTurn(
            gameId = state.gameId,
            turnNumber = state.turnNumber,
            playerId = player,
            card = card,
            declaredColor = chosenColor,
            declaredSwintus = shouted
        )
    }

    private fun askForWildCardColor(): Color {
        while (true) {
            println("Выберите цвет (RED, GREEN, BLUE, YELLOW):")
            val inputColor = scanner.nextLine().uppercase().trim()
            try {
                return Color.valueOf(inputColor)
            } catch (e: Exception) {
                println("Некорректный цвет! Допустимы только: RED, GREEN, BLUE, YELLOW. Попробуйте еще раз.")
            }
        }
    }
}

fun mainConsole() {
    ConsoleGameClient().start()
}