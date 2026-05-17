package view.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import Cards.Types.*

@Composable
fun CardView(
    card: Cards.Card?,
    isBack: Boolean = false,
    onClick: () -> Unit = {}
) {
    val fileName = when {
        isBack || card == null -> "back.png"
        else -> {
            val color = card.color.toString().lowercase()

            val cardValue = when (card) {
                is NumberCard -> card.value.toString()
                is ActionCard -> {
                    val effectName = card.effect.javaClass.simpleName.lowercase()
                    if (effectName.contains("taketwo") || effectName.contains("three")) "draw3" else effectName.replace("effect", "")
                }
                is WildCard -> "wild"
                else -> "unknown"
            }

            "${color}_$cardValue.png"
        }
    }

    Image(
        painter = painterResource("cards/$fileName"),
        contentDescription = null,
        modifier = Modifier
            .width(100.dp)
            .height(150.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentScale = ContentScale.Fit
    )
}