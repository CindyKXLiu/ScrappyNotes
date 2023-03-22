package cs346.shared

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class UndoTest {
    @Test
    fun createNote() {
        val model = Model()
        var expectedNotesSize = 0
        assertEquals(expectedNotesSize, model.getAllNotes().size)

        model.createNote()
        expectedNotesSize = 1
        assertEquals(expectedNotesSize, model.getAllNotes().size)

        model.undo()
        expectedNotesSize = 0
        assertEquals(expectedNotesSize, model.getAllNotes().size)
    }

    @Test
    fun deleteNote() {
        val model = Model()
        var expectedNotesSize = 0
        assertEquals(expectedNotesSize, model.getAllNotes().size)

        val note = model.createNote()
        expectedNotesSize = 1
        assertEquals(expectedNotesSize, model.getAllNotes().size)

        model.deleteNote(note.id)
        expectedNotesSize = 0
        assertEquals(expectedNotesSize, model.getAllNotes().size)

        model.undo()
        expectedNotesSize = 1
        assertEquals(expectedNotesSize, model.getAllNotes().size)
    }

    @Test
    fun editNoteTitle() {
        val model = Model()
        val noteID = model.createNote().id
        var expectedTitle = ""
        assertEquals(expectedTitle, model.getNoteByID(noteID).title)

        model.editNoteTitle(noteID, "new title")
        expectedTitle = "new title"
        assertEquals(expectedTitle, model.getNoteByID(noteID).title)

        model.undo()
        expectedTitle = ""
        assertEquals(expectedTitle, model.getNoteByID(noteID).title)
    }

    @Test
    fun editNoteContent() {
        val model = Model()
        val noteID = model.createNote().id
        var expectedContent = ""
        assertEquals(expectedContent, model.getNoteByID(noteID).content)

        model.editNoteContent(noteID, "new content")
        expectedContent = "new content"
        assertEquals(expectedContent, model.getNoteByID(noteID).content)

        model.undo()
        expectedContent = ""
        assertEquals(expectedContent, model.getNoteByID(noteID).content)
    }

    @Test
    fun createGroup() {
        val model = Model()
        var group1 = model.createGroup("group1")
        assertEquals(1, model.getAllGroups().size)
        assertEquals("group1", group1.name)

        model.undo()
        assertEquals(0, model.getAllGroups().size)

        group1 = model.createGroup("group1")
        assertEquals(1, model.getAllGroups().size)
        assertEquals("group1", group1.name)
        val group2 = model.createGroup("group2")
        assertEquals(2, model.getAllGroups().size)
        assertEquals("group2", group2.name)

        model.undo()
        assertEquals(1, model.getAllGroups().size)
        assertEquals("group1", group1.name)
    }

    @Test
    fun deleteGroup() {
        val model = Model()
        model.createGroup("group1")
        model.createGroup("group2")
        assertEquals(2, model.getAllGroups().size)

        model.deleteGroup("group2")
        var groups = model.getAllGroups()
        assertEquals(1, groups.size)

        model.undo()
        groups = model.getAllGroups()
        assertEquals(2, groups.size)

        model.deleteGroup("group1")
        model.deleteGroup("group2")
        model.undo()
        groups = model.getAllGroups()
        assertEquals(1, groups.size)
        assertEquals("group2", groups[0].name)
    }

    @Test
    fun editGroupName() {
        val model = Model()
        model.createGroup("group1")
        model.createGroup("group2")
        model.editGroupName("group1", "group1modified")
        model.editGroupName("group2", "group2modified")
        model.undo()

        try{
            model.getGroupByName("group1modified")
            assert(true)
        } catch (e: NonExistentGroupException) {
            assert(false)
        }
        try{
            model.getGroupByName("group2")
            assert(true)
        } catch (e: NonExistentGroupException) {
            assert(false)
        }
    }

    @Test
    fun addNoteToGroup() {
        val model = Model()
        val note1 = model.createNote("title1", "content1")
        val note2 = model.createNote("title2", "content2")

        model.createGroup("group1")
        var notes = model.getGroupByName("group1").notes
        assertEquals(0, notes.size)

        model.addNoteToGroup("group1", note1)
        notes = model.getGroupByName("group1").notes
        assertEquals(1, notes.size)

        model.undo()
        notes = model.getGroupByName("group1").notes
        assertEquals(0, notes.size)

        model.addNoteToGroup("group1", note1)
        notes = model.getGroupByName("group1").notes
        assertEquals(1, notes.size)

        model.addNoteToGroup("group1", note2)
        notes = model.getGroupByName("group1").notes
        assertEquals(2, notes.size)

        model.undo()
        notes = model.getGroupByName("group1").notes
        assertEquals(1, notes.size)
    }

    @Test
    fun removeNoteFromGroup() {
        val model = Model()
        val note1 = model.createNote("title1", "content1")
        val note2 = model.createNote("title2", "content2")
        val note3 = model.createNote("title3", "content3")
        val note4 = model.createNote("title4", "content4")
        val notes = model.getAllNotes()

        model.createGroup("group1")
        for (note in notes) {
            model.addNoteToGroup("group1", note)
        }
        var notesInGroup = model.getGroupByName("group1").notes
        assertEquals(4, notesInGroup.size)

        model.removeNoteFromGroup("group1", note1)
        notesInGroup = model.getGroupByName("group1").notes
        assertEquals(3, notesInGroup.size)

        model.undo()
        notesInGroup = model.getGroupByName("group1").notes
        assertEquals(4, notesInGroup.size)

        model.removeNoteFromGroup("group1", note1)
        notesInGroup = model.getGroupByName("group1").notes
        assertEquals(3, notesInGroup.size)

        model.removeNoteFromGroup("group1", note2)
        notesInGroup = model.getGroupByName("group1").notes
        assertEquals(2, notesInGroup.size)

        model.removeNoteFromGroup("group1", note3)
        notesInGroup = model.getGroupByName("group1").notes
        assertEquals(1, notesInGroup.size)

        model.removeNoteFromGroup("group1", note4)
        notesInGroup = model.getGroupByName("group1").notes
        assertEquals(0, notesInGroup.size)

        model.undo()
        notesInGroup = model.getGroupByName("group1").notes
        assertEquals(1, notesInGroup.size)
    }

    @Test
    fun moveNoteToGroup() {
        val model = Model()
        val note1 = model.createNote("title1", "content1")
        val note1ID = note1.id
        val note2 = model.createNote("title2", "content2")
        val note2ID = note2.id
        val note3 = model.createNote("title3", "content3")
        val note3ID = note3.id
        val notesGroup1 = model.getAllNotes()
        model.createNote("title4", "content4")
        val notesGroup2 = model.getNotesByTitle("title4")

        model.createGroup("group1")
        for (note in notesGroup1) {
            model.addNoteToGroup("group1", note)
        }
        model.createGroup("group2")
        for (note in notesGroup2) {
            model.addNoteToGroup("group2", note)
        }
        var notesInGroup1 = model.getGroupByName("group1").notes
        var notesInGroup2 = model.getGroupByName("group2").notes
        assertEquals(3, notesInGroup1.size)
        assertEquals(1, notesInGroup2.size)

        model.moveNoteToGroup("group2", note1)
        notesInGroup1 = model.getGroupByName("group1").notes
        notesInGroup2 = model.getGroupByName("group2").notes
        assertEquals(2, notesInGroup1.size)
        assertEquals(2, notesInGroup2.size)

        model.undo()
        notesInGroup1 = model.getGroupByName("group1").notes
        notesInGroup2 = model.getGroupByName("group2").notes
        assertEquals(3, notesInGroup1.size)
        assertEquals(1, notesInGroup2.size)

        model.moveNoteToGroup("group2", model.getNoteByID(note1ID))
        notesInGroup1 = model.getGroupByName("group1").notes
        notesInGroup2 = model.getGroupByName("group2").notes
        assertEquals(2, notesInGroup1.size)
        assertEquals(2, notesInGroup2.size)

        model.moveNoteToGroup("group2", model.getNoteByID(note2ID))
        notesInGroup1 = model.getGroupByName("group1").notes
        notesInGroup2 = model.getGroupByName("group2").notes
        assertEquals(1, notesInGroup1.size)
        assertEquals(3, notesInGroup2.size)

        model.moveNoteToGroup("group2", model.getNoteByID(note3ID))
        notesInGroup1 = model.getGroupByName("group1").notes
        notesInGroup2 = model.getGroupByName("group2").notes
        assertEquals(0, notesInGroup1.size)
        assertEquals(4, notesInGroup2.size)

        model.undo()
        notesInGroup1 = model.getGroupByName("group1").notes
        notesInGroup2 = model.getGroupByName("group2").notes
        assertEquals(1, notesInGroup1.size)
        assertEquals(3, notesInGroup2.size)
    }
}