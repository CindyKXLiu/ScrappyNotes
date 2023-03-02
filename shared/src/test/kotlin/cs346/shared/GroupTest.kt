package cs346.shared

import kotlin.collections.HashMap
import kotlin.test.Test
import kotlin.test.assertEquals

internal class GroupTest {
    @Test
    fun notesFunctions() {
        val group = Group("Group")
        var expectedNotesSize = 0
        assertEquals(expectedNotesSize, group.notes.size)

        // Add a note to the group
        val note = Note("note1", "content1")
        group.notes[note.id] = note
        expectedNotesSize = 1
        var expectedNoteTitle = "note1"
        var expectedNoteContent = "content1"
        assertEquals(expectedNotesSize, group.notes.size)
        group.notes[note.id]?.let { assertEquals(expectedNoteTitle, it.title) }
        group.notes[note.id]?.let { assertEquals(expectedNoteContent, it.content) }

        // Change the content and title of the note added
        note.title = "new title"
        note.content = "new content"
        expectedNoteTitle = "new title"
        expectedNoteContent = "new content"
        assertEquals(expectedNotesSize, group.notes.size)
        group.notes[note.id]?.let { assertEquals(expectedNoteTitle, it.title) }
        group.notes[note.id]?.let { assertEquals(expectedNoteContent, it.content) }

        // Delete note from group
        group.notes.remove(note.id)
        expectedNotesSize = 0
        assertEquals(expectedNotesSize, group.notes.size)
    }

    @Test
    fun getName() {
        val group = Group("Group")
        val expectedGroupName = "Group"
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

    @Test
    fun addNote() {
        val group = Group("Group")
        val note1 = Note("title1", "content1")
        val note2 = Note("title2", "content2")
        val note3 = Note("title3", "content3")

        assertEquals(0, group.notes.size)

        group.addNote(note1)
        assertEquals(1, group.notes.size)

        group.addNote(note2)
        assertEquals(2, group.notes.size)

        group.addNote(note3)
        assertEquals(3, group.notes.size)
    }

    @Test
    fun addNotes() {
        val group = Group("Group")
        val note1 = Note("title1", "content1")
        val note2 = Note("title2", "content2")
        val note3 = Note("title3", "content3")

        assertEquals(0, group.notes.size)

        val notesMap = HashMap<Int, Note>()
        notesMap[note1.id] = note1
        notesMap[note2.id] = note2
        notesMap[note3.id] = note3

        group.addNotes(notesMap)
        assertEquals(3, group.notes.size)
    }

    @Test
    fun removeNote() {
        val group = Group("Group")
        val note1 = Note("title1", "content1")
        val note2 = Note("title2", "content2")
        val note3 = Note("title3", "content3")

        group.addNote(note1)
        group.addNote(note2)
        group.addNote(note3)

        assertEquals(3, group.notes.size)

        group.removeNote(note1.id)
        assertEquals(2, group.notes.size)

        group.removeNote(note2.id)
        assertEquals(1, group.notes.size)

        group.removeNote(note3.id)
        assertEquals(0, group.notes.size)
    }

    @Test
    fun removeNotes() {
        val group = Group("Group")
        val note1 = Note("title1", "content1")
        val note2 = Note("title2", "content2")
        val note3 = Note("title3", "content3")

        val notesMap = HashMap<Int, Note>()
        notesMap[note1.id] = note1
        notesMap[note2.id] = note2
        notesMap[note3.id] = note3

        group.addNotes(notesMap)
        assertEquals(3, group.notes.size)

        val partialNotesMap1 = HashMap<Int, Note>()
        partialNotesMap1[note1.id] = note1
        partialNotesMap1[note2.id] = note2
        group.removeNotes(partialNotesMap1)
        assertEquals(1, group.notes.size)

        val partialNotesMap2 = HashMap<Int, Note>()
        partialNotesMap2[note1.id] = note3
        group.removeNotes(partialNotesMap2)
        assertEquals(0, group.notes.size)
    }
}