package Game

import Cards.Card
import java.util.Stack

class GiveCard {
    var cards: Stack<Card> = Stack()

    fun draw(discardCard: DiscardCard? = null): Card {
        if (cards.isEmpty()) {
            if (discardCard != null && discardCard.cards.isNotEmpty()) {
                reshuffle(discardCard)
            } else {
                throw IllegalStateException("Deck and discard are both empty")
            }
        }
        return cards.pop()
    }

    fun reshuffle(discardCard: DiscardCard) {
        val tempCards = discardCard.cards.toMutableList()
        tempCards.shuffle()
        cards.addAll(tempCards)
        discardCard.cards.clear()
    }
}