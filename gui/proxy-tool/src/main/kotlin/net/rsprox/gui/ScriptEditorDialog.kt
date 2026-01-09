package net.rsprox.gui

import net.miginfocom.swing.MigLayout
import net.rsprox.proxy.plugin.Script
import java.awt.Dimension
import javax.swing.*

public class ScriptEditorDialog(owner: JFrame, script: Script?) : JDialog(owner, "Script Editor", true) {
    public var result: ScriptResult? = null

    init {
        layout = MigLayout("fill")
        size = Dimension(800, 600)
        setLocationRelativeTo(owner)

        val nameField = JTextField(script?.name ?: "")
        val codeArea = JTextArea(script?.code ?: "")
        codeArea.tabSize = 4

        add(JLabel("Name:"), "split 2")
        add(nameField, "growx, wrap")
        add(JScrollPane(codeArea), "grow, push, wrap")

        val saveButton = JButton("Save")
        saveButton.addActionListener {
            result = ScriptResult(nameField.text, codeArea.text)
            isVisible = false
        }

        val cancelButton = JButton("Cancel")
        cancelButton.addActionListener {
            isVisible = false
        }

        add(saveButton, "split 2, tag ok")
        add(cancelButton, "tag cancel")
    }

    public data class ScriptResult(val name: String, val code: String)
}
