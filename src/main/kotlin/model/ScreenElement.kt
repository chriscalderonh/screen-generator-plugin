package model

import util.toSnakeCase
import java.io.Serializable

private const val UNNAMED_ELEMENT = "UnnamedElement"
const val DEFAULT_SOURCE_SET = "main"

data class ScreenElement(
    var name: String = "",
    var template: String = "",
    var fileType: FileType = FileType.KOTLIN,
    var fileNameTemplate: String = "",
    var relatedAndroidComponent: AndroidComponent = AndroidComponent.NONE,
    var categoryId: Int = 0,
    var subdirectory: String = "",
    var sourceSet: String = DEFAULT_SOURCE_SET
) : Serializable {

    override fun toString() = name

    fun body(
        screenName: String,
        packageName: String,
        androidComponent: String,
        customVariablesMap: Map<CustomVariable, String>,
        nameWithoutPrefix: String = ""
    ) =
        template
            .replaceVariables(screenName, packageName, androidComponent, nameWithoutPrefix)
            .replaceCustomVariables(customVariablesMap)

    fun subdirectory(
        screenName: String,
        packageName: String,
        androidComponent: String
    ) =
        subdirectory
            .replaceVariables(screenName, packageName, androidComponent)

    fun fileName(
        screenName: String,
        packageName: String,
        androidComponent: String,
        customVariablesMap: Map<CustomVariable, String>
    ) =
        fileNameTemplate
            .replaceVariables(screenName, packageName, androidComponent)
            .replaceCustomVariables(customVariablesMap)
            .run {
                if ((fileType == FileType.LAYOUT_XML) || (fileType == FileType.NAVIGATION_XML))
                    toLowerCase()
                else
                    this
            }

    private fun String.replaceVariables(
        screenName: String,
        packageName: String,
        androidComponent: String,
        nameWithoutPrefix: String = ""
    ) =
        replace(Variable.NAME.value, screenName)
            .replace(Variable.NAME_SNAKE_CASE.value, screenName.toSnakeCase())
            .replace(Variable.NAME_LOWER_CASE.value, screenName.decapitalize())
            .replace(Variable.NAME_ALL_LOWER_CASE.value, screenName.toLowerCase())
            .replace(Variable.SCREEN_ELEMENT.value, name)
            .replace(Variable.PACKAGE_NAME.value, packageName)
            .replace(Variable.ANDROID_COMPONENT_NAME.value, androidComponent)
            .replace(Variable.ANDROID_COMPONENT_NAME_LOWER_CASE.value, androidComponent.decapitalize())
            .replace(Variable.PATH_TO_BINDING.value, getBindingPath(packageName, nameWithoutPrefix))

    private fun String.replaceCustomVariables(variables: Map<CustomVariable, String>): String {
        var updatedString = this
        variables.forEach { (variable, text) ->
            updatedString = updatedString.replace("%${variable.name}%", text)
            updatedString = updatedString.replace("%${variable.name}_lowercase%", text.toLowerCase())
            updatedString = updatedString.replace("%${variable.name}_decapitalize%", text.decapitalize())
        }
        return updatedString
    }

    private fun getBindingPath(
        packageName: String,
        nameWithoutPrefix: String
    ): String {
        val lastPartPath = nameWithoutPrefix.split(".").last()
        return "${packageName.substringBefore(lastPartPath)}$lastPartPath"
    }

    companion object {
        fun getDefault(categoryId: Int) = ScreenElement(
            UNNAMED_ELEMENT,
            FileType.KOTLIN.defaultTemplate,
            FileType.KOTLIN,
            FileType.KOTLIN.defaultFileName,
            categoryId = categoryId
        )
    }
}
