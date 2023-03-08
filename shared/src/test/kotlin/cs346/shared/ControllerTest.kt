package cs346.shared

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ControllerTest {
    @Test
    fun createNote() {
        val controller = Controller()
        var expectedNotesSize = 0
        assertEquals(expectedNotesSize, controller.getAllNotes().size)

        var note = controller.createNote()
        expectedNotesSize = 1
        var expectedTitle = ""
        var expectedContent = ""
        assertEquals(expectedNotesSize, controller.getAllNotes().size)
        try{
            controller.getNoteByID(note.id)
            assert(true)
        } catch (e: NonExistentNoteException) {
            assert(false)
        }
        assertEquals(expectedTitle, note.title)
        assertEquals(expectedContent, note.content)

        note = controller.createNote("title")
        expectedNotesSize = 2
        expectedTitle = "title"
        expectedContent = ""
        assertEquals(expectedNotesSize, controller.getAllNotes().size)
        try{
            controller.getNoteByID(note.id)
            assert(true)
        } catch (e: NonExistentNoteException) {
            assert(false)
        }
        assertEquals(expectedTitle, note.title)
        assertEquals(expectedContent, note.content)

        note = controller.createNote(content = "content")
        expectedNotesSize = 3
        expectedTitle = ""
        expectedContent = "content"
        assertEquals(expectedNotesSize, controller.getAllNotes().size)
        try{
            controller.getNoteByID(note.id)
            assert(true)
        } catch (e: NonExistentNoteException) {
            assert(false)
        }
        assertEquals(expectedTitle, note.title)
        assertEquals(expectedContent, note.content)

        note = controller.createNote("title", "content")
        expectedNotesSize = 4
        expectedTitle = "title"
        expectedContent = "content"
        assertEquals(expectedNotesSize, controller.getAllNotes().size)
        try{
            controller.getNoteByID(note.id)
            assert(true)
        } catch (e: NonExistentNoteException) {
            assert(false)
        }
        assertEquals(expectedTitle, note.title)
        assertEquals(expectedContent, note.content)
    }

    @Test
    fun deleteNote() {
        val controller = Controller()
        val note1 = controller.createNote()
        val note2 = controller.createNote()
        val note3 = controller.createNote()
        var expectedNotesSize = 3
        assertEquals(expectedNotesSize, controller.getAllNotes().size)

        controller.deleteNote(note1.id)
        expectedNotesSize = 2
        assertEquals(expectedNotesSize, controller.getAllNotes().size)

        controller.createGroup("group1")
        controller.addNoteToGroup("group1", note2)
        controller.addNoteToGroup("group1", note3)
        var expectedGroupSize = 2
        try {
            controller.getGroupByName("group1").notes.let { assertEquals(expectedGroupSize, it.size) }
            assert(true)
        } catch (e: NonExistentGroupException) {
            assert(false)
        }
        controller.deleteNote(note2.id)
        expectedNotesSize = 1
        expectedGroupSize = 1
        assertEquals(expectedNotesSize, controller.getAllNotes().size)
        try {
            controller.getGroupByName("group1").notes.let { assertEquals(expectedGroupSize, it.size) }
            assert(true)
        } catch (e: NonExistentGroupException) {
            assert(false)
        }
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
    }

    @Test
    fun createGroup() {
        val controller = Controller()
        controller.createNote("title1", "content1")
        controller.createNote("title2", "content2")
        controller.createNote("title3", "content3")
        val notes1 = controller.getAllNotes()
        controller.createNote("title4", "content4")
        val notes2 = controller.getAllNotes()

        assertEquals(0, controller.getAllGroups().size)

        val group1 = controller.createGroup("group1")
        assertEquals(1, controller.getAllGroups().size)
        assertEquals("group1", group1.name)

        val group2 = controller.createGroup("group2")
        for (note in notes1) {
            controller.addNoteToGroup("group2", note)
        }
        assertEquals(2, controller.getAllGroups().size)
        assertEquals("group2", group2.name)
        assertEquals(3, group2.notes.size)

        val group3 = controller.createGroup("group3")
        for (note in notes2) {
            controller.addNoteToGroup("group3", note)
        }
        assertEquals(3, controller.getAllGroups().size)
        assertEquals("group3", group3.name)
        assertEquals(4, group3.notes.size)
    }

    @Test
    fun deleteGroup() {
        val controller = Controller()
        controller.createNote("title1", "content1")
        controller.createNote("title2", "content2")
        controller.createNote("title3", "content3")
        val notes1 = controller.getAllNotes()
        controller.createNote("title4", "content4")
        val notes2 = controller.getAllNotes()

        controller.createGroup("group1")
        controller.createGroup("group2")
        for (note in notes1) {
            controller.addNoteToGroup("group2", note)
        }
        controller.createGroup("group3")
        for (note in notes2) {
            controller.addNoteToGroup("group3", note)
        }
        assertEquals(3, controller.getAllGroups().size)

        controller.deleteGroup("group2")
        var groups = controller.getAllGroups()
        assertEquals(2, groups.size)
        assert(groups[0].name != "group2" && groups[1].name != "group2")

        controller.deleteGroup("group1")
        groups = controller.getAllGroups()
        assertEquals(1, groups.size)
        assert(groups[0].name != "group1")

        controller.deleteGroup("group3")
        groups = controller.getAllGroups()
        assertEquals(0, groups.size)
    }

    @Test
    fun getAllGroups() {
        val controller = Controller()
        controller.createNote("title1", "content1")
        controller.createNote("title2", "content2")
        controller.createNote("title3", "content3")
        val notes1 = controller.getAllNotes()
        controller.createNote("title4", "content4")
        val notes2 = controller.getAllNotes()

        controller.createGroup("group1")
        var groups = controller.getAllGroups()
        assertEquals(1, groups.size)

        controller.createGroup("group2")
        for (note in notes1) {
            controller.addNoteToGroup("group2", note)
        }
        groups = controller.getAllGroups()
        assertEquals(2, groups.size)

        controller.createGroup("group3")
        for (note in notes2) {
            controller.addNoteToGroup("group3", note)
        }
        groups = controller.getAllGroups()
        assertEquals(3, groups.size)

        controller.deleteGroup("group1")
        groups = controller.getAllGroups()
        assertEquals(2, groups.size)

        controller.deleteGroup("group2")
        groups = controller.getAllGroups()
        assertEquals(1, groups.size)

        controller.deleteGroup("group3")
        groups = controller.getAllGroups()
        assertEquals(0, groups.size)
    }

    @Test
    fun getGroupByName() {
        val controller = Controller()
        controller.createNote("title1", "content1")
        controller.createNote("title2", "content2")
        controller.createNote("title3", "content3")
        val notes1 = controller.getAllNotes()
        controller.createNote("title4", "content4")
        val notes2 = controller.getAllNotes()

        val group1 = controller.createGroup("group1")
        val group2 = controller.createGroup("group2")
        for (note in notes1) {
            controller.addNoteToGroup("group2", note)
        }
        val group3 = controller.createGroup("group3")
        for (note in notes2) {
            controller.addNoteToGroup("group3", note)
        }

        try{
            controller.getGroupByName("notagroupname")
            assert(false)
        } catch (e: NonExistentGroupException) {
            assert(true)
        }
        val getGroup1 = controller.getGroupByName("group1")
        assertEquals(group1.name, getGroup1.name)
        assertEquals(group1.notes, getGroup1.notes)
        val getGroup2 = controller.getGroupByName("group2")
        assertEquals(group2.name, getGroup2.name)
        assertEquals(group2.notes, getGroup2.notes)
        val getGroup3 = controller.getGroupByName("group3")
        assertEquals(group3.name, getGroup3.name)
        assertEquals(group3.notes, getGroup3.notes)
    }

    @Test
    fun editGroupName() {
        val controller = Controller()
        controller.createNote("title1", "content1")
        controller.createNote("title2", "content2")
        controller.createNote("title3", "content3")
        val notes1 = controller.getAllNotes()
        controller.createNote("title4", "content4")
        val notes2 = controller.getAllNotes()

        controller.createGroup("group1")
        controller.createGroup("group2")
        for (note in notes1) {
            controller.addNoteToGroup("group2", note)
        }
        controller.createGroup("group3")
        for (note in notes2) {
            controller.addNoteToGroup("group3", note)
        }

        controller.editGroupName("group1", "group1modified")
        controller.editGroupName("group2", "group2modified")
        controller.editGroupName("group3", "group3modified")
        try{
            controller.getGroupByName("group1")
            assert(false)
        } catch (e: NonExistentGroupException) {
            assert(true)
        }
        try{
            controller.getGroupByName("group1modified")
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
        val note3 = controller.createNote("title3", "content3")
        val note4 = controller.createNote("title4", "content4")

        controller.createGroup("group1")
        var notes = controller.getGroupByName("group1").notes
        assertEquals(0, notes.size)

        controller.addNoteToGroup("group1", note1)
        notes = controller.getGroupByName("group1").notes
        assertEquals(1, notes.size)

        controller.addNoteToGroup("group1", note2)
        notes = controller.getGroupByName("group1").notes
        assertEquals(2, notes.size)

        controller.addNoteToGroup("group1", note3)
        notes = controller.getGroupByName("group1").notes
        assertEquals(3, notes.size)

        controller.addNoteToGroup("group1", note4)
        notes = controller.getGroupByName("group1").notes
        assertEquals(4, notes.size)
    }

    @Test
    fun removeNoteFromGroup() {
        val controller = Controller()
        val note1 = controller.createNote("title1", "content1")
        val note2 = controller.createNote("title2", "content2")
        val note3 = controller.createNote("title3", "content3")
        val note4 = controller.createNote("title4", "content4")
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

        controller.removeNoteFromGroup("group1", note2)
        notesInGroup = controller.getGroupByName("group1").notes
        assertEquals(2, notesInGroup.size)

        controller.removeNoteFromGroup("group1", note3)
        notesInGroup = controller.getGroupByName("group1").notes
        assertEquals(1, notesInGroup.size)

        controller.removeNoteFromGroup("group1", note4)
        notesInGroup = controller.getGroupByName("group1").notes
        assertEquals(0, notesInGroup.size)
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

        controller.moveNoteToGroup("group2", note2)
        notesInGroup1 = controller.getGroupByName("group1").notes
        notesInGroup2 = controller.getGroupByName("group2").notes
        assertEquals(1, notesInGroup1.size)
        assertEquals(3, notesInGroup2.size)

        controller.moveNoteToGroup("group2", note3)
        notesInGroup1 = controller.getGroupByName("group1").notes
        notesInGroup2 = controller.getGroupByName("group2").notes
        assertEquals(0, notesInGroup1.size)
        assertEquals(4, notesInGroup2.size)
    }

    @Test
    fun getAllNotes() {
        val controller = Controller()
        var expectedNotesSize = 0
        assertEquals(expectedNotesSize, controller.getAllNotes().size)

        controller.createNote("title", "content")
        Thread.sleep(1)
        var allNotes = controller.getAllNotes()
        val expectedTitle = "title"
        val expectedContent = "content"
        expectedNotesSize = 1
        assertEquals(expectedNotesSize, allNotes.size)
        assertEquals(expectedTitle, allNotes[0].title)
        assertEquals(expectedContent, allNotes[0].content)

        controller.createNote("title", "content")
        allNotes = controller.getAllNotes()
        expectedNotesSize = 2
        assertEquals(expectedNotesSize, allNotes.size)
        assertEquals(expectedTitle, allNotes[0].title)
        assertEquals(expectedContent, allNotes[0].content)
        assertEquals(expectedTitle, allNotes[1].title)
        assertEquals(expectedContent, allNotes[1].content)
        assert(allNotes[0].dateCreated != allNotes[1].dateCreated)
    }

    @Test
    fun getAllUngroupedNotes() {
        val controller = Controller()
        var expectedSize = 0
        assertEquals(expectedSize, controller.getAllUngroupedNotes().size)

        val note1 = controller.createNote("n1")
        expectedSize = 1
        assertEquals(expectedSize, controller.getAllUngroupedNotes().size)
        assert(controller.getAllUngroupedNotes().contains(note1))

        val note2 = controller.createNote("n2")
        expectedSize = 2
        assertEquals(expectedSize, controller.getAllUngroupedNotes().size)
        assert(controller.getAllUngroupedNotes().contains(note1))
        assert(controller.getAllUngroupedNotes().contains(note2))

        controller.createGroup("g")
        expectedSize = 2
        assertEquals(expectedSize, controller.getAllUngroupedNotes().size)
        assert(controller.getAllUngroupedNotes().contains(note1))
        assert(controller.getAllUngroupedNotes().contains(note2))

        controller.addNoteToGroup("g", note1)
        expectedSize = 1
        assertEquals(expectedSize, controller.getAllUngroupedNotes().size)
        assert(controller.getAllUngroupedNotes().contains(note2))

        controller.addNoteToGroup("g", note2)
        expectedSize = 0
        assertEquals(expectedSize, controller.getAllUngroupedNotes().size)
    }
    @Test
    fun getNotesByDateCreated() {
        val controller = Controller()
        val note1 = controller.createNote("n1", "c1")
        val expectedSize = 1
        assertEquals(expectedSize, controller.getNotesByDateCreated(note1.dateCreated).size)
        assertEquals(note1, controller.getNotesByDateCreated(note1.dateCreated)[0])
    }

    @Test
    fun getNotesByTitle() {
        val controller = Controller()
        var expectedSize = 0
        assertEquals(expectedSize, controller.getNotesByTitle("title").size)

        controller.createNote("title", "content1")
        controller.createNote("t", "content2")
        expectedSize = 1
        val expectedTitle = "title"
        val expectedContent = "content1"
        var notesTitledTitle = controller.getNotesByTitle("title")
        assertEquals(expectedSize, notesTitledTitle.size)
        assertEquals(expectedTitle, notesTitledTitle[0].title)
        assertEquals(expectedContent, notesTitledTitle[0].content)

        controller.createNote("title", "content3")
        expectedSize = 2
        val expectedContent2 = "content3"
        notesTitledTitle = controller.getNotesByTitle("title")
        assertEquals(expectedSize, notesTitledTitle.size)
        assertEquals(expectedTitle, notesTitledTitle[0].title)
        assertEquals(expectedTitle, notesTitledTitle[1].title)
        assert((expectedContent == notesTitledTitle[0].content)
                || (expectedContent2 == notesTitledTitle[0].content))
        if (expectedContent == notesTitledTitle[0].content) {
            assertEquals(expectedContent2, notesTitledTitle[1].content)
        } else {
            assertEquals(expectedContent, notesTitledTitle[1].content)
        }
    }

    @Test
    fun getNotesByContent() {
        val controller = Controller()
        var expectedSize = 0

        controller.createNote("note1", "content")
        controller.createNote("note2", "content")
        controller.createNote("note3", "this note contains some content")
        controller.createNote("note4", "cont")

        var notesWithContent = controller.getNotesByContent("title")
        assertEquals(expectedSize, notesWithContent.size)

        notesWithContent = controller.getNotesByContent("")
        expectedSize = 4
        var noteList = notesWithContent.map { it.title }
        assertEquals(expectedSize, notesWithContent.size)
        assert(noteList.contains("note1"))
        assert(noteList.contains("note2"))
        assert(noteList.contains("note3"))
        assert(noteList.contains("note4"))

        notesWithContent = controller.getNotesByContent("content")
        expectedSize = 3
        noteList = notesWithContent.map { it.title }
        assertEquals(expectedSize, notesWithContent.size)
        assert(noteList.contains("note1"))
        assert(noteList.contains("note2"))
        assert(noteList.contains("note3"))
    }

    @Test
    fun getSortedNotesByTitleAscending() {
        val controller = Controller()
        controller.createNote("a")
        controller.createNote("b")
        controller.createNote("c")
        controller.createNote("c1")
        controller.createNote("c2")
        val expectedSize = 5
        val sortedNote = controller.getSortedNotesByTitleAscending()
        assertEquals(expectedSize, sortedNote.size)
        assertEquals("a", sortedNote[0].title)
        assertEquals("b", sortedNote[1].title)
        assertEquals("c", sortedNote[2].title)
        assertEquals("c1", sortedNote[3].title)
        assertEquals("c2", sortedNote[4].title)
    }

    @Test
    fun getSortedNotesByTitleDescending() {
        val controller = Controller()
        controller.createNote("a")
        controller.createNote("b")
        controller.createNote("c")
        controller.createNote("c1")
        controller.createNote("c2")
        val expectedSize = 5
        val sortedNote = controller.getSortedNotesByTitleDescending()
        assertEquals(expectedSize, sortedNote.size)
        assertEquals("a", sortedNote[4].title)
        assertEquals("b", sortedNote[3].title)
        assertEquals("c", sortedNote[2].title)
        assertEquals("c1", sortedNote[1].title)
        assertEquals("c2", sortedNote[0].title)
    }

    @Test
    fun getSortedNotesByModifiedDateAscending() {
        val controller = Controller()
        val note1 = controller.createNote("1")
        Thread.sleep(1)
        controller.createNote("2")
        Thread.sleep(1)
        controller.createNote("3")
        Thread.sleep(1)
        controller.createNote("4")
        Thread.sleep(1)
        val note5 = controller.createNote("5")
        var expectedSize = 5
        var sortedNote = controller.getSortedNotesByModifiedDateAscending()
        assertEquals(expectedSize, sortedNote.size)
        assertEquals("1", sortedNote[0].title)
        assertEquals("2", sortedNote[1].title)
        assertEquals("3", sortedNote[2].title)
        assertEquals("4", sortedNote[3].title)
        assertEquals("5", sortedNote[4].title)

        Thread.sleep(1)

        controller.editNoteContent(note5.id, "content")
        expectedSize = 5
        sortedNote = controller.getSortedNotesByModifiedDateAscending()
        assertEquals(expectedSize, sortedNote.size)
        assertEquals("1", sortedNote[0].title)
        assertEquals("2", sortedNote[1].title)
        assertEquals("3", sortedNote[2].title)
        assertEquals("4", sortedNote[3].title)
        assertEquals("5", sortedNote[4].title)

        Thread.sleep(1)

        controller.editNoteContent(note1.id, "content")
        expectedSize = 5
        sortedNote = controller.getSortedNotesByModifiedDateAscending()
        assertEquals(expectedSize, sortedNote.size)
        assertEquals("1", sortedNote[4].title)
        assertEquals("2", sortedNote[0].title)
        assertEquals("3", sortedNote[1].title)
        assertEquals("4", sortedNote[2].title)
        assertEquals("5", sortedNote[3].title)
    }

    @Test
    fun getSortedNotesByModifiedDateDescending() {
        val controller = Controller()
        val note1 = controller.createNote("1")
        Thread.sleep(1)
        controller.createNote("2")
        Thread.sleep(1)
        controller.createNote("3")
        Thread.sleep(1)
        controller.createNote("4")
        Thread.sleep(1)
        val note5 = controller.createNote("5")
        var expectedSize = 5
        var sortedNote = controller.getSortedNotesByModifiedDateDescending()
        assertEquals(expectedSize, sortedNote.size)
        assertEquals("1", sortedNote[4].title)
        assertEquals("2", sortedNote[3].title)
        assertEquals("3", sortedNote[2].title)
        assertEquals("4", sortedNote[1].title)
        assertEquals("5", sortedNote[0].title)

        controller.editNoteContent(note5.id, "content")
        expectedSize = 5
        sortedNote = controller.getSortedNotesByModifiedDateDescending()
        assertEquals(expectedSize, sortedNote.size)
        assertEquals("1", sortedNote[4].title)
        assertEquals("2", sortedNote[3].title)
        assertEquals("3", sortedNote[2].title)
        assertEquals("4", sortedNote[1].title)
        assertEquals("5", sortedNote[0].title)

        controller.editNoteContent(note1.id, "content")
        expectedSize = 5
        sortedNote = controller.getSortedNotesByModifiedDateDescending()
        assertEquals(expectedSize, sortedNote.size)
        assertEquals("1", sortedNote[0].title)
        assertEquals("2", sortedNote[4].title)
        assertEquals("3", sortedNote[3].title)
        assertEquals("4", sortedNote[2].title)
        assertEquals("5", sortedNote[1].title)
    }

    @Test
    fun getSortedNotesByCreatedDateAscending() {
        val controller = Controller()
        val note1 = controller.createNote("1")
        Thread.sleep(1)
        controller.createNote("2")
        Thread.sleep(1)
        controller.createNote("3")
        Thread.sleep(1)
        controller.createNote("4")
        Thread.sleep(1)
        controller.createNote("5")
        var expectedSize = 5
        var sortedNote = controller.getSortedNotesByCreatedDateAscending()
        assertEquals(expectedSize, sortedNote.size)
        assertEquals("1", sortedNote[0].title)
        assertEquals("2", sortedNote[1].title)
        assertEquals("3", sortedNote[2].title)
        assertEquals("4", sortedNote[3].title)
        assertEquals("5", sortedNote[4].title)

        Thread.sleep(1)

        controller.editNoteContent(note1.id, "content")
        expectedSize = 5
        sortedNote = controller.getSortedNotesByCreatedDateAscending()
        assertEquals(expectedSize, sortedNote.size)
        assertEquals("1", sortedNote[0].title)
        assertEquals("2", sortedNote[1].title)
        assertEquals("3", sortedNote[2].title)
        assertEquals("4", sortedNote[3].title)
        assertEquals("5", sortedNote[4].title)
    }

    @Test
    fun getSortedNotesByCreatedDateDescending() {
        val controller = Controller()
        controller.createNote("1")
        Thread.sleep(1)
        controller.createNote("2")
        Thread.sleep(1)
        controller.createNote("3")
        Thread.sleep(1)
        controller.createNote("4")
        Thread.sleep(1)
        val note5 = controller.createNote("5")
        var expectedSize = 5
        var sortedNote = controller.getSortedNotesByCreatedDateDescending()
        assertEquals(expectedSize, sortedNote.size)
        assertEquals("1", sortedNote[4].title)
        assertEquals("2", sortedNote[3].title)
        assertEquals("3", sortedNote[2].title)
        assertEquals("4", sortedNote[1].title)
        assertEquals("5", sortedNote[0].title)

        controller.editNoteContent(note5.id, "content")
        expectedSize = 5
        sortedNote = controller.getSortedNotesByCreatedDateDescending()
        assertEquals(expectedSize, sortedNote.size)
        assertEquals("1", sortedNote[4].title)
        assertEquals("2", sortedNote[3].title)
        assertEquals("3", sortedNote[2].title)
        assertEquals("4", sortedNote[1].title)
        assertEquals("5", sortedNote[0].title)
    }
}