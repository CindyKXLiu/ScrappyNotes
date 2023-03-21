package cs346.shared

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ModelTest {
    @Test
    fun test() {
        val model = Model()
    }

    @Test
    fun createNote() {
        val model = Model()
        var expectedNotesSize = 0
        assertEquals(expectedNotesSize, model.getAllNotes().size)

        var note = model.createNote()
        expectedNotesSize = 1
        var expectedTitle = ""
        var expectedContent = ""
        assertEquals(expectedNotesSize, model.getAllNotes().size)
        try{
            model.getNoteByID(note.id)
            assert(true)
        } catch (e: NonExistentNoteException) {
            assert(false)
        }
        assertEquals(expectedTitle, note.title)
        assertEquals(expectedContent, note.content)

        note = model.createNote("title")
        expectedNotesSize = 2
        expectedTitle = "title"
        expectedContent = ""
        assertEquals(expectedNotesSize, model.getAllNotes().size)
        try{
            model.getNoteByID(note.id)
            assert(true)
        } catch (e: NonExistentNoteException) {
            assert(false)
        }
        assertEquals(expectedTitle, note.title)
        assertEquals(expectedContent, note.content)

        note = model.createNote(content = "content")
        expectedNotesSize = 3
        expectedTitle = ""
        expectedContent = "content"
        assertEquals(expectedNotesSize, model.getAllNotes().size)
        try{
            model.getNoteByID(note.id)
            assert(true)
        } catch (e: NonExistentNoteException) {
            assert(false)
        }
        assertEquals(expectedTitle, note.title)
        assertEquals(expectedContent, note.content)

        note = model.createNote("title", "content")
        expectedNotesSize = 4
        expectedTitle = "title"
        expectedContent = "content"
        assertEquals(expectedNotesSize, model.getAllNotes().size)
        try{
            model.getNoteByID(note.id)
            assert(true)
        } catch (e: NonExistentNoteException) {
            assert(false)
        }
        assertEquals(expectedTitle, note.title)
        assertEquals(expectedContent, note.content)
    }

    @Test
    fun deleteNote() {
        val model = Model()
        val note1 = model.createNote()
        val note2 = model.createNote()
        val note3 = model.createNote()
        var expectedNotesSize = 3
        assertEquals(expectedNotesSize, model.getAllNotes().size)

        model.deleteNote(note1.id)
        expectedNotesSize = 2
        assertEquals(expectedNotesSize, model.getAllNotes().size)

        model.createGroup("group1")
        model.addNoteToGroup("group1", note2)
        model.addNoteToGroup("group1", note3)
        var expectedGroupSize = 2
        try {
            assertEquals(expectedGroupSize, model.getAllNotesInGroup("group1").size)
            assert(true)
        } catch (e: NonExistentGroupException) {
            assert(false)
        }
        model.deleteNote(note2.id)
        expectedNotesSize = 1
        expectedGroupSize = 1
        assertEquals(expectedNotesSize, model.getAllNotes().size)
        try {
            assertEquals(expectedGroupSize, model.getAllNotesInGroup("group1").size)
            assert(true)
        } catch (e: NonExistentGroupException) {
            assert(false)
        }
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
    }

    @Test
    fun createGroup() {
        val model = Model()
        model.createNote("title1", "content1")
        model.createNote("title2", "content2")
        model.createNote("title3", "content3")
        val notes1 = model.getAllNotes()
        model.createNote("title4", "content4")
        val notes2 = model.getAllNotes()

        assertEquals(0, model.getAllGroups().size)

        val group1 = model.createGroup("group1")
        assertEquals(1, model.getAllGroups().size)
        assertEquals("group1", group1.name)

        val group2 = model.createGroup("group2")
        for (note in notes1) {
            model.addNoteToGroup("group2", note)
        }
        assertEquals(2, model.getAllGroups().size)
        assertEquals("group2", group2.name)
        assertEquals(3, group2.notes.size)

        val group3 = model.createGroup("group3")
        for (note in notes2) {
            model.addNoteToGroup("group3", note)
        }
        assertEquals(3, model.getAllGroups().size)
        assertEquals("group3", group3.name)
        assertEquals(4, group3.notes.size)

        val g = model.createGroup("g")
        try {
            val gDuplicate = model.createGroup("g")
            assert(false)
        } catch (e: DuplicateGroupException) {
            assert(true)
        }
    }

    @Test
    fun deleteGroup() {
        val model = Model()
        model.createNote("title1", "content1")
        model.createNote("title2", "content2")
        model.createNote("title3", "content3")
        val notes1 = model.getAllNotes()
        model.createNote("title4", "content4")
        val notes2 = model.getAllNotes()

        model.createGroup("group1")
        model.createGroup("group2")
        for (note in notes1) {
            model.addNoteToGroup("group2", note)
        }
        model.createGroup("group3")
        for (note in notes2) {
            model.addNoteToGroup("group3", note)
        }
        assertEquals(3, model.getAllGroups().size)

        model.deleteGroup("group2")
        var groups = model.getAllGroups()
        assertEquals(2, groups.size)
        assert(groups[0].name != "group2" && groups[1].name != "group2")

        model.deleteGroup("group1")
        groups = model.getAllGroups()
        assertEquals(1, groups.size)
        assert(groups[0].name != "group1")

        model.deleteGroup("group3")
        groups = model.getAllGroups()
        assertEquals(0, groups.size)
    }

    @Test
    fun getAllGroups() {
        val model = Model()
        model.createNote("title1", "content1")
        model.createNote("title2", "content2")
        model.createNote("title3", "content3")
        val notes1 = model.getAllNotes()
        model.createNote("title4", "content4")
        val notes2 = model.getAllNotes()

        model.createGroup("group1")
        var groups = model.getAllGroups()
        assertEquals(1, groups.size)

        model.createGroup("group2")
        for (note in notes1) {
            model.addNoteToGroup("group2", note)
        }
        groups = model.getAllGroups()
        assertEquals(2, groups.size)

        model.createGroup("group3")
        for (note in notes2) {
            model.addNoteToGroup("group3", note)
        }
        groups = model.getAllGroups()
        assertEquals(3, groups.size)

        model.deleteGroup("group1")
        groups = model.getAllGroups()
        assertEquals(2, groups.size)

        model.deleteGroup("group2")
        groups = model.getAllGroups()
        assertEquals(1, groups.size)

        model.deleteGroup("group3")
        groups = model.getAllGroups()
        assertEquals(0, groups.size)
    }

    @Test
    fun getGroupByName() {
        val model = Model()
        model.createNote("title1", "content1")
        model.createNote("title2", "content2")
        model.createNote("title3", "content3")
        val notes1 = model.getAllNotes()
        model.createNote("title4", "content4")
        val notes2 = model.getAllNotes()

        val group1 = model.createGroup("group1")
        val group2 = model.createGroup("group2")
        for (note in notes1) {
            model.addNoteToGroup("group2", note)
        }
        val group3 = model.createGroup("group3")
        for (note in notes2) {
            model.addNoteToGroup("group3", note)
        }

        try{
            model.getGroupByName("notagroupname")
            assert(false)
        } catch (e: NonExistentGroupException) {
            assert(true)
        }
        val getGroup1 = model.getGroupByName("group1")
        assertEquals(group1.name, getGroup1.name)
        assertEquals(group1.notes, getGroup1.notes)
        val getGroup2 = model.getGroupByName("group2")
        assertEquals(group2.name, getGroup2.name)
        assertEquals(group2.notes, getGroup2.notes)
        val getGroup3 = model.getGroupByName("group3")
        assertEquals(group3.name, getGroup3.name)
        assertEquals(group3.notes, getGroup3.notes)
    }

    @Test
    fun editGroupName() {
        val model = Model()
        model.createNote("title1", "content1")
        model.createNote("title2", "content2")
        model.createNote("title3", "content3")
        val notes1 = model.getAllNotes()
        model.createNote("title4", "content4")
        val notes2 = model.getAllNotes()

        model.createGroup("group1")
        model.createGroup("group2")
        for (note in notes1) {
            model.addNoteToGroup("group2", note)
        }
        model.createGroup("group3")
        for (note in notes2) {
            model.addNoteToGroup("group3", note)
        }

        model.editGroupName("group1", "group1modified")
        model.editGroupName("group2", "group2modified")
        model.editGroupName("group3", "group3modified")
        try{
            model.getGroupByName("group1")
            assert(false)
        } catch (e: NonExistentGroupException) {
            assert(true)
        }
        try{
            model.getGroupByName("group1modified")
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
        val note3 = model.createNote("title3", "content3")
        val note4 = model.createNote("title4", "content4")

        model.createGroup("group1")
        var notes = model.getAllNotesInGroup("group1")
        assertEquals(0, notes.size)

        model.addNoteToGroup("group1", note1)
        notes = model.getAllNotesInGroup("group1")
        assertEquals(1, notes.size)

        model.addNoteToGroup("group1", note2)
        notes = model.getAllNotesInGroup("group1")
        assertEquals(2, notes.size)

        model.addNoteToGroup("group1", note3)
        notes = model.getAllNotesInGroup("group1")
        assertEquals(3, notes.size)

        model.addNoteToGroup("group1", note4)
        notes = model.getAllNotesInGroup("group1")
        assertEquals(4, notes.size)
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
        var notesInGroup = model.getAllNotesInGroup("group1")
        assertEquals(4, notesInGroup.size)

        model.removeNoteFromGroup("group1", note1)
        notesInGroup = model.getAllNotesInGroup("group1")
        assertEquals(3, notesInGroup.size)

        model.removeNoteFromGroup("group1", note2)
        notesInGroup = model.getAllNotesInGroup("group1")
        assertEquals(2, notesInGroup.size)

        model.removeNoteFromGroup("group1", note3)
        notesInGroup = model.getAllNotesInGroup("group1")
        assertEquals(1, notesInGroup.size)

        model.removeNoteFromGroup("group1", note4)
        notesInGroup = model.getAllNotesInGroup("group1")
        assertEquals(0, notesInGroup.size)
    }

    @Test
    fun moveNoteToGroup() {
        val model = Model()
        val note1 = model.createNote("title1", "content1")
        val note2 = model.createNote("title2", "content2")
        val note3 = model.createNote("title3", "content3")
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
        var notesInGroup1 = model.getAllNotesInGroup("group1")
        var notesInGroup2 = model.getAllNotesInGroup("group2")
        assertEquals(3, notesInGroup1.size)
        assertEquals(1, notesInGroup2.size)

        model.moveNoteToGroup("group2", note1)
        notesInGroup1 = model.getAllNotesInGroup("group1")
        notesInGroup2 = model.getAllNotesInGroup("group2")
        assertEquals(2, notesInGroup1.size)
        assertEquals(2, notesInGroup2.size)

        model.moveNoteToGroup("group2", note2)
        notesInGroup1 = model.getAllNotesInGroup("group1")
        notesInGroup2 = model.getAllNotesInGroup("group2")
        assertEquals(1, notesInGroup1.size)
        assertEquals(3, notesInGroup2.size)

        model.moveNoteToGroup("group2", note3)
        notesInGroup1 = model.getAllNotesInGroup("group1")
        notesInGroup2 = model.getAllNotesInGroup("group2")
        assertEquals(0, notesInGroup1.size)
        assertEquals(4, notesInGroup2.size)
    }

    @Test
    fun getAllNotesInGroup() {
        val model = Model()
        model.createNote("title1", "content1")
        model.createNote("title2", "content2")
        model.createNote("title3", "content3")
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

        val notesInGroup1 = model.getAllNotesInGroup("group1")
        val notesInGroup2 = model.getAllNotesInGroup("group2")

        assertEquals(3, notesInGroup1.size)
        assertEquals(1, notesInGroup2.size)
    }

    @Test
    fun getAllNotes() {
        val model = Model()
        var expectedNotesSize = 0
        assertEquals(expectedNotesSize, model.getAllNotes().size)

        model.createNote("title", "content")
        Thread.sleep(1)
        var allNotes = model.getAllNotes()
        val expectedTitle = "title"
        val expectedContent = "content"
        expectedNotesSize = 1
        assertEquals(expectedNotesSize, allNotes.size)
        assertEquals(expectedTitle, allNotes[0].title)
        assertEquals(expectedContent, allNotes[0].content)

        model.createNote("title", "content")
        allNotes = model.getAllNotes()
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
        val model = Model()
        var expectedSize = 0
        assertEquals(expectedSize, model.getAllUngroupedNotes().size)

        val note1 = model.createNote("n1")
        expectedSize = 1
        assertEquals(expectedSize, model.getAllUngroupedNotes().size)
        assert(model.getAllUngroupedNotes().contains(note1))

        val note2 = model.createNote("n2")
        expectedSize = 2
        assertEquals(expectedSize, model.getAllUngroupedNotes().size)
        assert(model.getAllUngroupedNotes().contains(note1))
        assert(model.getAllUngroupedNotes().contains(note2))

        model.createGroup("g")
        expectedSize = 2
        assertEquals(expectedSize, model.getAllUngroupedNotes().size)
        assert(model.getAllUngroupedNotes().contains(note1))
        assert(model.getAllUngroupedNotes().contains(note2))

        model.addNoteToGroup("g", note1)
        expectedSize = 1
        assertEquals(expectedSize, model.getAllUngroupedNotes().size)
        assert(model.getAllUngroupedNotes().contains(note2))

        model.addNoteToGroup("g", note2)
        expectedSize = 0
        assertEquals(expectedSize, model.getAllUngroupedNotes().size)
    }
    @Test
    fun getNotesByDateCreated() {
        val model = Model()
        val note1 = model.createNote("n1", "c1")
        val expectedSize = 1
        assertEquals(expectedSize, model.getNotesByDateCreated(note1.dateCreated).size)
        assertEquals(note1, model.getNotesByDateCreated(note1.dateCreated)[0])
    }

    @Test
    fun getNotesByTitle() {
        val model = Model()
        var expectedSize = 0
        assertEquals(expectedSize, model.getNotesByTitle("title").size)

        model.createNote("title", "content1")
        model.createNote("t", "content2")
        expectedSize = 1
        val expectedTitle = "title"
        val expectedContent = "content1"
        var notesTitledTitle = model.getNotesByTitle("title")
        assertEquals(expectedSize, notesTitledTitle.size)
        assertEquals(expectedTitle, notesTitledTitle[0].title)
        assertEquals(expectedContent, notesTitledTitle[0].content)

        model.createNote("title", "content3")
        expectedSize = 2
        val expectedContent2 = "content3"
        notesTitledTitle = model.getNotesByTitle("title")
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
        val model = Model()
        var expectedSize = 0

        model.createNote("note1", "content")
        model.createNote("note2", "content")
        model.createNote("note3", "this note contains some content")
        model.createNote("note4", "cont")

        var notesWithContent = model.getNotesByContent("title")
        assertEquals(expectedSize, notesWithContent.size)

        notesWithContent = model.getNotesByContent("")
        expectedSize = 4
        var noteList = notesWithContent.map { it.title }
        assertEquals(expectedSize, notesWithContent.size)
        assert(noteList.contains("note1"))
        assert(noteList.contains("note2"))
        assert(noteList.contains("note3"))
        assert(noteList.contains("note4"))

        notesWithContent = model.getNotesByContent("content")
        expectedSize = 3
        noteList = notesWithContent.map { it.title }
        assertEquals(expectedSize, notesWithContent.size)
        assert(noteList.contains("note1"))
        assert(noteList.contains("note2"))
        assert(noteList.contains("note3"))
    }
}