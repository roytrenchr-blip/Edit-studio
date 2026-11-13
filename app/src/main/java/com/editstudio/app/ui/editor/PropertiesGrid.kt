package com.editstudio.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BorderStyle
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ShapeLine
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.editstudio.app.domain.AnimProperty
import com.editstudio.app.domain.BlendMode
import com.editstudio.app.domain.EasingType
import com.editstudio.app.domain.Layer
import com.editstudio.app.domain.LayerType
import com.editstudio.app.domain.ShapeContent
import com.editstudio.app.domain.StaggerConfig
import com.editstudio.app.domain.StaggerMode
import com.editstudio.app.domain.TextContent
import com.editstudio.app.engine.Keyframes
import com.editstudio.app.ui.components.AppPrimaryButton
import com.editstudio.app.ui.components.ColorPickerRow
import com.editstudio.app.ui.components.SliderRow
import com.editstudio.app.ui.theme.NavyAccentTeal
import com.editstudio.app.ui.theme.NavyAmber
import com.editstudio.app.ui.theme.NavyBg
import com.editstudio.app.ui.theme.NavyBorder
import com.editstudio.app.ui.theme.NavyCard
import com.editstudio.app.ui.theme.NavyCoral
import com.editstudio.app.ui.theme.NavyPrimary
import com.editstudio.app.ui.theme.NavyPrimaryDeep
import com.editstudio.app.ui.theme.NavySurface
import com.editstudio.app.ui.theme.NavyTextPrimary
import com.editstudio.app.ui.theme.NavyTextSecondary

data class PropertyGridItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val accent: Color,
    val onClick: () -> Unit
)

@Composable
fun PropertiesGrid(
    layer: Layer,
    isSimpleMode: Boolean,
    onSplit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onOpenSubSheet: (PropertySubSheet) -> Unit,
    onOpenEffects: () -> Unit,
    onOpenMotion: () -> Unit,
    onOpenKeyframesSheet: () -> Unit,
    onOpenSpeedRampSheet: () -> Unit,
    onOpenLayersSheet: () -> Unit,
    onOpenAudioSheet: () -> Unit,
    onResumeDrawing: () -> Unit,
    onUpdateLayer: (Layer) -> Unit,
    modifier: Modifier = Modifier
) {
    var moreMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavySurface)
    ) {
        // TOOL ROW: Speed, Split/Trim, Duplicate, Volume, Keyframe, More
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyBg)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolRowChip(Icons.Default.Speed, "Speed", NavyPrimary) { onOpenSpeedRampSheet() }
            ToolRowChip(Icons.Default.ContentCut, "Split", NavyAccentTeal) { onSplit() }
            ToolRowChip(Icons.Default.ContentCopy, "Duplicate", NavyPrimary) { onDuplicate() }
            ToolRowChip(Icons.Default.VolumeUp, "Volume", NavyAccentTeal) { onOpenSubSheet(PropertySubSheet.AUDIO) }
            if (!isSimpleMode) {
                ToolRowChip(Icons.Default.Diamond, "Keyframe", NavyAmber) { onOpenKeyframesSheet() }
            }
            Box {
                ToolRowChip(Icons.Default.MoreHoriz, "More", NavyTextPrimary) { moreMenuExpanded = true }
                DropdownMenu(
                    expanded = moreMenuExpanded,
                    onDismissRequest = { moreMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (layer.isVisible) "Hide Layer" else "Show Layer") },
                        leadingIcon = { Icon(if (layer.isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) },
                        onClick = {
                            onUpdateLayer(layer.copy(isVisible = !layer.isVisible))
                            moreMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(if (layer.isLocked) "Unlock Layer" else "Lock Layer") },
                        leadingIcon = { Icon(if (layer.isLocked) Icons.Default.LockOpen else Icons.Default.Lock, null) },
                        onClick = {
                            onUpdateLayer(layer.copy(isLocked = !layer.isLocked))
                            moreMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Layers & Parenting") },
                        leadingIcon = { Icon(Icons.Default.Layers, null) },
                        onClick = {
                            onOpenLayersSheet()
                            moreMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Layer", color = NavyCoral) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = NavyCoral) },
                        onClick = {
                            onDelete()
                            moreMenuExpanded = false
                        }
                    )
                }
            }
        }

        // PROPERTIES GRID of big touch-friendly buttons
        val itemsList = buildList {
            add(PropertyGridItem("color", "Color & Fill", Icons.Default.Palette, NavyPrimary) {
                onOpenSubSheet(PropertySubSheet.COLOR_FILL)
            })
            add(PropertyGridItem("border", "Border & Shadow", Icons.Default.BorderStyle, NavyAccentTeal) {
                onOpenSubSheet(PropertySubSheet.BORDER_SHADOW)
            })
            add(PropertyGridItem("blend", "Blending & Opacity", Icons.Default.Opacity, NavyPrimary) {
                onOpenSubSheet(PropertySubSheet.BLENDING_OPACITY)
            })
            add(PropertyGridItem("transform", "Move & Transform", Icons.Default.OpenWith, NavyAccentTeal) {
                onOpenSubSheet(PropertySubSheet.MOVE_TRANSFORM)
            })
            if (layer.type == LayerType.SHAPE) {
                add(PropertyGridItem("shape", "Edit Shape", Icons.Default.ShapeLine, NavyAmber) {
                    onOpenSubSheet(PropertySubSheet.EDIT_SHAPE)
                })
            }
            if (layer.type == LayerType.TEXT) {
                add(PropertyGridItem("text", "Edit Text", Icons.Default.TextFields, NavyAmber) {
                    onOpenSubSheet(PropertySubSheet.EDIT_TEXT)
                })
            }
            if (layer.type == LayerType.DRAWING) {
                add(PropertyGridItem("draw", "Draw on Canvas", Icons.Default.Brush, NavyAmber) {
                    onResumeDrawing()
                })
            }
            add(PropertyGridItem("effects", "Effects", Icons.Default.AutoAwesome, NavyPrimary) {
                onOpenEffects()
            })
            add(PropertyGridItem("motion", "Motion", Icons.Default.Animation, NavyAccentTeal) {
                onOpenMotion()
            })
            add(PropertyGridItem("speed", "Speed", Icons.Default.Speed, NavyPrimary) {
                onOpenSubSheet(PropertySubSheet.SPEED)
            })
            if (layer.type == LayerType.VIDEO || layer.type == LayerType.AUDIO) {
                add(PropertyGridItem("audio", "Audio", Icons.Default.GraphicEq, NavyAccentTeal) {
                    onOpenAudioSheet()
                })
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(itemsList, key = { it.id }) { btn ->
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(NavyCard)
                        .border(1.dp, NavyBorder, RoundedCornerShape(12.dp))
                        .clickable { btn.onClick() }
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                        .testTag("prop_btn_${btn.id}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = btn.icon,
                        contentDescription = btn.label,
                        tint = btn.accent,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = btn.label,
                        color = NavyTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolRowChip(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(NavyCard)
            .border(1.dp, NavyBorder, RoundedCornerShape(9.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("tool_chip_${label.lowercase()}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(15.dp))
        Text(text = label, color = NavyTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun PropertySubSheetContent(
    subSheet: PropertySubSheet,
    layer: Layer,
    currentTimeMs: Long,
    isSimpleMode: Boolean,
    activeEffectId: String?,
    activeMotionPresetIndex: Int?,
    onUpdateLayer: (Layer) -> Unit,
    onToggleKeyframe: (AnimProperty, Float) -> Unit,
    onOpenSpeedRampFull: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localTimeMs = (currentTimeMs - layer.startTimeMs).coerceAtLeast(0L)
    val showKf = !isSimpleMode

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavySurface)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // Header with Back button to PropertiesGrid
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Peeche", tint = NavyPrimary)
                }
                Text(
                    text = "${layer.name} • ${subSheet.title}",
                    color = NavyTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (!isSimpleMode) {
                Text("◆ = Keyframe", color = NavyAmber, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 12.dp)
        ) {
            when (subSheet) {
                PropertySubSheet.COLOR_FILL -> {
                    when (layer.type) {
                        LayerType.SHAPE -> {
                            val sc = layer.shapeContent ?: ShapeContent()
                            ColorPickerRow(
                                selectedColor = sc.fillColor,
                                onColorSelected = { onUpdateLayer(layer.copy(shapeContent = sc.copy(fillColor = it))) },
                                label = "Shape Fill Color"
                            )
                        }
                        LayerType.TEXT -> {
                            val tc = layer.textContent ?: TextContent()
                            ColorPickerRow(
                                selectedColor = tc.textColor,
                                onColorSelected = { onUpdateLayer(layer.copy(textContent = tc.copy(textColor = it))) },
                                label = "Text Color"
                            )
                            ColorPickerRow(
                                selectedColor = tc.backgroundColor,
                                onColorSelected = { onUpdateLayer(layer.copy(textContent = tc.copy(backgroundColor = it))) },
                                label = "Text Box Background"
                            )
                        }
                        LayerType.DRAWING -> {
                            val dc = layer.drawingContent ?: com.editstudio.app.domain.DrawingContent()
                            ColorPickerRow(
                                selectedColor = dc.activeColor,
                                onColorSelected = { onUpdateLayer(layer.copy(drawingContent = dc.copy(activeColor = it))) },
                                label = "Brush Color"
                            )
                            SliderRow(
                                label = "Brush Width",
                                value = dc.activeWidth,
                                onValueChange = { onUpdateLayer(layer.copy(drawingContent = dc.copy(activeWidth = it))) },
                                valueRange = 2f..36f,
                                formatPattern = "%.0f px"
                            )
                        }
                        else -> {
                            ColorPickerRow(
                                selectedColor = layer.solidColor,
                                onColorSelected = { onUpdateLayer(layer.copy(solidColor = it)) },
                                label = "Solid / Tint Color"
                            )
                        }
                    }
                }

                PropertySubSheet.BORDER_SHADOW -> {
                    val borderVal = Keyframes.evaluateProperty(layer, AnimProperty.BORDER_WIDTH, localTimeMs, layer.borderWidth)
                    val shadowVal = Keyframes.evaluateProperty(layer, AnimProperty.SHADOW_RADIUS, localTimeMs, layer.shadowRadius)
                    SliderRow(
                        label = "Border Width",
                        value = borderVal,
                        onValueChange = { onUpdateLayer(layer.copy(borderWidth = it)) },
                        valueRange = 0f..24f,
                        formatPattern = "%.1f px",
                        showKeyframeButton = showKf,
                        hasKeyframeAtPlayhead = Keyframes.hasKeyframeNear(layer, AnimProperty.BORDER_WIDTH, localTimeMs),
                        onToggleKeyframe = { onToggleKeyframe(AnimProperty.BORDER_WIDTH, borderVal) }
                    )
                    ColorPickerRow(
                        selectedColor = layer.borderColor,
                        onColorSelected = { onUpdateLayer(layer.copy(borderColor = it)) },
                        label = "Border Color"
                    )
                    SliderRow(
                        label = "Shadow Size",
                        value = shadowVal,
                        onValueChange = { onUpdateLayer(layer.copy(shadowRadius = it)) },
                        valueRange = 0f..40f,
                        formatPattern = "%.0f px",
                        showKeyframeButton = showKf,
                        hasKeyframeAtPlayhead = Keyframes.hasKeyframeNear(layer, AnimProperty.SHADOW_RADIUS, localTimeMs),
                        onToggleKeyframe = { onToggleKeyframe(AnimProperty.SHADOW_RADIUS, shadowVal) }
                    )
                }

                PropertySubSheet.BLENDING_OPACITY -> {
                    val op = Keyframes.evaluateProperty(layer, AnimProperty.OPACITY, localTimeMs, layer.transform.opacity)
                    SliderRow(
                        label = "Opacity",
                        value = op,
                        onValueChange = { onUpdateLayer(layer.copy(transform = layer.transform.copy(opacity = it))) },
                        valueRange = 0f..1f,
                        formatPattern = "%.0f%%",
                        showKeyframeButton = showKf,
                        hasKeyframeAtPlayhead = Keyframes.hasKeyframeNear(layer, AnimProperty.OPACITY, localTimeMs),
                        onToggleKeyframe = { onToggleKeyframe(AnimProperty.OPACITY, op) }
                    )
                    Text("Blend Mode", color = NavyTextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        BlendMode.entries.forEach { mode ->
                            FilterChip(
                                selected = layer.blendMode == mode,
                                onClick = { onUpdateLayer(layer.copy(blendMode = mode)) },
                                label = { Text(mode.displayName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = NavyPrimaryDeep)
                            )
                        }
                    }
                }

                PropertySubSheet.MOVE_TRANSFORM -> {
                    val t = layer.transform
                    val px = Keyframes.evaluateProperty(layer, AnimProperty.POSITION_X, localTimeMs, t.x)
                    val py = Keyframes.evaluateProperty(layer, AnimProperty.POSITION_Y, localTimeMs, t.y)
                    val sx = Keyframes.evaluateProperty(layer, AnimProperty.SCALE_X, localTimeMs, t.scaleX)
                    val rot = Keyframes.evaluateProperty(layer, AnimProperty.ROTATION, localTimeMs, t.rotationZ)

                    SliderRow(
                        label = "Position X",
                        value = px,
                        onValueChange = { onUpdateLayer(layer.copy(transform = t.copy(x = it))) },
                        valueRange = -540f..540f,
                        formatPattern = "%.0f px",
                        showKeyframeButton = showKf,
                        hasKeyframeAtPlayhead = Keyframes.hasKeyframeNear(layer, AnimProperty.POSITION_X, localTimeMs),
                        onToggleKeyframe = { onToggleKeyframe(AnimProperty.POSITION_X, px) }
                    )
                    SliderRow(
                        label = "Position Y",
                        value = py,
                        onValueChange = { onUpdateLayer(layer.copy(transform = t.copy(y = it))) },
                        valueRange = -960f..960f,
                        formatPattern = "%.0f px",
                        showKeyframeButton = showKf,
                        hasKeyframeAtPlayhead = Keyframes.hasKeyframeNear(layer, AnimProperty.POSITION_Y, localTimeMs),
                        onToggleKeyframe = { onToggleKeyframe(AnimProperty.POSITION_Y, py) }
                    )
                    SliderRow(
                        label = "Scale",
                        value = sx,
                        onValueChange = { onUpdateLayer(layer.copy(transform = t.copy(scaleX = it, scaleY = it))) },
                        valueRange = 0.1f..4.0f,
                        formatPattern = "%.2fx",
                        showKeyframeButton = showKf,
                        hasKeyframeAtPlayhead = Keyframes.hasKeyframeNear(layer, AnimProperty.SCALE_X, localTimeMs),
                        onToggleKeyframe = { onToggleKeyframe(AnimProperty.SCALE_X, sx) }
                    )
                    SliderRow(
                        label = "Rotation",
                        value = rot,
                        onValueChange = { onUpdateLayer(layer.copy(transform = t.copy(rotationZ = it))) },
                        valueRange = -180f..180f,
                        formatPattern = "%.0f°",
                        showKeyframeButton = showKf,
                        hasKeyframeAtPlayhead = Keyframes.hasKeyframeNear(layer, AnimProperty.ROTATION, localTimeMs),
                        onToggleKeyframe = { onToggleKeyframe(AnimProperty.ROTATION, rot) }
                    )
                }

                PropertySubSheet.EDIT_SHAPE -> {
                    val sc = layer.shapeContent ?: ShapeContent()
                    SliderRow(
                        label = "Corner Radius",
                        value = sc.cornerRadius,
                        onValueChange = { onUpdateLayer(layer.copy(shapeContent = sc.copy(cornerRadius = it))) },
                        valueRange = 0f..64f,
                        formatPattern = "%.0f dp"
                    )
                    SliderRow(
                        label = "Stroke Width",
                        value = sc.strokeWidth,
                        onValueChange = { onUpdateLayer(layer.copy(shapeContent = sc.copy(strokeWidth = it))) },
                        valueRange = 0f..24f,
                        formatPattern = "%.1f px"
                    )
                    SliderRow(
                        label = "Polygon Sides",
                        value = sc.sides.toFloat(),
                        onValueChange = { onUpdateLayer(layer.copy(shapeContent = sc.copy(sides = it.toInt()))) },
                        valueRange = 5f..12f,
                        formatPattern = "%.0f"
                    )
                }

                PropertySubSheet.EDIT_TEXT -> {
                    val tc = layer.textContent ?: TextContent()
                    OutlinedTextField(
                        value = tc.text,
                        onValueChange = { onUpdateLayer(layer.copy(textContent = tc.copy(text = it))) },
                        label = { Text("Text Likhein") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NavyPrimary,
                            unfocusedBorderColor = NavyBorder
                        )
                    )
                    SliderRow(
                        label = "Font Size",
                        value = tc.fontSizeSp,
                        onValueChange = { onUpdateLayer(layer.copy(textContent = tc.copy(fontSizeSp = it))) },
                        valueRange = 14f..90f,
                        formatPattern = "%.0f sp"
                    )
                    SliderRow(
                        label = "Outline Width",
                        value = tc.outlineWidth,
                        onValueChange = { onUpdateLayer(layer.copy(textContent = tc.copy(outlineWidth = it))) },
                        valueRange = 0f..14f,
                        formatPattern = "%.1f px"
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = layer.stagger.isEnabled,
                            onClick = {
                                onUpdateLayer(layer.copy(stagger = StaggerConfig(isEnabled = !layer.stagger.isEnabled, mode = StaggerMode.PER_LETTER)))
                            },
                            label = { Text("Typewriter Effect") }
                        )
                    }
                }

                PropertySubSheet.EFFECT_SLIDERS -> {
                    val effect = layer.effects.firstOrNull { it.id == activeEffectId } ?: layer.effects.lastOrNull()
                    if (effect != null) {
                        Text(effect.type.displayName, color = NavyPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        SliderRow(
                            label = "Effect Intensity",
                            value = effect.intensity,
                            onValueChange = { newVal ->
                                val updated = layer.effects.map { if (it.id == effect.id) it.copy(intensity = newVal) else it }
                                onUpdateLayer(layer.copy(effects = updated))
                            },
                            valueRange = 0f..1f,
                            formatPattern = "%.0f%%",
                            showKeyframeButton = showKf,
                            hasKeyframeAtPlayhead = Keyframes.hasKeyframeNear(layer, AnimProperty.GLOW, localTimeMs),
                            onToggleKeyframe = { onToggleKeyframe(AnimProperty.GLOW, effect.intensity) }
                        )
                    } else {
                        Text("Koi effect select nahi hai", color = NavyTextSecondary)
                    }
                }

                PropertySubSheet.MOTION_SLIDERS -> {
                    val idx = activeMotionPresetIndex ?: layer.motionPresets.lastIndex
                    val preset = layer.motionPresets.getOrNull(idx)
                    if (preset != null) {
                        Text(preset.presetType.title, color = NavyAccentTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        SliderRow(
                            label = "Duration",
                            value = preset.durationMs.toFloat(),
                            onValueChange = { ms ->
                                val updated = layer.motionPresets.mapIndexed { i, p -> if (i == idx) p.copy(durationMs = ms.toLong()) else p }
                                onUpdateLayer(layer.copy(motionPresets = updated))
                            },
                            valueRange = 150f..4000f,
                            formatPattern = "%.0f ms"
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            EasingType.entries.forEach { e ->
                                FilterChip(
                                    selected = preset.easing == e,
                                    onClick = {
                                        val updated = layer.motionPresets.mapIndexed { i, p -> if (i == idx) p.copy(easing = e) else p }
                                        onUpdateLayer(layer.copy(motionPresets = updated))
                                    },
                                    label = { Text(e.label, fontSize = 11.sp) }
                                )
                            }
                        }
                    } else {
                        Text("Koi motion preset apply nahi hai", color = NavyTextSecondary)
                    }
                }

                PropertySubSheet.SPEED -> {
                    val curSpeed = layer.speedRamp.points.firstOrNull()?.speedMultiplier ?: 1f
                    SliderRow(
                        label = "Playback Speed",
                        value = curSpeed,
                        onValueChange = { s ->
                            val pts = listOf(com.editstudio.app.domain.SpeedPoint(0f, s), com.editstudio.app.domain.SpeedPoint(1f, s))
                            onUpdateLayer(layer.copy(speedRamp = layer.speedRamp.copy(isEnabled = true, points = pts)))
                        },
                        valueRange = 0.1f..10f,
                        formatPattern = "%.1fx"
                    )
                    AppPrimaryButton(
                        text = "Speed Ramp Curve Kholein",
                        onClick = onOpenSpeedRampFull,
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                    )
                }

                PropertySubSheet.AUDIO -> {
                    SliderRow(
                        label = "Layer Volume",
                        value = layer.volume,
                        onValueChange = { onUpdateLayer(layer.copy(volume = it)) },
                        valueRange = 0f..2f,
                        formatPattern = "%.0f%%"
                    )
                }

                else -> {}
            }
        }
    }
}
