package view.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import view.components.CardView
import viewmodel.SwintusViewModel
import Cards.Card
import Cards.Types.WildCard
import Game.Color as GameColor
import Game.GameState
import Game.InGamePlayer
import java.util.UUID

@Composable
fun GameScreen(viewModel: SwintusViewModel) {
    val stateNullable by viewModel.gameState.collectAsState()
    val version by viewModel.stateVersion.collectAsState()

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var shoutedSwintus by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var showAccuseDialog by remember { mutableStateOf(false) }
    var pendingWildCard by remember { mutableStateOf<Card?>(null) }

    val state = stateNullable ?: return
    if (state.players.isEmpty()) return

    if (!state.gameStatus) {
        GameOverOverlay(state = state, onReset = { viewModel.resetToLobby() })
        return
    }

    val actualPlayer = state.players.getOrNull(state.currentPlayerIndex) ?: return
    LaunchedEffect(state.currentPlayerIndex, state.turnNumber) { errorMessage = null }

    GameDialogsWrapper(
        showColorDialog = showColorDialog,
        showAccuseDialog = showAccuseDialog,
        pendingWildCard = pendingWildCard,
        state = state,
        actualPlayer = actualPlayer,
        shoutedSwintus = shoutedSwintus,
        viewModel = viewModel,
        onDismissColor = { showColorDialog = false; pendingWildCard = null },
        onDismissAccuse = { showAccuseDialog = false },
        onSetError = { errorMessage = it },
        onResetSwintus = { shoutedSwintus = false }
    )

    GameScreenLayout(
        state = state,
        version = version,
        viewModel = viewModel,
        actualPlayer = actualPlayer,
        errorMessage = errorMessage,
        shoutedSwintus = shoutedSwintus,
        onShoutSwintusChanged = { shoutedSwintus = it },
        onShowAccuse = { showAccuseDialog = true },
        onCardClick = { card ->
            if (card is WildCard) {
                pendingWildCard = card
                showColorDialog = true
            } else {
                errorMessage = viewModel.handlePlayCardFromId(actualPlayer.playerId, card, shoutedSwintus)
                if (errorMessage == null) shoutedSwintus = false
            }
        },
        onSetError = { errorMessage = it }
    )
}

@Composable
fun GameScreenLayout(
    state: GameState, version: Int, viewModel: SwintusViewModel, actualPlayer: InGamePlayer,
    errorMessage: String?, shoutedSwintus: Boolean, onShoutSwintusChanged: (Boolean) -> Unit,
    onShowAccuse: () -> Unit, onCardClick: (Card) -> Unit, onSetError: (String?) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize().background(Color(0xFF1A2232)).padding(16.dp)) {
        Column(modifier = Modifier.weight(0.6f).fillMaxHeight()) {
            GameBoardSection(state = state, version = version, onDrawClick = { onSetError(viewModel.handleDrawCard()) })
            PlayersHandSection(state = state, version = version, actualPlayer = actualPlayer, onCardClick = onCardClick, onWrongTurn = onSetError)
        }
        Column(modifier = Modifier.weight(0.4f).fillMaxHeight()) {
            ControlPanelSection(
                viewModel = viewModel, actualPlayer = actualPlayer, errorMessage = errorMessage, shoutedSwintus = shoutedSwintus,
                onShoutSwintusChanged = onShoutSwintusChanged, onShowAccuse = onShowAccuse, onDrawCard = { onSetError(viewModel.handleDrawCard()) },
                onReset = { viewModel.resetToLobby() }
            )
        }
    }
}

@Composable
fun ColumnScope.GameBoardSection(state: GameState, version: Int, onDrawClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().weight(0.55f).padding(8.dp),
        elevation = 6.dp, backgroundColor = Color(0xFF242E42), shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ИГРОВОЙ СТОЛ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF6B00))
            Text("ХОД №${state.turnNumber}", fontSize = 14.sp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(12.dp))

            TableCardsRow(state = state, version = version, onDrawClick = onDrawClick)

            Spacer(modifier = Modifier.height(12.dp))
            TableStatusFooter(state = state)
        }
    }
}

@Composable
fun TableCardsRow(state: GameState, version: Int, onDrawClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        DeckCardSlot(title = "КОЛОДА", subtitle = "Осталось: ${state.giveCard.cards.size} к.", titleColor = Color(0xFFA0AEC0), isDeck = true, onClick = onDrawClick) {
            DeckBackStub()
        }
        Spacer(modifier = Modifier.width(56.dp))
        DeckCardSlot(title = "В СБРОСЕ", subtitle = "В стопке: ${state.discardCard.cards.size + 1} к.", titleColor = Color(0xFFFF8833), isDeck = false, onClick = {}) {
            key(state.topCard, state.turnNumber, version) {
                val cardForUi = state.topCard
                if (cardForUi is WildCard) cardForUi.color = GameColor.GRAY
                CardView(cardForUi)
            }
        }
    }
}

@Composable
fun TableStatusFooter(state: GameState) {
    Text("Направление: ${if (state.direction) "➔ По часовой стрелке" else "➔ Против часовой стрелки"}", fontSize = 14.sp, color = Color.White)
    Spacer(modifier = Modifier.height(4.dp))
    val actualPlayer = state.players.getOrNull(state.currentPlayerIndex)
    val colorSuffix = if (state.topCard is WildCard && (state.topCard as WildCard).chosenColor != GameColor.GRAY) " (Загадан цвет: ${(state.topCard as WildCard).chosenColor.name})" else ""
    Text("СЕЙЧАС ХОДИТ: ${actualPlayer?.name ?: ""}$colorSuffix", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64B5F6))
}

@Composable
fun DeckBackStub() {
    if (Thread.currentThread().contextClassLoader.getResource("cards/back.png") != null) {
        Image(painter = painterResource("cards/back.png"), contentDescription = "Рубашка")
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🐷", fontSize = 42.sp)
            Text("СВИНТУС", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DeckCardSlot(title: String, subtitle: String, titleColor: Color, isDeck: Boolean, onClick: () -> Unit, content: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, fontSize = 13.sp, color = titleColor, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier.size(120.dp, 180.dp).background(Color(0xFF2D3748), RoundedCornerShape(12.dp))
                .border(if (isDeck) 3.dp else 0.dp, Color(0xFFFF6B00), RoundedCornerShape(12.dp))
                .clickable(enabled = isDeck) { onClick() },
            contentAlignment = Alignment.Center
        ) { content() }
        Spacer(modifier = Modifier.height(8.dp))
        Text(subtitle, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ColumnScope.PlayersHandSection(
    state: GameState, version: Int, actualPlayer: InGamePlayer,
    onCardClick: (Card) -> Unit, onWrongTurn: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().weight(0.45f).padding(8.dp),
        elevation = 6.dp, backgroundColor = Color(0xFF242E42), shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("КАРТЫ ИГРОКОВ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                itemsIndexed(state.players) { _, player ->
                    PlayerHandRow(player = player, actualPlayer = actualPlayer, version = version, onCardClick = onCardClick, onWrongTurn = onWrongTurn)
                }
            }
        }
    }
}

@Composable
fun PlayerHandRow(
    player: InGamePlayer, actualPlayer: InGamePlayer, version: Int,
    onCardClick: (Card) -> Unit, onWrongTurn: (String) -> Unit
) {
    val isCurrentTurnOwner = player.playerId == actualPlayer.playerId
    Row(
        modifier = Modifier.fillMaxWidth().background(if (isCurrentTurnOwner) Color(0xFF2C3A54) else Color.Transparent, shape = RoundedCornerShape(12.dp)).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("${player.name} (${player.hand.size} к.)", fontWeight = FontWeight.Bold, color = if (isCurrentTurnOwner) Color(0xFFFF8833) else Color.White, modifier = Modifier.width(130.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            player.hand.forEach { card ->
                key(card, card.hashCode(), player.hand.size, version) {
                    InteractiveCardSlot(isCurrentTurnOwner = isCurrentTurnOwner, actualPlayer = actualPlayer, card = card, onCardClick = onCardClick, onWrongTurn = onWrongTurn)
                }
            }
        }
    }
}

@Composable
fun InteractiveCardSlot(isCurrentTurnOwner: Boolean, actualPlayer: InGamePlayer, card: Card, onCardClick: (Card) -> Unit, onWrongTurn: (String) -> Unit) {
    Box(
        modifier = Modifier.size(65.dp, 95.dp).border(width = if (isCurrentTurnOwner) 2.dp else 1.dp, color = if (isCurrentTurnOwner) Color(0xFFFF6B00) else Color(0xFF475569), shape = RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(2.dp).pointerInput(Unit) {}) {
            if (card is WildCard) card.color = GameColor.GRAY
            CardView(card = card)
        }
        Box(modifier = Modifier.fillMaxSize().background(Color.Transparent).clickable {
            if (!isCurrentTurnOwner) onWrongTurn("Сейчас ход игрока ${actualPlayer.name}!") else onCardClick(card)
        })
    }
}

@Composable
fun ColumnScope.ControlPanelSection(
    viewModel: SwintusViewModel, actualPlayer: InGamePlayer, errorMessage: String?, shoutedSwintus: Boolean,
    onShoutSwintusChanged: (Boolean) -> Unit, onShowAccuse: () -> Unit, onDrawCard: () -> Unit, onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().weight(0.45f).padding(8.dp),
        elevation = 6.dp, backgroundColor = Color(0xFF242E42), shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("ИСТОРИЯ ДЕЙСТВИЙ", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            ActionLogsTerminal(viewModel = viewModel)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth().weight(0.55f).padding(8.dp),
        elevation = 6.dp, backgroundColor = Color(0xFF242E42), shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text("УПРАВЛЕНИЕ ИГРОЙ", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.align(Alignment.CenterHorizontally))
            ControlStatusMessage(errorMessage = errorMessage, actualPlayer = actualPlayer)
            ControlButtonsGrid(shoutedSwintus = shoutedSwintus, actualPlayer = actualPlayer, viewModel = viewModel, onShoutSwintusChanged = onShoutSwintusChanged, onShowAccuse = onShowAccuse, onDrawCard = onDrawCard, onReset = onReset)
        }
    }
}

@Composable
fun ActionLogsTerminal(viewModel: SwintusViewModel) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1E2638), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp)).padding(8.dp)) {
        LazyColumn(modifier = Modifier.fillMaxSize(), reverseLayout = true) {
            items(viewModel.actionLogs.toList()) { log ->
                Text(text = log, fontSize = 12.sp, color = Color(0xFFE2E8F0), modifier = Modifier.padding(vertical = 2.dp))
            }
        }
    }
}

@Composable
fun ControlStatusMessage(errorMessage: String?, actualPlayer: InGamePlayer) {
    Box(modifier = Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.Center) {
        Text(
            text = errorMessage ?: "Сейчас ход игрока ${actualPlayer.name}!",
            color = if (errorMessage != null) Color(0xFFEF4444) else Color(0xFFFF8833),
            fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ControlButtonsGrid(
    shoutedSwintus: Boolean, actualPlayer: InGamePlayer, viewModel: SwintusViewModel,
    onShoutSwintusChanged: (Boolean) -> Unit, onShowAccuse: () -> Unit, onDrawCard: () -> Unit, onReset: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { onShoutSwintusChanged(true); viewModel.actionLogs.add("${actualPlayer.name} подготовил крик: СВИНТУС!") },
                modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(backgroundColor = if (shoutedSwintus) Color(0xFFFF6B00) else Color(0xFF475569)), shape = RoundedCornerShape(10.dp)
            ) { Text("🐷 СВИНТУС!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp) }

            Button(onClick = onShowAccuse, modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF7E57C2)), shape = RoundedCornerShape(10.dp)) {
                Text("👁️ ОБВИНЯЮ!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onDrawCard, modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF10B981)), shape = RoundedCornerShape(10.dp)) { Text("Взять карту", color = Color.White, fontWeight = FontWeight.Bold) }
            Button(onClick = onReset, modifier = Modifier.weight(1f).height(48.dp), colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFEF4444)), shape = RoundedCornerShape(10.dp)) { Text("В лобби", color = Color.White, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun GameDialogsWrapper(
    showColorDialog: Boolean, showAccuseDialog: Boolean, pendingWildCard: Card?, state: GameState, actualPlayer: InGamePlayer,
    shoutedSwintus: Boolean, viewModel: SwintusViewModel, onDismissColor: () -> Unit, onDismissAccuse: () -> Unit,
    onSetError: (String?) -> Unit, onResetSwintus: () -> Unit
) {
    if (showColorDialog && pendingWildCard != null) {
        ColorPickerDialog(
            onDismiss = onDismissColor,
            onColorSelected = { gameColor ->
                if (pendingWildCard is WildCard) {
                    (pendingWildCard as WildCard).chosenColor = gameColor
                    pendingWildCard.color = GameColor.GRAY
                }
                val result = viewModel.handlePlayCardFromId(actualPlayer.playerId, pendingWildCard, shoutedSwintus)
                onSetError(result)
                if (result == null) {
                    onResetSwintus()
                    onDismissColor()
                }
            }
        )
    }
    if (showAccuseDialog) {
        AccusePlayerDialog(state = state, actualPlayer = actualPlayer, onDismiss = onDismissAccuse, onAccuse = { targetId ->
            onSetError(viewModel.handleAccusePlayer(targetId, actualPlayer.name))
            onDismissAccuse()
        })
    }
}

@Composable
fun GameOverOverlay(state: GameState, onReset: () -> Unit) {
    val winnerName = state.players.find { it.hand.isEmpty() }?.name ?: "Кто-то"
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF141923)), contentAlignment = Alignment.Center) {
        Card(modifier = Modifier.width(450.dp).padding(24.dp), backgroundColor = Color(0xFF242E42), shape = RoundedCornerShape(24.dp), elevation = 12.dp) {
            Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎉 ПОБЕДА! 🎉", fontSize = 36.sp, fontWeight = FontWeight.Black, color = Color(0xFFFF6B00), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                Text("Игрок $winnerName избавился от всех карт и победил!", fontSize = 18.sp, color = Color.White, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onReset, modifier = Modifier.fillMaxWidth().height(54.dp), colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF10B981)), shape = RoundedCornerShape(12.dp)) { Text("Вернуться в лобби", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
fun ColorPickerDialog(onDismiss: () -> Unit, onColorSelected: (GameColor) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss, backgroundColor = Color(0xFF1E2640), shape = RoundedCornerShape(16.dp),
        title = { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Text("🎨 ВЫБЕРЕТЕ ЦВЕТ", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black) } },
        buttons = {
            Column(modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("КРАСНЫЙ" to GameColor.RED, "ЗЕЛЕНЫЙ" to GameColor.GREEN, "СИНИЙ" to GameColor.BLUE, "ЖЕЛТЫЙ" to GameColor.YELLOW).forEach { (name, gameColor) ->
                    Button(
                        onClick = { onColorSelected(gameColor) },
                        modifier = Modifier.fillMaxWidth().height(50.dp).border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = when(gameColor) { GameColor.RED -> Color(0xFFE53935); GameColor.GREEN -> Color(0xFF43A047); GameColor.BLUE -> Color(0xFF1E88E5); GameColor.YELLOW -> Color(0xFFFFB300); else -> Color.Gray })
                    ) { Text(name, color = if (gameColor == GameColor.YELLOW) Color(0xFF242E42) else Color.White, fontWeight = FontWeight.Bold) }
                }
            }
        }
    )
}

@Composable
fun AccusePlayerDialog(state: GameState, actualPlayer: InGamePlayer, onDismiss: () -> Unit, onAccuse: (UUID) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss, title = { Text("Кого вы хотите обвинить?", fontWeight = FontWeight.Bold) },
        buttons = {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.players.filter { it.playerId != actualPlayer.playerId }.forEach { targetPlayer ->
                    Button(onClick = { onAccuse(targetPlayer.playerId) }, modifier = Modifier.fillMaxWidth().height(45.dp)) { Text(targetPlayer.name) }
                }
            }
        }
    )
}