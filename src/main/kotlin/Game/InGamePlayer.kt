package Game

import Application.PlayerProfile
import Cards.Types.ActionCard
import Cards.Card
import Cards.Types.NumberCard
import java.util.UUID

class InGamePlayer(playerProfile: PlayerProfile) {
    val playerId: UUID = playerProfile.id
    val name: String = playerProfile.username
    var hand: MutableList<Card> = mutableListOf()
    var declaredSwintus: Boolean = false

    fun drawCards(count: Int, giveCard: GiveCard) {
        for (i in 1..count) {
            this.hand.add(giveCard.draw())
        }
    }

    fun canPlayCard(topCard: Card) : Boolean {
        for (card in this.hand) {
            if (card.color == Color.GRAY || card.color == topCard.color) return true
            if (card is NumberCard && topCard is NumberCard && card.value == topCard.value) return true
            if (card is ActionCard && topCard is ActionCard && card.effect::class == topCard.effect::class) return true
        }
        return false
    }
}