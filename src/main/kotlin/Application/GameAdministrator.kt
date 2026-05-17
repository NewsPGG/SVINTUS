package Application

import Cards.Card
import Cards.Effects.*
import Cards.Types.ActionCard
import Cards.Types.NumberCard
import Cards.Types.WildCard
import Game.*
import Validator.DefaultTurnValidator
import Validator.TurnActions.Turn
import java.util.*

class GameAdministrator {
    private val validator = DefaultTurnValidator()
    private lateinit var logic: GameStateLogic
    lateinit var gameState: GameState

    fun startGame(profiles: List<PlayerProfile>) {
        val players = profiles.map { profile ->
            InGamePlayer(profile).apply { hand = mutableListOf() }
        }.toMutableList()

        // Создаем честную колоду Свинтуса на 104 карты
        val fullDeck = createSwintusDeck()
        val giveCard = GiveCard().apply {
            cards = Stack<Card>()
            cards.addAll(fullDeck.shuffled())
        }

        val discardCard = DiscardCard().apply { cards = Stack<Card>() }

        // Раздаем игрокам по 8 карт на руку (для 2 игроков это 16 карт)
        players.forEach { it.drawCards(8, giveCard) }

        // Вытаскиваем первую карту для стола. Она должна быть обычной цифровой.
        var firstCard = giveCard.draw()

        // Фикс "потерянных" карт: вместо ломающего структуру .add(0, ...)
        // используем родной .push() и заново перемешиваем колоду, если вылез спец-эффект.
        while (firstCard is WildCard || firstCard is ActionCard) {
            giveCard.cards.push(firstCard)
            giveCard.cards.shuffle()
            firstCard = giveCard.draw()
        }

        gameState = GameState(
            gameId = UUID.randomUUID(),
            players = players,
            giveCard = giveCard,
            discardCard = discardCard,
            currentPlayerIndex = 0,
            turnNumber = 1,
            gameStatus = true,
            topCard = firstCard
        )

        logic = GameStateLogic(gameState)
        println("--- Игра успешно инициализирована (В колоде осталось: ${giveCard.cards.size} к.) ---")
    }

    fun processTurn(turn: Turn): String? {
        if (!gameState.gameStatus) return "Игра уже завершена!"

        val validation = validator.validate(turn, gameState)
        if (!validation.isValid) {
            return validation.errorMessage
        }

        gameState = logic.applyTurn(turn)

        if (!gameState.gameStatus) {
            val winner = gameState.players.find { it.hand.isEmpty() }
            println("\n!!! ПОБЕДА: ${winner?.name} !!!")
        }

        return null
    }

    fun printGameState() {
        val currentPlayer = gameState.players[gameState.currentPlayerIndex]

        println("\n========================================")
        println("ХОД №${gameState.turnNumber}")
        println("На столе: ${formatCardName(gameState.topCard)}")
        println("Очередь игрока: ${currentPlayer.name}")
        println("----------------------------------------")
        println("Карты в руке:")
        currentPlayer.hand.forEachIndexed { index, card ->
            println("[$index] ${formatCardName(card)}")
        }
        println("========================================\n")
    }

    private fun formatCardName(card: Card): String {
        return when (card) {
            is NumberCard -> "${card.color} ${card.value}"
            is ActionCard -> {
                val effectName = when(card.effect) {
                    is SkipEffect -> "ЗАХРАПИН"
                    is ReverseEffect -> "ПЕРЕХРЮК"
                    is TakeThreeEffect -> "ХАПЕЖ"
                    is TakeTwoEffect -> "УНО-ХАПЕЖ"
                    else -> "ЭФФЕКТ"
                }
                "${card.color} [$effectName]"
            }
            is WildCard -> "ПОЛИПЕЦ (Цвет: ${card.chosenColor})"
            else -> "Неизвестная карта"
        }
    }

    /**
     * Генерация оригинальной колоды Свинтуса (ровно 104 карты)
     */
    private fun createSwintusDeck(): List<Card> {
        val deck = mutableListOf<Card>()
        val colors = listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)

        // Каждая карта масти (цифры и спец-эффекты) встречается в колоде ровно ДВАЖДЫ
        repeat(2) {
            for (color in colors) {
                // 1. Цифровые карты от 0 до 7 (все удваиваются, включая 0) -> 64 карты
                for (value in 0..7) {
                    deck.add(NumberCard(color, value))
                }

                // 2. Карты предписаний (спец-эффекты масти) -> 24 карты
                deck.add(ActionCard(color, SkipEffect()))       // Захрапин
                deck.add(ActionCard(color, ReverseEffect()))    // Перехрюк
                deck.add(ActionCard(color, TakeThreeEffect()))  // Хапеж (3 карты)
            }
        }

        // 3. Дикие карты (Полипец) — ровно 8 штук на колоду -> 16 карт
        repeat(8) {
            deck.add(WildCard(Color.GRAY, Color.GRAY))
        }

        println("ГЕНЕРАЦИЯ: Создана колода Свинтуса. Всего карт: ${deck.size} шт.")
        return deck
    }
}