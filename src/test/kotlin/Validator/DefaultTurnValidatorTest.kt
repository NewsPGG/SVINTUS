package Validator

import Game.*
import Application.PlayerProfile
import Cards.Types.NumberCard
import Validator.TurnActions.PlayCardTurn
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.util.UUID

class DefaultTurnValidatorTest {
    @Test
    fun `validate should return error when card does not match top card`() {
        val player = InGamePlayer(PlayerProfile(UUID.randomUUID(), "Test", 0,0,0))
        val topCard = NumberCard(Color.RED, 1)
        val wrongCard = NumberCard(Color.BLUE, 9)
        val state = GameState(UUID.randomUUID(), mutableListOf(player), GiveCard(), DiscardCard(), 0, 1, true, topCard)

        val validator = DefaultTurnValidator()
        val turn = PlayCardTurn(state.gameId, 1, player, wrongCard, Color.BLUE, false)

        val result = validator.validate(turn, state)
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
    }
}