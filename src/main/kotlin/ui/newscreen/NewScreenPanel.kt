package ui.newscreen

import com.intellij.openapi.ui.ComboBox
import model.AndroidComponent
import model.Category
import model.Module
import ui.settings.widget.constraintsLeft
import ui.settings.widget.constraintsLeftWithSpace
import ui.settings.widget.constraintsRight
import ui.settings.widget.constraintsRightWithSpace
import util.updateText
import java.awt.Dimension
import java.awt.GridBagLayout
import javax.swing.*


class NewScreenPanel : JPanel() {

    val nameTextField = JTextField()
    val packageTextField = JTextField()

    val categoryComboBox = ComboBox<Category>()
    private val descriptionPane = JTextPane()
    val androidComponentComboBox = ComboBox(AndroidComponent.values())
    val moduleComboBox = ComboBox<Module>()
    val customVariablesPanel = CustomVariablesPanel()

    var onCategoryIndexChanged: ((Int) -> Unit)? = null

    private var listenersBlocked = false

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        add(JPanel().apply {
            layout = GridBagLayout()
            add(JLabel("Name:"), constraintsLeftWithSpace(0, 0))
            add(nameTextField, constraintsRightWithSpace(1, 0))
            add(JLabel("Category:"), constraintsLeftWithSpace(0, 1))
            add(categoryComboBox, constraintsRightWithSpace(1, 1))
            add(JLabel(" "), constraintsLeftWithSpace(0, 2))
            add(textPaneProperties(descriptionPane), constraintsRightWithSpace(1, 2))
            categoryComboBox.addActionListener { if (!listenersBlocked) onCategoryIndexChanged?.invoke(categoryComboBox.selectedIndex) }
        })
        add(customVariablesPanel)
        add(JPanel().apply {
            layout = GridBagLayout()
            add(JLabel("Module:"), constraintsLeft(0, 0))
            add(moduleComboBox, constraintsRight(1, 0))
            add(JLabel("Package:"), constraintsLeft(0, 1))
            add(packageTextField, constraintsRight(1, 1))
        })
    }

    override fun getPreferredSize() = Dimension(400, 110)

    fun render(state: NewScreenState) = state.run {
        listenersBlocked = true
        packageTextField.updateText(packageName)

        if (moduleComboBox.itemCount == 0) {
            moduleComboBox.removeAllItems()
            modules.forEach { moduleComboBox.addItem(it) }
        }

        moduleComboBox.selectedItem = selectedModule

        if (categoryComboBox.itemCount == 0) {
            categories.forEach { categoryComboBox.addItem(it) }
        }

        descriptionPane.text = selectedCategory?.description?.replace("\n", "<br>") ?: ""

        customVariablesPanel.render(state)
        listenersBlocked = false
    }

    private fun textPaneProperties(textPane: JTextPane): JTextPane {
        textPane.apply {
            isEditable = false
            cursor = null
            isOpaque = false
            isFocusable = false
            contentType = "text/html"
        }
        return textPane
    }
}
