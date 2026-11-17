package com.example.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File

data class SettingsDocument(
    val entries: List<WorkspaceSettingEntry>,
    val formattedJson: String,
    val isModifiedFromRepo: Boolean
)

class WorkspaceSettingsRepository(context: Context) {

    private val storageFile = File(context.applicationContext.filesDir, "vscode_settings.json")

    fun loadSettings(): SettingsDocument {
        val raw = if (storageFile.exists()) {
            runCatching { storageFile.readText() }.getOrNull()?.takeIf { it.isNotBlank() }
        } else {
            null
        } ?: ImportedRepoDefaults.INITIAL_JSON.also { persistRaw(it) }

        return parseJsonToDocument(raw).getOrElse {
            persistRaw(ImportedRepoDefaults.INITIAL_JSON)
            parseJsonToDocument(ImportedRepoDefaults.INITIAL_JSON).getOrThrow()
        }
    }

    fun updateBooleanSetting(key: String, newValue: Boolean): SettingsDocument {
        val current = loadSettings()
        val orderedMap = linkedMapOf<String, Any>()
        current.entries.forEach { entry ->
            orderedMap[entry.key] = if (entry.key == key) {
                newValue
            } else {
                settingValueToJsonAny(entry.value)
            }
        }
        if (!orderedMap.containsKey(key)) {
            orderedMap[key] = newValue
        }
        val json = formatOrderedMap(orderedMap)
        persistRaw(json)
        return parseJsonToDocument(json).getOrThrow()
    }

    fun resetKeyToDefault(key: String): SettingsDocument {
        val schema = ImportedRepoDefaults.coreSchemaByKey[key] ?: return loadSettings()
        return updateBooleanSetting(key, schema.defaultValue)
    }

    fun resetToRepoDefaults(): SettingsDocument {
        persistRaw(ImportedRepoDefaults.INITIAL_JSON)
        return parseJsonToDocument(ImportedRepoDefaults.INITIAL_JSON).getOrThrow()
    }

    fun applyRawJson(rawInput: String): Result<SettingsDocument> {
        val parsed = parseJsonToDocument(rawInput)
        return parsed.onSuccess { doc ->
            persistRaw(doc.formattedJson)
        }
    }

    private fun persistRaw(content: String) {
        storageFile.writeText(content)
    }

    private fun parseJsonToDocument(rawJson: String): Result<SettingsDocument> {
        return try {
            val trimmed = rawJson.trim()
            if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
                return Result.failure(IllegalArgumentException("settings.json must be a valid JSON object { ... }."))
            }
            val jsonObj = JSONObject(trimmed)
            val entries = mutableListOf<WorkspaceSettingEntry>()
            val orderedMap = linkedMapOf<String, Any>()

            // Keep core schema keys in canonical repo order if present, then append any additional keys
            val seenKeys = mutableSetOf<String>()
            for (schema in ImportedRepoDefaults.coreSchemas) {
                if (jsonObj.has(schema.key)) {
                    val rawVal = jsonObj.get(schema.key)
                    orderedMap[schema.key] = rawVal
                    seenKeys.add(schema.key)
                    entries.add(
                        WorkspaceSettingEntry(
                            key = schema.key,
                            title = schema.title,
                            description = schema.description,
                            value = jsonAnyToSettingValue(rawVal),
                            repoDefaultValue = SettingValue.BoolValue(schema.defaultValue),
                            isCoreRepoSetting = true
                        )
                    )
                }
            }

            val keysIterator = jsonObj.keys()
            while (keysIterator.hasNext()) {
                val key = keysIterator.next()
                if (seenKeys.add(key)) {
                    val rawVal = jsonObj.get(key)
                    orderedMap[key] = rawVal
                    entries.add(
                        WorkspaceSettingEntry(
                            key = key,
                            title = key.substringAfterLast('.').replace(Regex("([A-Z])"), " $1")
                                .replaceFirstChar { it.uppercase() }.trim(),
                            description = "Custom workspace setting in .vscode/settings.json.",
                            value = jsonAnyToSettingValue(rawVal),
                            repoDefaultValue = null,
                            isCoreRepoSetting = false
                        )
                    )
                }
            }

            val formatted = formatOrderedMap(orderedMap)
            val isModified = entries.any { it.isModifiedFromDefault } ||
                entries.size != ImportedRepoDefaults.coreSchemas.size ||
                entries.any { !it.isCoreRepoSetting }

            Result.success(
                SettingsDocument(
                    entries = entries,
                    formattedJson = formatted,
                    isModifiedFromRepo = isModified
                )
            )
        } catch (e: JSONException) {
            Result.failure(IllegalArgumentException(e.message ?: "Invalid JSON syntax"))
        }
    }

    private fun jsonAnyToSettingValue(value: Any): SettingValue {
        return when (value) {
            is Boolean -> SettingValue.BoolValue(value)
            is String -> SettingValue.StringValue(value)
            is Number -> SettingValue.NumberValue(value)
            is JSONObject -> SettingValue.RawJsonValue(value.toString(4))
            is JSONArray -> SettingValue.RawJsonValue(value.toString(4))
            else -> SettingValue.RawJsonValue(value.toString())
        }
    }

    private fun settingValueToJsonAny(value: SettingValue): Any {
        return when (value) {
            is SettingValue.BoolValue -> value.value
            is SettingValue.StringValue -> value.value
            is SettingValue.NumberValue -> value.value
            is SettingValue.RawJsonValue -> value.serialized
        }
    }

    private fun formatOrderedMap(map: LinkedHashMap<String, Any>): String {
        if (map.isEmpty()) return "{\n}"
        val builder = StringBuilder()
        builder.append("{\n")
        val entriesList = map.entries.toList()
        entriesList.forEachIndexed { index, (key, value) ->
            val escapedKey = JSONObject.quote(key)
            val formattedVal = when (value) {
                is Boolean -> value.toString()
                is Number -> value.toString()
                is String -> JSONObject.quote(value)
                is JSONObject -> value.toString(4).replace("\n", "\n    ")
                is JSONArray -> value.toString(4).replace("\n", "\n    ")
                else -> JSONObject.quote(value.toString())
            }
            val comma = if (index < entriesList.lastIndex) "," else ""
            builder.append("    ").append(escapedKey).append(": ").append(formattedVal).append(comma).append("\n")
        }
        builder.append("}")
        return builder.toString()
    }
}
