import Application.GameAdministrator
import Application.PlayerProfile
import Cards.Types.WildCard
import Game.Color
import Validator.TurnActions.DrawCardTurn
import Validator.TurnActions.PlayCardTurn
import java.util.Scanner
import java.util.UUID

fun mainConsole() {
    val admin = GameAdministrator()
    val scanner = Scanner(System.`in`)

    println("=== СВИНТУС-АДМИН ===")

    val profiles = mutableListOf<PlayerProfile>()
    println("Сколько игроков будет в партии?")
    val count = scanner.nextLine().toIntOrNull() ?: 2

    repeat(count) { i ->
        println("Введите имя игрока ${i + 1}:")
        val name = scanner.nextLine()
        profiles.add(PlayerProfile(UUID.randomUUID(), name, 1000, 0, 0))
    }

    admin.startGame(profiles)

    while (admin.gameState.gameStatus) {
        admin.printGameState()

        println("ДЕЙСТВИЕ:")
        println("Введите индекс карты (например: 3)")
        println("Добавьте ' s' если игрок кричит Свинтус (например: 3 s)")
        println("Введите -1 для взятия карты")

        val rawInput = scanner.nextLine().trim().split(" ")
        val input = rawInput[0].toIntOrNull() ?: -2
        val shouted = rawInput.size > 1 && rawInput[1].lowercase() == "s"

        val currentPlayer = admin.gameState.players[admin.gameState.currentPlayerIndex]

        val turn = if (input == -1) {
            DrawCardTurn(admin.gameState.gameId, admin.gameState.turnNumber, currentPlayer, 1, false)
        } else if (input in 0 until currentPlayer.hand.size) {
            val card = currentPlayer.hand[input]

            var chosenColor = card.color
            if (card is WildCard) {
                println("Выберите цвет (RED, GREEN, BLUE, YELLOW):")
                chosenColor = try { Color.valueOf(scanner.nextLine().uppercase()) } catch(e: Exception) { Color.RED }
            }

            PlayCardTurn(admin.gameState.gameId, admin.gameState.turnNumber, currentPlayer, card, chosenColor, shouted)
        } else {
            println("Неверный ввод!")
            continue
        }

        val result = admin.processTurn(turn)
        if (result != null) {
            println("\nОШИБКА ВАЛИДАЦИИ: $result")
        } else {

            println("\nДействие обработано.")
        }
    }

    println("Партия завершена. Спасибо за игру!")
}