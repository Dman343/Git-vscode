package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.SettingValue
import com.example.data.WorkspaceSettingEntry
import com.example.ui.theme.LocalSemanticCodeColors

@Composable
fun SettingCard(
    entry: WorkspaceSettingEntry,
    onToggleBoolean: (Boolean) -> Unit,
    onResetToDefault: () -> Unit,
    modifier: Modifier = Modifier
) {
    val codeColors = LocalSemanticCodeColors.current
    val boolValue = (entry.value as? SettingValue.BoolValue)?.value
    val borderColor by animateColorAsState(
        targetValue = if (entry.isModifiedFromDefault) {
            codeColors.modifiedBadge.copy(alpha = 0.65f)
        } else {
            MaterialTheme.colorScheme.outline
        },
        animationSpec = spring(),
        label = "setting_card_border"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("setting_card_${entry.shortKey}"),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .then(
                    if (boolValue != null) {
                        Modifier.clickable(
                            role = Role.Switch,
                            onClick = { onToggleBoolean(!boolValue) }
                        )
                    } else {
                        Modifier
                    }
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = entry.key,
                        style = MaterialTheme.typography.labelSmall,
                        color = codeColors.jsonKey,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (entry.isModifiedFromDefault) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onResetToDefault,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(48.dp)
                            .testTag("reset_key_${entry.shortKey}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.RestartAlt,
                            contentDescription = "Reset ${entry.shortKey} to repo default",
                            tint = codeColors.modifiedBadge
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = entry.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                if (boolValue != null) {
                    Switch(
                        checked = boolValue,
                        onCheckedChange = onToggleBoolean,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("switch_${entry.shortKey}"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                            uncheckedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                } else {
                    ValuePill(entry.value)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ValuePill(entry.value)

                val defaultBool = (entry.repoDefaultValue as? SettingValue.BoolValue)?.value
                if (defaultBool != null) {
                    Text(
                        text = if (entry.isModifiedFromDefault) {
                            "Modified (repo default: $defaultBool)"
                        } else {
                            "Repo default: $defaultBool"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (entry.isModifiedFromDefault) {
                            codeColors.modifiedBadge
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                } else {
                    Text(
                        text = "Custom key",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ValuePill(value: SettingValue) {
    val codeColors = LocalSemanticCodeColors.current
    val (label, color) = when (value) {
        is SettingValue.BoolValue -> if (value.value) {
            "true" to codeColors.jsonBooleanTrue
        } else {
            "false" to codeColors.jsonBooleanFalse
        }
        is SettingValue.StringValue -> "\"${value.value}\"" to codeColors.jsonString
        is SettingValue.NumberValue -> value.value.toString() to codeColors.jsonNumber
        is SettingValue.RawJsonValue -> "json" to codeColors.jsonPunctuation
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
