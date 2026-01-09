package net.rsprox.gui

import com.formdev.flatlaf.extras.components.FlatButton
import net.miginfocom.swing.MigLayout
import net.rsprox.proxy.ProxyService
import net.rsprox.proxy.plugin.Script
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.PrintWriter
import java.io.StringWriter
import javax.swing.table.AbstractTableModel
import javax.swing.*

public class ScriptsSidePanel(private val proxyService: ProxyService) : JPanel() {
    private val tableModel = ScriptsTableModel()
    private val scriptTable = JTable(tableModel)

    init {
        layout = BorderLayout()

        // Populate list
        refreshList()

        scriptTable.selectionModel.selectionMode = ListSelectionModel.SINGLE_SELECTION
        scriptTable.setShowGrid(false)
        scriptTable.tableHeader.reorderingAllowed = false
        scriptTable.rowHeight = scriptTable.rowHeight.coerceAtLeast(22)
        scriptTable.columnModel.getColumn(0).maxWidth = 60
        scriptTable.columnModel.getColumn(0).minWidth = 60
        scriptTable.columnModel.getColumn(0).preferredWidth = 60

        scriptTable.addMouseListener(
            object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (e.clickCount != 2 || !SwingUtilities.isLeftMouseButton(e)) return
                    val row = scriptTable.rowAtPoint(e.point)
                    val col = scriptTable.columnAtPoint(e.point)
                    if (row < 0 || col != 1) return
                    val script = tableModel.getScriptAt(row) ?: return
                    val dialog = ScriptEditorDialog(SwingUtilities.getWindowAncestor(this@ScriptsSidePanel) as JFrame, script)
                    dialog.isVisible = true
                    val result = dialog.result
                    if (result != null) {
                        try {
                            proxyService.scriptManager.updateScript(script, result.name, result.code)
                        } catch (e: Exception) {
                            showScriptError("Error", e)
                        }
                        refreshList()
                    }
                }
            },
        )

        add(JScrollPane(scriptTable), BorderLayout.CENTER)

        val buttonsPanel = JPanel(MigLayout("fill"))
        val addButton = FlatButton().apply { text = "Add" }
        val removeButton = FlatButton().apply { text = "Remove" }
        val logsButton = FlatButton().apply { text = "Logs" }

        buttonsPanel.add(addButton, "grow")
        buttonsPanel.add(removeButton, "grow, wrap")

        buttonsPanel.add(logsButton, "span, grow")

        add(buttonsPanel, BorderLayout.SOUTH)

        addButton.addActionListener {
            val dialog = ScriptEditorDialog(SwingUtilities.getWindowAncestor(this) as JFrame, null)
            dialog.isVisible = true
            val result = dialog.result
            if (result != null) {
                proxyService.scriptManager.addScript(result.name, result.code)
                refreshList()
            }
        }

        removeButton.addActionListener {
            val selected = tableModel.getScriptAt(scriptTable.selectedRow) ?: return@addActionListener
            if (JOptionPane.showConfirmDialog(this, "Delete script '${selected.name}'?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                proxyService.scriptManager.removeScript(selected)
                refreshList()
            }
        }

        logsButton.addActionListener {
            val owner = SwingUtilities.getWindowAncestor(this) as? JFrame ?: return@addActionListener
            val dialog = ScriptLogsDialog(owner)
            dialog.isVisible = true
        }
    }

    private fun refreshList() {
        tableModel.setScripts(proxyService.scriptManager.scripts.toList())
    }

    private fun showScriptError(title: String, throwable: Throwable) {
        val owner = SwingUtilities.getWindowAncestor(this)

        val sw = StringWriter()
        throwable.printStackTrace(PrintWriter(sw))
        val text = sw.toString().ifBlank { throwable.message ?: throwable.toString() }

        val textArea =
            JTextArea(text).apply {
                isEditable = false
                lineWrap = false
                wrapStyleWord = false
                caretPosition = 0
            }

        val scrollPane = JScrollPane(textArea).apply {
            preferredSize = Dimension(800, 400)
        }

        val copyButton = JButton("Copy Error")
        val closeButton = JButton("Close")

        val buttons = JPanel(MigLayout("ins 0, fillx", "push[]10[]"))
        buttons.add(copyButton, "right")
        buttons.add(closeButton, "right")

        val panel = JPanel(BorderLayout(0, 10)).apply {
            add(scrollPane, BorderLayout.CENTER)
            add(buttons, BorderLayout.SOUTH)
        }

        val dialog = JDialog(owner as? JFrame, title, true).apply {
            contentPane.add(panel)
            pack()
            setLocationRelativeTo(this@ScriptsSidePanel)
        }

        copyButton.addActionListener {
            Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
        }
        closeButton.addActionListener { dialog.isVisible = false }

        dialog.isVisible = true
    }

    private inner class ScriptsTableModel : AbstractTableModel() {
        private var scripts: List<Script> = emptyList()

        fun setScripts(scripts: List<Script>) {
            this.scripts = scripts
            fireTableDataChanged()
        }

        fun getScriptAt(row: Int): Script? = scripts.getOrNull(row)

        override fun getRowCount(): Int = scripts.size

        override fun getColumnCount(): Int = 2

        override fun getColumnName(column: Int): String =
            when (column) {
                0 -> "Enabled"
                1 -> "Name"
                else -> ""
            }

        override fun getColumnClass(columnIndex: Int): Class<*> =
            when (columnIndex) {
                0 -> java.lang.Boolean::class.java
                1 -> String::class.java
                else -> Any::class.java
            }

        override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = columnIndex == 0

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            val script = scripts[rowIndex]
            return when (columnIndex) {
                0 -> script.enabled
                1 -> script.name
                else -> ""
            }
        }

        override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
            if (columnIndex != 0) return
            val script = scripts.getOrNull(rowIndex) ?: return
            val newEnabled = aValue as? Boolean ?: return
            if (script.enabled == newEnabled) return

            try {
                if (newEnabled) {
                    proxyService.scriptManager.enableScript(script)
                } else {
                    proxyService.scriptManager.disableScript(script)
                }
            } catch (e: Exception) {
                showScriptError("Error", e)
            }

            // Refresh to reflect actual enabled state (in case it failed).
            refreshList()
        }
    }
}
