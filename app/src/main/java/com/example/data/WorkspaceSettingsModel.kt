package com.example.data

sealed interface SettingValue {
    data class BoolValue(val value: Boolean) : SettingValue
    data class StringValue(val value: String) : SettingValue
    data class NumberValue(val value: Number) : SettingValue
    data class RawJsonValue(val serialized: String) : SettingValue
}

data class WorkspaceSettingEntry(
    val key: String,
    val title: String,
    val description: String,
    val value: SettingValue,
    val repoDefaultValue: SettingValue?,
    val isCoreRepoSetting: Boolean
) {
    val isModifiedFromDefault: Boolean
        get() = repoDefaultValue != null && value != repoDefaultValue

    val shortKey: String
        get() = key.substringAfterLast('.')
}

object ImportedRepoDefaults {
    const val REPO_NAME = "Dman343/Git-vscode"
    const val FILE_PATH = ".vscode/settings.json"
    const val EXTENSION_NAMESPACE = "dotnetAcquisitionExtension"

    val INITIAL_JSON = """
        {
            "dotnetAcquisitionExtension.enablePreviewFeatures": true,
            "dotnetAcquisitionExtension.highVerbosity": true,
            "dotnetAcquisitionExtension.suppressOutput": false
        }
    """.trimIndent()

    data class CoreSchema(
        val key: String,
        val title: String,
        val description: String,
        val defaultValue: Boolean
    )

    val coreSchemas = listOf(
        CoreSchema(
            key = "dotnetAcquisitionExtension.enablePreviewFeatures",
            title = "Enable Preview Features",
            description = "Opt into experimental and preview capabilities in the .NET Acquisition Extension.",
            defaultValue = true
        ),
        CoreSchema(
            key = "dotnetAcquisitionExtension.highVerbosity",
            title = "High Verbosity Logging",
            description = "Emit detailed diagnostic telemetry and resolution logs when acquiring .NET runtimes.",
            defaultValue = true
        ),
        CoreSchema(
            key = "dotnetAcquisitionExtension.suppressOutput",
            title = "Suppress Output",
            description = "Silence automatic output channel notifications and status logs from the extension.",
            defaultValue = false
        )
    )

    val coreSchemaByKey: Map<String, CoreSchema> = coreSchemas.associateBy { it.key }
}
