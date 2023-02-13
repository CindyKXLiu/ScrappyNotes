package cs346.shared

import kotlin.test.Test
import kotlin.test.assertEquals

internal class GroupTest {
    @Test
    fun notesFunctions() {
        val group = Group("Group")
        var expectedNotesSize = 0
        assertEquals(expectedNotesSize, group.notes.size)

        // Add a note to the group
        var note = Note("note1", "content1")
        group.notes[note.dateCreated] = note
        expectedNotesSize = 1
        var expectedNoteTitle = "note1"
        var expectedNoteContent = "content1"
        assertEquals(expectedNotesSize, group.notes.size)
        group.notes[note.dateCreated]?.let { assertEquals(expectedNoteTitle, it.title) }
        group.notes[note.dateCreated]?.let { assertEquals(expectedNoteContent, it.content) }

        // Change the content and title of the note added
        note.title = "new title"
        note.content = "new content"
        expectedNoteTitle = "new title"
        expectedNoteContent = "new content"
        assertEquals(expectedNotesSize, group.notes.size)
        group.notes[note.dateCreated]?.let { assertEquals(expectedNoteTitle, it.title) }
        group.notes[note.dateCreated]?.let { assertEquals(expectedNoteContent, it.content) }

        // Delete note from group
        group.notes.remove(note.dateCreated)
        expectedNotesSize = 0
        assertEquals(expectedNotesSize, group.notes.size)
    }

    @Test
    fun getName() {
        val group = Group("Group")
        var expectedGroupName = "Group"
        assertEquals(expectedGroupName, group.name)
    }

    @Test
    fun setName() {
        val group = Group("Group")
        var expectedGroupName = "Group"
        assertEquals(expectedGroupName, group.name)

        group.name = "new name"
        expectedGroupName = "new name"
        assertEquals(expectedGroupName, group.name)
    }
}