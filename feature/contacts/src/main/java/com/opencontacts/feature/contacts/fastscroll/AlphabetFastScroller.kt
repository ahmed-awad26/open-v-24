package com.opencontacts.feature.contacts.fastscroll

import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun AlphabetFastScroller(
    letters: List<String>,
    availableLetters: Set<String>,
    modifier: Modifier = Modifier,
    onLetterChanged: (String) -> Unit,
    onInteractionEnd: () -> Unit = {},
) {
    var activeLetter by remember(letters) { mutableStateOf<String?>(null) }
    var railHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val bubbleHeightPx = with(density) { 40.dp.roundToPx() }
    val bubbleOffsetXPx = with(density) { 52.dp.roundToPx() }

    fun handleTouch(y: Float) {
        val index = mapTouchYToLetterIndex(
            touchY = y,
            containerHeightPx = railHeightPx.toFloat(),
            letterCount = letters.size,
        )
        val newLetter = letters[index]
        if (newLetter != activeLetter) {
            activeLetter = newLetter
            onLetterChanged(newLetter)
        }
    }

    Box(
        modifier = modifier
            .width(42.dp)
            .fillMaxHeight(),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 12.dp, bottom = 12.dp, end = 2.dp)
                .width(36.dp)
                .fillMaxHeight(0.82f)
                .onSizeChanged { railHeightPx = it.height }
                .pointerInteropFilter { event ->
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN,
                        MotionEvent.ACTION_MOVE -> {
                            handleTouch(event.y)
                            true
                        }

                        MotionEvent.ACTION_UP,
                        MotionEvent.ACTION_CANCEL -> {
                            activeLetter = null
                            onInteractionEnd()
                            true
                        }

                        else -> false
                    }
                },
            contentAlignment = Alignment.CenterEnd,
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(10.dp)
                    .fillMaxHeight()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f),
                        shape = RoundedCornerShape(999.dp),
                    ),
            )
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(36.dp)
                    .padding(vertical = 6.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                letters.forEach { letter ->
                    val isActive = letter == activeLetter
                    val isAvailable = letter in availableLetters
                    Text(
                        text = letter,
                        color = when {
                            isActive -> MaterialTheme.colorScheme.primary
                            isAvailable -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.86f)
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)
                        },
                        style = if (isActive) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .background(
                                color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                                shape = CircleShape,
                            )
                            .padding(horizontal = 4.dp, vertical = 1.dp),
                    )
                }
            }
            activeLetter?.let { letter ->
                val slotHeight = if (letters.isNotEmpty() && railHeightPx > 0) railHeightPx.toFloat() / letters.size else 0f
                val rawTop = (slotHeight * letters.indexOf(letter)) + ((slotHeight - bubbleHeightPx) / 2f)
                val bubbleTop = rawTop.coerceIn(0f, (railHeightPx - bubbleHeightPx).coerceAtLeast(0).toFloat())
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset {
                            IntOffset(
                                x = -bubbleOffsetXPx,
                                y = bubbleTop.roundToInt(),
                            )
                        },
                    shape = RoundedCornerShape(14.dp),
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = letter,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}
