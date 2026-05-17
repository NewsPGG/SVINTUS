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
    val darkBackground = Color(0xFF121824)
    val cardBackground = Color(0xFF1E2638)
    val accentOrange = Color(0xFFFF6B00)
    val accentOrangeVariant = Color(0xFFFF8833)
    val textLight = Color(0xFFE2E8F0)
    val textMuted = Color(0xFF94A3B8)
    val borderStrokeColor = Color(0xFF334155)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            backgroundColor = cardBackground,
            elevation = 16.dp,
            modifier = Modifier
                .width(480.dp)
                .height(580.dp)
                .border(1.dp, borderStrokeColor, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "СВИНТУС",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = accentOrange,
                        letterSpacing = 2.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "ПОДГОТОВКА К БИТВЕ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = textMuted,
                        letterSpacing = 4.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newPlayerName,
                            onValueChange = { newPlayerName = it },
                            label = { Text("Имя нового игрока", color = textMuted) },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                textColor = textLight,
                                focusedBorderColor = accentOrange,
                                unfocusedBorderColor = borderStrokeColor,
                                cursorColor = accentOrange,
                                focusedLabelColor = accentOrange
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = {
                                if (newPlayerName.isNotBlank() && lobbyPlayers.size < 8) {
                                    lobbyPlayers.add(
                                        PlayerProfile(
                                            id = UUID.randomUUID(),
                                            username = newPlayerName.trim(),
                                            rating = 0,
                                            gamesPlayed = 0,
                                            wins = 0
                                        )
                                    )
                                    newPlayerName = ""
                                }
                            },
                            enabled = newPlayerName.isNotBlank() && lobbyPlayers.size < 8,
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = accentOrange,
                                contentColor = Color.White,
                                disabledBackgroundColor = borderStrokeColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(56.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Добавить")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Участники лобби",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = textLight
                        )
                        Text(
                            text = "${lobbyPlayers.size} / 8",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (lobbyPlayers.size >= 2) accentOrange else textMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (lobbyPlayers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Добавьте минимум 2 игроков, чтобы начать игру",
                                color = textMuted,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(lobbyPlayers) { player ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF263147), RoundedCornerShape(12.dp))
                                        .border(1.dp, Color(0xFF2D3A54), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = accentOrange,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = player.username,
                                            color = textLight,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 15.sp
                                        )
                                    }
                                    IconButton(
                                        onClick = { lobbyPlayers.remove(player) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Удалить",
                                            tint = Color(0xFFEF4444)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val isButtonEnabled = lobbyPlayers.size >= 2

                Button(
                    onClick = {
                        if (isButtonEnabled) {
                            val administrator = GameAdministrator()
                            administrator.startGame(lobbyPlayers.toList())
                            viewModel.startGameFromLobby(administrator.gameState)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = isButtonEnabled,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Transparent,
                        disabledBackgroundColor = borderStrokeColor
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    val buttonModifier = if (isButtonEnabled) {
                        Modifier
                            .fillMaxSize()
                            .background(Brush.horizontalGradient(listOf(accentOrange, accentOrangeVariant)))
                    } else {
                        Modifier
                            .fillMaxSize()
                            .background(borderStrokeColor)
                    }

                    Box(
                        modifier = buttonModifier,
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ПОЕХАЛИ!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isButtonEnabled) Color.White else textMuted,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }
        }
    }
}