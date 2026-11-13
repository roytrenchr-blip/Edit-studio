package com.editstudio.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.editstudio.app.ui.theme.NavyAccentTeal
import com.editstudio.app.ui.theme.NavyBorder
import com.editstudio.app.ui.theme.NavyCard
import com.editstudio.app.ui.theme.NavyPrimary
import com.editstudio.app.ui.theme.NavyPrimaryDeep
import com.editstudio.app.ui.theme.NavyTextPrimary
import com.editstudio.app.ui.theme.NavyTextSecondary

data class TourStep(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@Composable
fun GuidedTour(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = listOf(
        TourStep(
            title = "Swagat Hai Edit Studio Mein!",
            description = "Upar project naam tap karke rename karein, aur beech mein transparent checkerboard preview dekhein.",
            icon = Icons.Default.PlayArrow
        ),
        TourStep(
            title = "Center Playhead Timeline",
            description = "Timeline mein playhead beech mein fixed hai — layers ko swipe karein, edges se trim karein, aur '+' dabakar naya element add karein.",
            icon = Icons.Default.Timeline
        ),
        TourStep(
            title = "One-Tap Effects & Motion",
            description = "Kisi bhi layer par tap karein — neeche Properties Grid khul jayega. Effects aur Motion browser mein har preset ka live preview milega.",
            icon = Icons.Default.Animation
        ),
        TourStep(
            title = "Simple vs Pro Mode",
            description = "Top bar mein Simple/Pro switch se aasan sliders ya Keyframe Diamonds & Bezier Curves chalu karein.",
            icon = Icons.Default.AutoAwesome
        )
    )

    var currentStep by remember { mutableIntStateOf(0) }
    val step = steps[currentStep]

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(NavyCard)
                .border(1.dp, NavyBorder, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(NavyPrimaryDeep.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = step.icon,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "Step ${currentStep + 1} of 4",
                    style = MaterialTheme.typography.labelSmall,
                    color = NavyAccentTeal,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = NavyTextPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NavyTextSecondary,
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Skip Tour", color = NavyTextSecondary)
                    }

                    Button(
                        onClick = {
                            if (currentStep < steps.size - 1) {
                                currentStep++
                            } else {
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimaryDeep)
                    ) {
                        Text(if (currentStep == steps.size - 1) "Samajh Gaya!" else "Aage (Next)")
                    }
                }
            }
        }
    }
}
