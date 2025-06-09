package io.toolbox.passwdmanager.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.pr0gramm3r101.utils.applyIf
import com.pr0gramm3r101.utils.left
import com.pr0gramm3r101.utils.link
import com.pr0gramm3r101.utils.right
import io.toolbox.passwdmanager.utils.PasswordStrength

@OptIn(ExperimentalStdlibApi::class)
@Composable
fun PasswordStrengthMeter(
    password: String,
    modifier: Modifier = Modifier
) {
    val strengthResult = remember(password) {
        PasswordStrength.calculateStrength(password)
    }

    val progress by animateFloatAsState(strengthResult.score / 4f)
    val color by animateColorAsState(Color(strengthResult.color))

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Strength bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }

        Spacer(Modifier.height(8.dp))

        // Crack time estimate
        Text(
            text = "Estimated crack time: ${PasswordStrength.formatCrackTime(strengthResult.crackTime)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Feedback messages
        if (strengthResult.feedback.isNotEmpty()) {
            var expanded by remember { mutableStateOf(false) }
            val rotation by animateFloatAsState(if (expanded) 0f else 180f)

            Spacer(Modifier.height(10.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    ConstraintLayout(
                        modifier = Modifier
                            .padding(8.dp, 4.dp, 4.dp, 4.dp)
                            .fillMaxWidth()
                    ) {
                        val (icon, text, expand) = createRefs()

                        Icon(
                            imageVector = Icons.Filled.Lightbulb,
                            contentDescription = null,
                            modifier = Modifier.constrainAs(icon) {
                                top link parent.top
                                bottom link parent.bottom
                                left link parent.left
                            }
                        )
                        Text(
                            text = "Suggestions",
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .constrainAs(text) {
                                    top link parent.top
                                    bottom link parent.bottom
                                    left link icon.right
                                }
                        )
                        IconButton(
                            onClick = { expanded = !expanded },
                            modifier = Modifier.constrainAs(expand) {
                                top link parent.top
                                bottom link parent.bottom
                                right link parent.right
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowUp,
                                contentDescription = if (expanded) "Close" else "Expand",
                                modifier = Modifier.rotate(rotation)
                            )
                        }
                    }

                    Column(
                        Modifier
                            .animateContentSize()
                            .applyIf(!expanded) { height(0.dp) }
                            .padding(bottom = 16.dp)
                    ) {
                        strengthResult.feedback.forEach { feedback ->
                            Text(
                                text = feedback,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
} 