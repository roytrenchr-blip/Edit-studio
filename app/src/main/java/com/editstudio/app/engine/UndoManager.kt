package com.editstudio.app.engine

import com.editstudio.app.domain.Project

class UndoManager(private val maxHistory: Int = 60) {
    private val undoStack = ArrayDeque<Project>()
    private val redoStack = ArrayDeque<Project>()

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun recordSnapshot(currentProject: Project) {
        if (undoStack.lastOrNull() == currentProject) return
        if (undoStack.size >= maxHistory) {
            undoStack.removeFirst()
        }
        undoStack.addLast(currentProject)
        redoStack.clear()
    }

    fun undo(currentProject: Project): Project? {
        if (!canUndo) return null
        val previous = undoStack.removeLast()
        redoStack.addLast(currentProject)
        return previous
    }

    fun redo(currentProject: Project): Project? {
        if (!canRedo) return null
        val next = redoStack.removeLast()
        undoStack.addLast(currentProject)
        return next
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}
