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
import androidx.compose.material.icons.filled.Check
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
import com.editstudio.app.ui.theme.StudioBorder
import com.editstudio.app.ui.theme.StudioCard
import com.editstudio.app.ui.theme.StudioCyberCyan
import com.editstudio.app.ui.theme.StudioNeonPurple
import com.editstudio.app.ui.theme.TextPrimary
import com.editstudio.app.ui.theme.TextSecondary

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
            description = "Ye ek professional native video aur motion graphics editor hai. Har button ko long-press karke Hinglish tooltip dekh sakte hain.",
            icon = Icons.Default.PlayArrow
        ),
        TourStep(
            title = "Timeline Par Control",
            description = "Neeche compact timeline hai. Do ungliyon se pinch karke zoom karein aur red playhead ko drag karke kisi bhi frame par jayein.",
            icon = Icons.Default.Timeline
        ),
        TourStep(
            title = "One-Tap Motion Presets",
            description = "Bottom toolbar se 'Motion' ya 'Effects' tap karein. Har preset ka live animated thumbnail dikhega - sirf ek tap mein apply ho jayega!",
            icon = Icons.Default.Animation
        ),
        TourStep(
            title = "Simple vs Pro Mode",
            description = "Upar switch se Simple Mode (sliders) ya Pro Mode (keyframes, bezier curves, masks aur 3D camera) mein switch karein.",
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
                .background(StudioCard)
                .border(1.dp, StudioBorder, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Step Indicator badge
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(StudioNeonPurple.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = step.icon,
                        contentDescription = null,
                        tint = StudioCyberCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "Step ${currentStep + 1} of 4",
                    style = MaterialTheme.typography.labelSmall,
                    color = StudioCyberCyan,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = step.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Skip Tour", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            if (currentStep < steps.size - 1) {
                                currentStep++
                            } else {
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioNeonPurple)
                    ) {
                        Text(if (currentStep == steps.size - 1) "Samajh Gaya!" else "Aage (Next)")
                    }
                }
            }
        }
    }
}
