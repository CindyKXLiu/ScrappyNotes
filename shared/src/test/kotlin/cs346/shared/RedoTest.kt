package cs346.shared

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class RedoTest {
    @Test
    fun createNote() {
        val controller = Controller()
        var expectedNotesSize = 0
        assertEquals(expectedNotesSize, controller.getAllNotes().size)

        controller.createNote()
        expectedNotesSize = 1
        assertEquals(expectedNotesSize, controller.getAllNotes().size)

        controller.undo()
        expectedNotesSize = 0
        assertEquals(expectedNotesSize, controller.getAllNotes().size)

        controller.redo()
        expectedNotesSize = 1
        assertEquals(expectedNotesSize, controller.getAllNotes().size)

        controller.createNote()
        controller.createNote()
        controller.createNote()
        expectedNotesSize = 4
        assertEquals(expectedNotesSize, controller.getAllNotes().size)

        controller.undo()
        expectedNotesSize = 3
        assertEquals(expectedNotesSize, controller.getAllNotes().size)

        controller.redo()
        expectedNotesSize = 4
        assertEquals(expectedNotesSize, controller.getAllNotes().size)

        controller.undo()
        expectedNotesSize = 3
        assertEquals(expectedNotesSize, controller.getAllNotes().size)
    }

    @Test
    fun deleteNote() {
        val controller = Controller()
        var expectedNotesSize = 0
        assertEquals(expectedNotesSize, controller.getAllNotes().size)

        val note = controller.createNote()
        expectedNotesSize = 1
        assertEquals(expectedNotesSize, controller.getAllNotes().size)

        controller.deleteNote(note.id)
        expectedNotesSize = 0
        assertEquals(expectedNotesSize, controller.getAllNotes().size)

        controller.undo()
        expectedNotesSize = 1
        assertEquals(expectedNotesSize, controller.getAllNotes().size)

        controller.redo()
        expectedNotesSize = 0
        assertEquals(expectedNotesSize, controller.getAllNotes().size)

        controller.undo()
        expectedNotesSize = 1
        assertEquals(expectedNotesSize, controller.getAllNotes().size)
    }

    @Test
    fun editNoteTitle() {
        val controller = Controller()
        val noteID = controller.createNote().id
        var expectedTitle = ""
        assertEquals(expectedTitle, controller.getNoteByID(noteID).title)

        controller.editNoteTitle(noteID, "new title")
        expectedTitle = "new title"
        assertEquals(expectedTitle, controller.getNoteByID(noteID).title)

        controller.undo()
        expectedTitle = ""
        assertEquals(expectedTitle, controller.getNoteByID(noteID).title)

        controller.redo()
        expectedTitle = "new title"
        assertEquals(expectedTitle, controller.getNoteByID(noteID).title)
    }

    @Test
    fun editNoteContent() {
        val controller = Controller()
        val noteID = controller.createNote().id
        var expectedContent = ""
        assertEquals(expectedContent, controller.getNoteByID(noteID).content)

        controller.editNoteContent(noteID, "new content")
        expectedContent = "new content"
        assertEquals(expectedContent, controller.getNoteByID(noteID).content)

        controller.undo()
        expectedContent = ""
        assertEquals(expectedContent, controller.getNoteByID(noteID).content)

        controller.redo()
        expectedContent = "new content"
        assertEquals(expectedContent, controller.getNoteByID(noteID).content)
    }

    @Test
    fun createGroup() {
        val controller = Controller()
        var group1 = controller.createGroup("group1")
        assertEquals(1, controller.getAllGroups().size)
        assertEquals("group1", group1.name)

        controller.undo()
        assertEquals(0, controller.getAllGroups().size)

        controller.redo()
        assertEquals(1, controller.getAllGroups().size)
        assertEquals("group1", group1.name)

        controller.undo()
        group1 = controller.createGroup("group1")
        assertEquals(1, controller.getAllGroups().size)
        assertEquals("group1", group1.name)
        val group2 = controller.createGroup("group2")
        assertEquals(2, controller.getAllGroups().size)
        assertEquals("group2", group2.name)

        controller.undo()
        assertEquals(1, controller.getAllGroups().size)
        assertEquals("group1", group1.name)

        controller.redo()
        assertEquals("group1", group1.name)
        assertEquals(2, controller.getAllGroups().size)
        assertEquals("group2", group2.name)
    }

    @Test
    fun deleteGroup() {
        val controller = Controller()
        controller.createGroup("group1")
        controller.createGroup("group2")
        assertEquals(2, controller.getAllGroups().size)

        controller.deleteGroup("group1")
        controller.deleteGroup("group2")
        controller.undo()
        controller.undo()
        var groups = controller.getAllGroups()
        assertEquals(2, groups.size)

        controller.redo()
        groups = controller.getAllGroups()
        assertEquals(1, groups.size)

        controller.redo()
        groups = controller.getAllGroups()
        assertEquals(0, groups.size)
    }

    @Test
    fun editGroupName() {
        val controller = Controller()
        controller.createGroup("group1")
        controller.createGroup("group2")
        controller.editGroupName("group1", "group1modified")
        controller.editGroupName("group2", "group2modified")
        controller.undo()

        try{
            controller.getGroupByName("group1modified")
            assert(true)
        } catch (e: NonExistentGroupException) {
            assert(false)
        }
        try{
            controller.getGroupByName("group2")
            assert(true)
        } catch (e: NonExistentGroupException) {
            assert(false)
        }

        controller.redo()
        try{
            controller.getGroupByName("group1modified")
            assert(true)
        } catch (e: NonExistentGroupException) {
            assert(false)
        }
        try{
            controller.getGroupByName("group2modified")
            assert(true)
        } catch (e: NonExistentGroupException) {
            assert(false)
        }
    }

    @Test
    fun addNoteToGroup() {
        val controller = Controller()
        val note1 = controller.createNote("title1", "content1")
        val note2 = controller.createNote("title2", "content2")

        controller.createGroup("group1")
        var notes = controller.getGroupByName("group1").notes
        assertEquals(0, notes.size)

        controller.addNoteToGroup("group1", note1)
        notes = controller.getGroupByName("group1").notes
        assertEquals(1, notes.size)

        controller.undo()
        notes = controller.getGroupByName("group1").notes
        assertEquals(0, notes.size)

        controller.redo()
        notes = controller.getGroupByName("group1").notes
        assertEquals(1, notes.size)
    }

    @Test
    fun removeNoteFromGroup() {
        val controller = Controller()
        val note1 = controller.createNote("title1", "content1")
        controller.createNote("title2", "content2")
        controller.createNote("title3", "content3")
        controller.createNote("title4", "content4")
        val notes = controller.getAllNotes()

        controller.createGroup("group1")
        for (note in notes) {
            controller.addNoteToGroup("group1", note)
        }
        var notesInGroup = controller.getGroupByName("group1").notes
        assertEquals(4, notesInGroup.size)

        controller.removeNoteFromGroup("group1", note1)
        notesInGroup = controller.getGroupByName("group1").notes
        assertEquals(3, notesInGroup.size)

        controller.undo()
        notesInGroup = controller.getGroupByName("group1").notes
        assertEquals(4, notesInGroup.size)

        controller.redo()
        notesInGroup = controller.getGroupByName("group1").notes
        assertEquals(3, notesInGroup.size)
    }

    @Test
    fun moveNoteToGroup() {
        val controller = Controller()
        val note1 = controller.createNote("title1", "content1")
        val note2 = controller.createNote("title2", "content2")
        val note3 = controller.createNote("title3", "content3")
        val notesGroup1 = controller.getAllNotes()
        controller.createNote("title4", "content4")
        val notesGroup2 = controller.getNotesByTitle("title4")

        controller.createGroup("group1")
        for (note in notesGroup1) {
            controller.addNoteToGroup("group1", note)
        }
        controller.createGroup("group2")
        for (note in notesGroup2) {
            controller.addNoteToGroup("group2", note)
        }
        var notesInGroup1 = controller.getGroupByName("group1").notes
        var notesInGroup2 = controller.getGroupByName("group2").notes
        assertEquals(3, notesInGroup1.size)
        assertEquals(1, notesInGroup2.size)

        controller.moveNoteToGroup("group2", note1)
        notesInGroup1 = controller.getGroupByName("group1").notes
        notesInGroup2 = controller.getGroupByName("group2").notes
        assertEquals(2, notesInGroup1.size)
        assertEquals(2, notesInGroup2.size)

        controller.undo()
        notesInGroup1 = controller.getGroupByName("group1").notes
        notesInGroup2 = controller.getGroupByName("group2").notes
        assertEquals(3, notesInGroup1.size)
        assertEquals(1, notesInGroup2.size)

        controller.redo()
        notesInGroup1 = controller.getGroupByName("group1").notes
        notesInGroup2 = controller.getGroupByName("group2").notes
        assertEquals(2, notesInGroup1.size)
        assertEquals(2, notesInGroup2.size)
    }
}