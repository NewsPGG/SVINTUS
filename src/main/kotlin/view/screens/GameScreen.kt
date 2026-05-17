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
    val size = state.players.size
    if (size == 0) return

    if (!state.gameStatus) {
        val winner = state.players.find { it.hand.isEmpty() }
        val winnerName = winner?.name ?: "Кто-то"

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF141923)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .width(450.dp)
                    .padding(24.dp),
                backgroundColor = Color(0xFF242E42),
                shape = RoundedCornerShape(24.dp),
                elevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "🎉 ПОБЕДА! 🎉",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF6B00),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Игрок $winnerName избавился от всех карт и победил в этой партии!",
                        fontSize = 18.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { viewModel.resetToLobby() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Вернуться в лобби",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        return
    }

    val actualPlayerIndex = state.currentPlayerIndex
    val actualPlayer = state.players.getOrNull(actualPlayerIndex) ?: return

    LaunchedEffect(state.currentPlayerIndex, state.turnNumber) {
        errorMessage = null
    }

    if (showColorDialog && pendingWildCard != null) {
        AlertDialog(
            onDismissRequest = {
                showColorDialog = false
                pendingWildCard = null
            },
            backgroundColor = Color(0xFF1E2640),
            shape = RoundedCornerShape(16.dp),
            title = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "🎨 ВЫБЕРЕТЕ ЦВЕТ",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            },
            buttons = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val colors = listOf(
                        "КРАСНЫЙ" to GameColor.RED,
                        "ЗЕЛЕНЫЙ" to GameColor.GREEN,
                        "СИНИЙ" to GameColor.BLUE,
                        "ЖЕЛТЫЙ" to GameColor.YELLOW
                    )
                    colors.forEach { (name, gameColor) ->
                        Button(
                            onClick = {
                                if (pendingWildCard is WildCard) {
                                    (pendingWildCard as WildCard).chosenColor = gameColor
                                    pendingWildCard!!.color = GameColor.GRAY
                                }

                                val result = viewModel.handlePlayCardFromId(actualPlayer.playerId, pendingWildCard!!, shoutedSwintus)
                                if (result != null) {
                                    errorMessage = result
                                } else {
                                    errorMessage = null
                                    shoutedSwintus = false
                                    showColorDialog = false
                                    pendingWildCard = null
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = when(gameColor) {
                                    GameColor.RED -> Color(0xFFE53935)
                                    GameColor.GREEN -> Color(0xFF43A047)
                                    GameColor.BLUE -> Color(0xFF1E88E5)
                                    GameColor.YELLOW -> Color(0xFFFFB300)
                                    else -> Color.Gray
                                }
                            ),
                            elevation = ButtonDefaults.elevation(defaultElevation = 4.dp, pressedElevation = 8.dp)
                        ) {
                            Text(
                                text = name,
                                color = if (gameColor == GameColor.YELLOW) Color(0xFF242E42) else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        )
    }

    if (showAccuseDialog) {
        AlertDialog(
            onDismissRequest = { showAccuseDialog = false },
            title = { Text("Кого вы хотите обвинить?", fontWeight = FontWeight.Bold) },
            buttons = {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.players.filter { it.playerId != actualPlayer.playerId }.forEach { targetPlayer ->
                        Button(
                            onClick = {
                                val result = viewModel.handleAccusePlayer(targetPlayer.playerId, actualPlayer.name)
                                if (result != null) {
                                    errorMessage = result
                                } else {
                                    errorMessage = null
                                    showAccuseDialog = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(45.dp)
                        ) {
                            Text(targetPlayer.name)
                        }
                    }
                }
            }
        )
    }

    Row(modifier = Modifier.fillMaxSize().background(Color(0xFF1A2232)).padding(16.dp)) {
        Column(modifier = Modifier.weight(0.6f).fillMaxHeight()) {

            Card(
                modifier = Modifier.fillMaxWidth().weight(0.55f).padding(8.dp),
                elevation = 6.dp,
                backgroundColor = Color(0xFF242E42),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ИГРОВОЙ СТОЛ", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF6B00))
                    Text("ХОД №${state.turnNumber}", fontSize = 14.sp, color = Color.LightGray)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        ) {
                            Text("КОЛОДА", fontSize = 13.sp, color = Color(0xFFA0AEC0), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .size(120.dp, 180.dp)
                                    .background(Color(0xFF2D3748), RoundedCornerShape(12.dp))
                                    .border(3.dp, Color(0xFFFF6B00), RoundedCornerShape(12.dp))
                                    .clickable {
                                        val result = viewModel.handleDrawCard()
                                        if (result != null) errorMessage = result else errorMessage = null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                val assetExists = Thread.currentThread().contextClassLoader.getResource("cards/back.png") != null

                                if (assetExists) {
                                    Image(
                                        painter = painterResource("cards/back.png"),
                                        contentDescription = "Рубашка колоды",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🐷", fontSize = 42.sp)
                                        Text("СВИНТУС", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val cardsLeft = state.giveCard.cards.size
                            Text("Осталось: $cardsLeft к.", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(24.dp))

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 32.dp)) {
                            Text("В СБРОСЕ", fontSize = 13.sp, color = Color(0xFFFF8833), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            Box(modifier = Modifier.size(120.dp, 180.dp), contentAlignment = Alignment.Center) {
                                key(state.topCard, state.turnNumber, version) {
                                    val cardForUi = state.topCard
                                    if (cardForUi is WildCard) {
                                        cardForUi.color = GameColor.GRAY
                                    }
                                    CardView(cardForUi)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val cardsDiscarded = state.discardCard.cards.size + 1
                            Text("В стопке: $cardsDiscarded к.", fontSize = 14.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Направление: ${if (state.direction) "➔ По часовой стрелке" else "➔ Против часовой стрелки"}",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    val currentTopCard = state.topCard
                    val colorSuffix = if (currentTopCard is WildCard && currentTopCard.chosenColor != GameColor.GRAY) {
                        " (Загадан цвет: ${currentTopCard.chosenColor.name})"
                    } else ""

                    Text(
                        text = "СЕЙЧАС ХОДИТ: ${actualPlayer.name}$colorSuffix",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64B5F6)
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().weight(0.45f).padding(8.dp),
                elevation = 6.dp,
                backgroundColor = Color(0xFF242E42),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("КАРТЫ ИГРОКОВ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(state.players) { _, player ->
                            val isCurrentTurnOwner = player.playerId == actualPlayer.playerId

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isCurrentTurnOwner) Color(0xFF2C3A54) else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${player.name} (${player.hand.size} к.)",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrentTurnOwner) Color(0xFFFF8833) else Color.White,
                                    modifier = Modifier.width(130.dp)
                                )

                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    player.hand.forEach { card ->
                                        key(card, card.hashCode(), player.hand.size, version) {
                                            Box(
                                                modifier = Modifier
                                                    .size(65.dp, 95.dp)
                                                    .border(
                                                        width = if (isCurrentTurnOwner) 2.dp else 1.dp,
                                                        color = if (isCurrentTurnOwner) Color(0xFFFF6B00) else Color(0xFF475569),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Box(modifier = Modifier.fillMaxSize().padding(2.dp).pointerInput(Unit) {}) {
                                                    if (card is WildCard) {
                                                        card.color = GameColor.GRAY
                                                    }
                                                    CardView(card = card)
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(Color.Transparent)
                                                        .clickable {
                                                            if (!isCurrentTurnOwner) {
                                                                errorMessage = "Сейчас ход игрока ${actualPlayer.name}!"
                                                                return@clickable
                                                            }

                                                            if (card is WildCard) {
                                                                pendingWildCard = card
                                                                showColorDialog = true
                                                            } else {
                                                                val result = viewModel.handlePlayCardFromId(player.playerId, card, shoutedSwintus)
                                                                if (result != null) {
                                                                    errorMessage = result
                                                                } else {
                                                                    errorMessage = null
                                                                    shoutedSwintus = false
                                                                }
                                                            }
                                                        }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.weight(0.4f).fillMaxHeight()) {
            Card(
                modifier = Modifier.fillMaxWidth().weight(0.45f).padding(8.dp),
                elevation = 6.dp,
                backgroundColor = Color(0xFF242E42),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ИСТОРИЯ ДЕЙСТВИЙ", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1E2638), RoundedCornerShape(8.dp)).border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp)).padding(8.dp)) {
                        LazyColumn(modifier = Modifier.fillMaxSize(), reverseLayout = true) {
                            items(viewModel.actionLogs.toList()) { log ->
                                Text(text = log, fontSize = 12.sp, color = Color(0xFFE2E8F0), modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().weight(0.55f).padding(8.dp),
                elevation = 6.dp,
                backgroundColor = Color(0xFF242E42),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "УПРАВЛЕНИЕ ИГРОЙ", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.align(Alignment.CenterHorizontally))

                    Box(modifier = Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.Center) {
                        if (errorMessage != null) {
                            Text(text = errorMessage!!, color = Color(0xFFEF4444), fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        } else {
                            Text(text = "Сейчас ход игрока ${actualPlayer.name}!", color = Color(0xFFFF8833), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                shoutedSwintus = true
                                errorMessage = "Вы объявили Свинтус! Выберите карту для хода."
                                viewModel.actionLogs.add("${actualPlayer.name} подготовил крик: СВИНТУС!")
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = if (shoutedSwintus) Color(0xFFFF6B00) else Color(0xFF475569)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("🐷 СВИНТУС!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { showAccuseDialog = true },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF7E57C2)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("👁️ ОБВИНЯЮ!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val result = viewModel.handleDrawCard()
                                if (result != null) errorMessage = result else errorMessage = null
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Взять карту", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.resetToLobby() },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("В лобби", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}