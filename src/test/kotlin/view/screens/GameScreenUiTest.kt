package view.screens

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

class GameScreenUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun gameScreen_clickSwintusButton_preparesShoutState() {
        val mockViewModel = SwintusViewModel()

        val profile = PlayerProfile(
            UUID.randomUUID(),
            "Игрок1",
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

        val mockState = GameState(
            gameId = UUID.randomUUID(),
            players = mutableListOf(mockPlayer),
            giveCard = giveCardDeck,
            discardCard = discardCardPile,
            currentPlayerIndex = 0,
            turnNumber = 1,
            gameStatus = true,
            topCard = NumberCard(Color.RED, 5)
        )
        mockViewModel.startGameFromLobby(mockState)

        composeTestRule.setContent {
            GameScreen(viewModel = mockViewModel)
        }

        composeTestRule.onNodeWithText("🐷 СВИНТУС!").performClick()
        composeTestRule.onNodeWithText("Вы объявили Свинтус! Выберите карту для хода.").assertExists()
    }
}