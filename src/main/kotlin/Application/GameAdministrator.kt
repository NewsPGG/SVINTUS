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

        val fullDeck = createSwintusDeck()
        val giveCard = GiveCard().apply {
            cards = Stack<Card>()
            cards.addAll(fullDeck.shuffled())
        }

        val discardCard = DiscardCard().apply { cards = Stack<Card>() }

        players.forEach { it.drawCards(8, giveCard) }

        var firstCard = giveCard.draw()
        while (firstCard is WildCard || firstCard is ActionCard) {
            giveCard.cards.add(0, firstCard)
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
        println("--- Игра успешно инициализирована ---")
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

    // Тот самый метод, который искал main
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
                    is TakeTwoEffect -> "ХАПЕЖ"
                    is TakeThreeEffect -> "ХАПЕЖ (3)"
                    else -> "ЭФФЕКТ"
                }
                "${card.color} [$effectName]"
            }
            is WildCard -> "ПОЛИПЕЦ (Цвет: ${card.chosenColor})"
            else -> "Неизвестная карта"
        }
    }

    private fun createSwintusDeck(): List<Card> {
        val deck = mutableListOf<Card>()
        val colors = listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)
        for (color in colors) {
            for (i in 0..9) {
                deck.add(NumberCard(color, i))
                if (i != 0) deck.add(NumberCard(color, i))
            }
            repeat(2) {
                deck.add(ActionCard(color, SkipEffect()))
                deck.add(ActionCard(color, ReverseEffect()))
                deck.add(ActionCard(color, TakeTwoEffect()))
            }
        }
        repeat(8) { deck.add(WildCard(Color.GRAY, Color.GRAY)) }
        return deck
    }
}