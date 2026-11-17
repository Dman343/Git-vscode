package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CodeBlockTextStyle
import com.example.ui.theme.LocalSemanticCodeColors
import com.example.ui.theme.SemanticCodeColors

@Composable
fun JsonInspectorPanel(
    formattedJson: String,
    isEditing: Boolean,
    draftJson: String,
    jsonError: String?,
    onCopyJson: () -> Unit,
    onStartEdit: () -> Unit,
    onDraftChange: (String) -> Unit,
    onApplyEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val codeColors = LocalSemanticCodeColors.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("json_inspector_panel"),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ".vscode/settings.json",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isEditing) "Raw JSON Editor (4-space indent)" else "Live synchronized output",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!isEditing) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(
                            onClick = onCopyJson,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("copy_json_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = "Copy JSON",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy")
                        }
                        OutlinedButton(
                            onClick = onStartEdit,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("edit_json_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit Raw JSON",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (jsonError != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("json_error_banner"),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ErrorOutline,
                            contentDescription = "JSON syntax error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = jsonError,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(14.dp)
            ) {
                if (isEditing) {
                    BasicTextField(
                        value = draftJson,
                        onValueChange = onDraftChange,
                        textStyle = CodeBlockTextStyle.copy(
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 160.dp)
                            .testTag("raw_json_editor_input")
                    )
                } else {
                    val lines = remember(formattedJson) { formattedJson.lines() }
                    val highlighted = remember(formattedJson, codeColors) {
                        highlightJson(formattedJson, codeColors)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Column(
                            modifier = Modifier.padding(end = 14.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            lines.forEachIndexed { index, _ ->
                                Text(
                                    text = "${index + 1}",
                                    style = CodeBlockTextStyle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                                )
                            }
                        }
                        Text(
                            text = highlighted,
                            style = CodeBlockTextStyle,
                            modifier = Modifier.testTag("json_preview_text")
                        )
                    }
                }
            }

            if (isEditing) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onCancelEdit,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("cancel_json_edit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Cancel JSON edit",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onApplyEdit,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("apply_json_edit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Apply JSON changes",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apply JSON")
                    }
                }
            }
        }
    }
}

private fun highlightJson(json: String, colors: SemanticCodeColors): AnnotatedString {
    return buildAnnotatedString {
        val lines = json.lines()
        lines.forEachIndexed { lineIndex, line ->
            val colonIdx = line.indexOf(':')
            if (colonIdx != -1 && line.trimStart().startsWith("\"")) {
                val indent = line.takeWhile { it == ' ' || it == '\t' }
                val keyPart = line.substring(indent.length, colonIdx)
                val rest = line.substring(colonIdx + 1)

                append(indent)
                withStyle(SpanStyle(color = colors.jsonKey)) {
                    append(keyPart)
                }
                withStyle(SpanStyle(color = colors.jsonPunctuation)) {
                    append(":")
                }

                val trimmedRest = rest.trimStart()
                val leadingSpace = rest.takeWhile { it == ' ' }
                append(leadingSpace)

                val hasTrailingComma = trimmedRest.endsWith(",")
                val token = if (hasTrailingComma) trimmedRest.dropLast(1) else trimmedRest

                val valueColor = when {
                    token == "true" -> colors.jsonBooleanTrue
                    token == "false" -> colors.jsonBooleanFalse
                    token.startsWith("\"") -> colors.jsonString
                    token.toDoubleOrNull() != null -> colors.jsonNumber
                    else -> colors.jsonPunctuation
                }
                withStyle(SpanStyle(color = valueColor)) {
                    append(token)
                }
                if (hasTrailingComma) {
                    withStyle(SpanStyle(color = colors.jsonPunctuation)) {
                        append(",")
                    }
                }
            } else {
                withStyle(SpanStyle(color = colors.jsonPunctuation)) {
                    append(line)
                }
            }
            if (lineIndex < lines.lastIndex) {
                append("\n")
            }
        }
    }
}
