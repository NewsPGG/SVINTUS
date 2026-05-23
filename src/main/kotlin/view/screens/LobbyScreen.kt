package view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import viewmodel.SwintusViewModel
import Application.PlayerProfile
import Application.GameAdministrator
import java.util.UUID

@Composable
fun LobbyScreen(viewModel: SwintusViewModel) {
    val lobbyPlayers = remember { mutableStateListOf<PlayerProfile>() }
    var newPlayerName by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    var leaderboardData by remember { mutableStateOf(listOf<PlayerProfile>()) }

    LaunchedEffect(Unit, selectedTab) {
        if (selectedTab == 1) {
            leaderboardData = viewModel.loadLeaderboard()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121824))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            backgroundColor = Color(0xFF1E2638),
            elevation = 16.dp,
            modifier = Modifier
                .width(520.dp)
                .height(620.dp)
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
                LobbyHeader()

                LobbyTabSelector(selectedTab) { selectedTab = it }

                Spacer(modifier = Modifier.height(24.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (selectedTab == 0) {
                        LobbySetupContent(
                            lobbyPlayers = lobbyPlayers,
                            newPlayerName = newPlayerName,
                            onNameChange = { newPlayerName = it },
                            onAddPlayer = {
                                if (newPlayerName.isNotBlank() && lobbyPlayers.size < 8) {
                                    lobbyPlayers.add(PlayerProfile(UUID.randomUUID(), newPlayerName.trim(), 0, 0, 0))
                                    newPlayerName = ""
                                }
                            },
                            onRemovePlayer = { lobbyPlayers.remove(it) },
                            onStartGame = {
                                val administrator = GameAdministrator()
                                administrator.startGame(lobbyPlayers.toList())
                                viewModel.startGameFromLobby(administrator.gameState)
                            }
                        )
                    } else {
                        LeaderboardContent(leaderboardData)
                    }
                }
            }
        }
    }
}

@Composable
fun LobbyTabSelector(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color(0xFF121824), RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        TabItem(
            text = "БИТВА",
            isSelected = selectedTab == 0,
            modifier = Modifier.weight(1f),
            onClick = { onTabSelected(0) }
        )
        TabItem(
            text = "ТОП ИГРОКОВ",
            isSelected = selectedTab == 1,
            modifier = Modifier.weight(1f),
            onClick = { onTabSelected(1) }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TabItem(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color(0xFF2D3A54) else Color.Transparent
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (isSelected) Color(0xFFFF6B00) else Color(0xFF94A3B8),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun LobbySetupContent(
    lobbyPlayers: List<PlayerProfile>,
    newPlayerName: String,
    onNameChange: (String) -> Unit,
    onAddPlayer: () -> Unit,
    onRemovePlayer: (PlayerProfile) -> Unit,
    onStartGame: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        PlayerInputField(
            value = newPlayerName,
            onValueChange = onNameChange,
            onAddClick = onAddPlayer,
            currentCount = lobbyPlayers.size
        )

        Spacer(modifier = Modifier.height(24.dp))
        LobbyPlayersCounter(currentCount = lobbyPlayers.size)
        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.fillMaxSize()) {
                LobbyPlayersListSection(lobbyPlayers, onRemovePlayer)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        StartGameButton(playerCount = lobbyPlayers.size, onStartGame = onStartGame)
    }
}

@Composable
fun LeaderboardContent(leaderboard: List<PlayerProfile>) {
    if (leaderboard.isEmpty()) {
        EmptyLeaderboardStub()
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("ИГРОК", modifier = Modifier.weight(2f), color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("LP", modifier = Modifier.weight(1f), color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Text("ПОБЕДЫ", modifier = Modifier.weight(1f), color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(leaderboard.sortedByDescending { it.rating }) { player ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF263147), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(player.username, modifier = Modifier.weight(2f), color = Color.White, fontWeight = FontWeight.Medium)
                        Text("${player.rating}", modifier = Modifier.weight(1f), color = Color(0xFFFF6B00), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text("${player.wins}", modifier = Modifier.weight(1f), color = Color.White, textAlign = TextAlign.End)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyLeaderboardStub() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "История пуста.\nСыграйте первую партию!",
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun LobbyHeader() {
    Text(
        text = "СВИНТУС",
        fontSize = 32.sp,
        fontWeight = FontWeight.Black,
        color = Color(0xFFFF6B00),
        letterSpacing = 2.sp,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
    Text(
        text = "ПОДГОТОВКА К БИТВЕ",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF94A3B8),
        letterSpacing = 4.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun PlayerInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onAddClick: () -> Unit,
    currentCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NameInputField(value = value, onValueChange = onValueChange)

        Spacer(modifier = Modifier.width(12.dp))

        AddPlayerButton(
            isEnabled = value.isNotBlank() && currentCount < 8,
            onClick = onAddClick
        )
    }
}

@Composable
fun RowScope.NameInputField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Имя нового игрока", color = Color(0xFF94A3B8)) },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = Color(0xFFE2E8F0),
            focusedBorderColor = Color(0xFFFF6B00),
            unfocusedBorderColor = Color(0xFF334155),
            cursorColor = Color(0xFFFF6B00),
            focusedLabelColor = Color(0xFFFF6B00)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.weight(1f),
        singleLine = true
    )
}

@Composable
fun AddPlayerButton(isEnabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color(0xFFFF6B00),
            contentColor = Color.White,
            disabledBackgroundColor = Color(0xFF334155)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.size(56.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = "Добавить")
    }
}

@Composable
fun LobbyPlayersCounter(currentCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Участники лобби",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE2E8F0)
        )
        Text(
            text = "$currentCount / 8",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (currentCount >= 2) Color(0xFFFF6B00) else Color(0xFF94A3B8)
        )
    }
}

@Composable
fun ColumnScope.LobbyPlayersListSection(
    lobbyPlayers: List<PlayerProfile>,
    onRemovePlayer: (PlayerProfile) -> Unit
) {
    if (lobbyPlayers.isEmpty()) {
        EmptyLobbyStub()
    } else {
        PlayersLazyColumn(lobbyPlayers = lobbyPlayers, onRemovePlayer = onRemovePlayer)
    }
}

@Composable
fun ColumnScope.PlayersLazyColumn(
    lobbyPlayers: List<PlayerProfile>,
    onRemovePlayer: (PlayerProfile) -> Unit
) {
    LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(lobbyPlayers) { player ->
            PlayerLobbyRow(player = player, onRemovePlayer = onRemovePlayer)
        }
    }
}

@Composable
fun ColumnScope.EmptyLobbyStub() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Добавьте минимум 2 игроков, чтобы начать игру",
            color = Color(0xFF94A3B8),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun PlayerLobbyRow(player: PlayerProfile, onRemovePlayer: (PlayerProfile) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF263147), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF2D3A54), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayerInfoLabel(username = player.username)
        DeletePlayerButton(onClick = { onRemovePlayer(player) })
    }
}

@Composable
fun PlayerInfoLabel(username: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            Icons.Default.Person,
            contentDescription = null,
            tint = Color(0xFFFF6B00),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = username,
            color = Color(0xFFE2E8F0),
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp
        )
    }
}

@Composable
fun DeletePlayerButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(24.dp)
    ) {
        Icon(
            Icons.Default.Delete,
            contentDescription = "Удалить",
            tint = Color(0xFFEF4444)
        )
    }
}

@Composable
fun StartGameButton(playerCount: Int, onStartGame: () -> Unit) {
    val isButtonEnabled = playerCount >= 2

    Button(
        onClick = onStartGame,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        enabled = isButtonEnabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.Transparent,
            disabledBackgroundColor = Color(0xFF334155)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        StartGameButtonContent(isButtonEnabled = isButtonEnabled)
    }
}

@Composable
fun StartGameButtonContent(isButtonEnabled: Boolean) {
    val buttonModifier = if (isButtonEnabled) {
        Modifier
            .fillMaxSize()
            .background(Brush.horizontalGradient(listOf(Color(0xFFFF6B00), Color(0xFFFF8833))))
    } else {
        Modifier
            .fillMaxSize()
            .background(Color(0xFF334155))
    }

    Box(
        modifier = buttonModifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "ПОЕХАЛИ!",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = if (isButtonEnabled) Color.White else Color(0xFF94A3B8),
            letterSpacing = 2.sp
        )
    }
}