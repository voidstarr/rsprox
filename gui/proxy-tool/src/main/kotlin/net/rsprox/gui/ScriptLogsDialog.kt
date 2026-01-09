package net.rsprox.gui

import net.miginfocom.swing.MigLayout
import net.rsprox.proxy.plugin.ScriptLogBus
import net.rsprox.proxy.plugin.ScriptLogEntry
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.SwingUtilities

public class ScriptLogsDialog(owner: JFrame) : JDialog(owner, "Script Logs", false) {
    private val textArea: JTextArea =
        JTextArea().apply {
            isEditable = false
            lineWrap = false
            wrapStyleWord = false
        }

    private val listener: (ScriptLogEntry) -> Unit = { entry ->
        SwingUtilities.invokeLater {
            appendEntry(entry)
        }
    }

    init {
        layout = BorderLayout(0, 10)

        val scrollPane = JScrollPane(textArea).apply {
            preferredSize = Dimension(900, 500)
        }

        val copyAllButton = JButton("Copy")
        val closeButton = JButton("Close")

        val buttons = JPanel(MigLayout("ins 0, fillx", "push[]10[]"))
        buttons.add(copyAllButton, "right")
        buttons.add(closeButton, "right")

        add(scrollPane, BorderLayout.CENTER)
        add(buttons, BorderLayout.SOUTH)

        copyAllButton.addActionListener {
            val text = textArea.text
            Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
        }
        closeButton.addActionListener { isVisible = false }

        addWindowListener(
            object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent) {
                    isVisible = false
                }

                override fun windowClosed(e: WindowEvent) {
                    ScriptLogBus.removeListener(listener)
                }
            },
        )

        // Initial fill
        val initial = ScriptLogBus.snapshot()
        for (entry in initial) {
            appendEntry(entry)
        }

        ScriptLogBus.addListener(listener)

        pack()
        setLocationRelativeTo(owner)
    }

    private fun appendEntry(entry: ScriptLogEntry) {
        if (textArea.text.isNotEmpty()) {
            textArea.append("\n\n")
        }
        textArea.append(ScriptLogBus.format(entry))
        textArea.caretPosition = textArea.document.length
    }

    override fun setVisible(b: Boolean) {
        super.setVisible(b)
        if (!b) {
            ScriptLogBus.removeListener(listener)
        } else {
            ScriptLogBus.addListener(listener)
        }
    }
}
