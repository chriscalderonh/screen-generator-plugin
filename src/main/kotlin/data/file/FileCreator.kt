package data.file

import data.repository.SettingsRepository
import data.repository.SourceRootRepository
import model.*
import javax.inject.Inject

private const val LAYOUT_DIRECTORY = "layout"
private const val NAV_DIRECTORY = "navigation"

interface FileCreator {

    fun createScreenFiles(
        packageName: String,
        screenName: String,
        androidComponent: AndroidComponent,
        module: Module,
        category: Category,
        customVariablesMap: Map<CustomVariable, String>
    )
}

class FileCreatorImpl @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val sourceRootRepository: SourceRootRepository
) : FileCreator {

    override fun createScreenFiles(
        packageName: String,
        screenName: String,
        androidComponent: AndroidComponent,
        module: Module,
        category: Category,
        customVariablesMap: Map<CustomVariable, String>
    ) {
        settingsRepository.loadScreenElements(category.id).apply {
            filter { it.relatedAndroidComponent == AndroidComponent.NONE || it.relatedAndroidComponent == androidComponent }
                .forEach {
                    val file = File(
                        it.fileName(screenName, packageName, androidComponent.displayName, customVariablesMap),
                        it.body(screenName, packageName, androidComponent.displayName, customVariablesMap, module.nameWithoutPrefix),
                        it.fileType
                    )
                    if (it.fileType == FileType.LAYOUT_XML) {
                        val resourcesSubdirectory = findResourcesSubdirectory(module)
                        if (resourcesSubdirectory != null) {
                            addFile(resourcesSubdirectory, file, it.subdirectory)
                        }
                    } else if (it.fileType == FileType.NAVIGATION_XML) {
                        val navSubdirectory = findNavigationSubdirectory(module)
                        if (navSubdirectory != null) {
                            addFile(navSubdirectory, file, it.subdirectory)
                        }
                    } else {
                        val codeSubdirectory = findCodeSubdirectory(packageName, module, it.sourceSet)
                        if (codeSubdirectory != null) {
                            addFile(codeSubdirectory, file, it.subdirectory(screenName, packageName, androidComponent.displayName))
                        }
                    }
                }

        }
    }

    private fun addFile(directory: Directory, file: File, subdirectory: String) {
        if (subdirectory.isNotEmpty()) {
            var newSubdirectory = directory
            subdirectory.split("/").forEach { segment ->
                newSubdirectory = newSubdirectory.findSubdirectory(segment) ?: newSubdirectory.createSubdirectory(segment)
            }
            newSubdirectory.addFile(file)
        } else {
            directory.addFile(file)
        }
    }

    private fun findCodeSubdirectory(packageName: String, module: Module, sourceSet: String): Directory? =
        sourceRootRepository.findCodeSourceRoot(module, sourceSet)?.run {
            var subdirectory = directory
            packageName.split(".").forEach {
                subdirectory = subdirectory.findSubdirectory(it) ?: subdirectory.createSubdirectory(it)
            }
            return subdirectory
        }

    private fun findResourcesSubdirectory(module: Module) =
        sourceRootRepository.findResourcesSourceRoot(module)?.directory?.run {
            findSubdirectory(LAYOUT_DIRECTORY) ?: createSubdirectory(LAYOUT_DIRECTORY)
        }

    private fun findNavigationSubdirectory(module: Module) =
        sourceRootRepository.findResourcesSourceRoot(module)?.directory?.run {
            findSubdirectory(NAV_DIRECTORY) ?: createSubdirectory(NAV_DIRECTORY)
        }
}
