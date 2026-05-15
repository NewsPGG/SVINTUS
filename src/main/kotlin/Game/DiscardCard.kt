package Game

import Cards.Card
import java.util.Stack

class DiscardCard {
    lateinit var cards: Stack<Card>

    fun push(card: Card) {
        cards.push(card)
    }

    fun top(): Card {
        return cards.peek()
    }
}