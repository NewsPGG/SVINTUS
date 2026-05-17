package view

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import viewmodel.SwintusViewModel
import Game.*
import Application.PlayerProfile
import Cards.Types.NumberCard
import java.util.UUID
import java.util.Stack

class AppNavigationIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun app_fullNavigationFromGameScreenToLobby() {
        val viewModel = SwintusViewModel()

        val profile = PlayerProfile(
            UUID.randomUUID(),
            "Игрок 1",
            100,
            0,
            0
        )

        val mockPlayer = InGamePlayer(profile)

        val giveCardDeck = GiveCard().apply {
            this.cards = Stack<Cards.Card>()
        }

        val discardCardPile = DiscardCard().apply {
            this.cards = Stack<Cards.Card>()
        }

        val activeState = GameState(
            gameId = UUID.randomUUID(),
            players = mutableListOf(mockPlayer),
            giveCard = giveCardDeck,
            discardCard = discardCardPile,
            currentPlayerIndex = 0,
            turnNumber = 5,
            gameStatus = true,
            topCard = NumberCard(Color.BLUE, 2)
        )

        viewModel.startGameFromLobby(activeState)

        composeTestRule.setContent {
            Application()
        }

        composeTestRule.onNodeWithText("ИГРОВОЙ СТОЛ").assertExists()
        composeTestRule.onNodeWithText("В лобби").performClick()
        composeTestRule.onNodeWithText("ИГРОВОЙ СТОЛ").assertDoesNotExist()
    }
}