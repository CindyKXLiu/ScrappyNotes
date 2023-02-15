package cs346.shared

import org.junit.jupiter.api.TestFactory
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

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
        assert(controller.getNoteByDateCreated(note.dateCreated) != null)
        assertEquals(expectedTitle, note.title)
        assertEquals(expectedContent, note.content)

        note = controller.createNote("title")
        expectedNotesSize = 2
        expectedTitle = "title"
        expectedContent = ""
        assertEquals(expectedNotesSize, controller.getAllNotes().size)
        assert(controller.getNoteByDateCreated(note.dateCreated) != null)
        assertEquals(expectedTitle, note.title)
        assertEquals(expectedContent, note.content)

        note = controller.createNote(content = "content")
        expectedNotesSize = 3
        expectedTitle = ""
        expectedContent = "content"
        assertEquals(expectedNotesSize, controller.getAllNotes().size)
        assert(controller.getNoteByDateCreated(note.dateCreated) != null)
        assertEquals(expectedTitle, note.title)
        assertEquals(expectedContent, note.content)

        note = controller.createNote("title", "content")
        expectedNotesSize = 4
        expectedTitle = "title"
        expectedContent = "content"
        assertEquals(expectedNotesSize, controller.getAllNotes().size)
        assert(controller.getNoteByDateCreated(note.dateCreated) != null)
        assertEquals(expectedTitle, note.title)
        assertEquals(expectedContent, note.content)
    }

    @Test
    fun deleteNote() {
        val controller = Controller()
        val id1 = controller.createNote().dateCreated
        val id2 = controller.createNote().dateCreated
        val id3 = controller.createNote().dateCreated
        var expectedNotesSize = 3
        assertEquals(expectedNotesSize, controller.getAllNotes().size)

        controller.deleteNote(Instant.now())
        assertEquals(expectedNotesSize, controller.getAllNotes().size)

        controller.deleteNote(id1)
        expectedNotesSize = 2
        assertEquals(expectedNotesSize, controller.getAllNotes().size)

        // Add one test for removing a note that belongs to a group when group functions are added
    }

    @Test
    fun editNoteTitle() {
        val controller = Controller()
        val noteID = controller.createNote().dateCreated
        var expectedTitle = ""
        controller.getNoteByDateCreated(noteID)?.let { assertEquals(expectedTitle, it.title) }

        controller.editNoteTitle(noteID, "new title")
        expectedTitle = "new title"
        controller.getNoteByDateCreated(noteID)?.let { assertEquals(expectedTitle, it.title) }
    }

    @Test
    fun editNoteContent() {
        val controller = Controller()
        val noteID = controller.createNote().dateCreated
        var expectedContent = ""
        controller.getNoteByDateCreated(noteID)?.let { assertEquals(expectedContent, it.content) }

        controller.editNoteContent(noteID, "new content")
        expectedContent = "new content"
        controller.getNoteByDateCreated(noteID)?.let { assertEquals(expectedContent, it.content) }
    }

    @Test
    fun createGroup() {
        val controller = Controller()
        val note1 = controller.createNote("title1", "content1")
        val note2 = controller.createNote("title2", "content2")
        val note3 = controller.createNote("title3", "content3")
        val notes3 = controller.getAllNotes()
        val note4 = controller.createNote("title4", "content4")
        val notes4 = controller.getAllNotes()

        assertEquals(0, controller.getAllGroups().size)

        val group1 = controller.createGroup("group1")
        assertEquals(1, controller.getAllGroups().size)
        assertEquals("group1", group1.name)

        val group2 = controller.createGroup("group2", notes3)
        assertEquals(2, controller.getAllGroups().size)
        assertEquals("group2", group2.name)
        assertEquals(3, group2.notes.size)

        val group3 = controller.createGroup("group3", notes4)
        assertEquals(3, controller.getAllGroups().size)
        assertEquals("group3", group3.name)
        assertEquals(4, group3.notes.size)
    }

    @Test
    fun deleteGroup() {
        val controller = Controller()

        val note1 = controller.createNote("title1", "content1")
        val note2 = controller.createNote("title2", "content2")
        val note3 = controller.createNote("title3", "content3")
        val notes3 = controller.getAllNotes()
        val note4 = controller.createNote("title4", "content4")
        val notes4 = controller.getAllNotes()

        val group1 = controller.createGroup("group1")
        val group2 = controller.createGroup("group2", notes3)
        val group3 = controller.createGroup("group3", notes4)

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

        val note1 = controller.createNote("title1", "content1")
        val note2 = controller.createNote("title2", "content2")
        val note3 = controller.createNote("title3", "content3")
        val notes3 = controller.getAllNotes()
        val note4 = controller.createNote("title4", "content4")
        val notes4 = controller.getAllNotes()

        val group1 = controller.createGroup("group1")
        var groups = controller.getAllGroups()
        assertEquals(1, groups.size)

        val group2 = controller.createGroup("group2", notes3)
        groups = controller.getAllGroups()
        assertEquals(2, groups.size)

        val group3 = controller.createGroup("group3", notes4)
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

        val note1 = controller.createNote("title1", "content1")
        val note2 = controller.createNote("title2", "content2")
        val note3 = controller.createNote("title3", "content3")
        val notes3 = controller.getAllNotes()
        val note4 = controller.createNote("title4", "content4")
        val notes4 = controller.getAllNotes()

        val group1 = controller.createGroup("group1")
        val group2 = controller.createGroup("group2", notes3)
        val group3 = controller.createGroup("group3", notes4)

        val getGroupNull = controller.getGroupByName("notagroupname")
        assert(getGroupNull == null)
        val getGroup1 = controller.getGroupByName("group1")
        assertEquals(group1.name, getGroup1?.name)
        assertEquals(group1.notes, getGroup1?.notes)
        val getGroup2 = controller.getGroupByName("group2")
        assertEquals(group2.name, getGroup2?.name)
        assertEquals(group2.notes, getGroup2?.notes)
        val getGroup3 = controller.getGroupByName("group3")
        assertEquals(group3.name, getGroup3?.name)
        assertEquals(group3.notes, getGroup3?.notes)
    }

    @Test
    fun editGroupName() {
        val controller = Controller()

        val note1 = controller.createNote("title1", "content1")
        val note2 = controller.createNote("title2", "content2")
        val note3 = controller.createNote("title3", "content3")
        val notes3 = controller.getAllNotes()
        val note4 = controller.createNote("title4", "content4")
        val notes4 = controller.getAllNotes()

        val group1 = controller.createGroup("group1")
        val group2 = controller.createGroup("group2", notes3)
        val group3 = controller.createGroup("group3", notes4)

        controller.editGroupName("group1", "group1modified")
        controller.editGroupName("group2", "group2modified")
        controller.editGroupName("group3", "group3modified")
        assert(controller.getGroupByName("group1") == null)
        assert(controller.getGroupByName("group1modified") != null)
        assert(controller.getGroupByName("group2") == null)
        assert(controller.getGroupByName("group2modified") != null)
        assert(controller.getGroupByName("group3") == null)
        assert(controller.getGroupByName("group3modified") != null)
    }

    @Test
    fun addNoteToGroup() {
        val controller = Controller()

        val note1 = controller.createNote("title1", "content1")
        val note2 = controller.createNote("title2", "content2")
        val note3 = controller.createNote("title3", "content3")
        val note4 = controller.createNote("title4", "content4")

        val group1 = controller.createGroup("group1")
        var notes = controller.getGroupByName("group1")?.notes
        assertEquals(0, notes?.size)

        controller.addNoteToGroup("group1", note1)
        notes = controller.getGroupByName("group1")?.notes
        assertEquals(1, notes?.size)

        controller.addNoteToGroup("group1", note2)
        notes = controller.getGroupByName("group1")?.notes
        assertEquals(2, notes?.size)

        controller.addNoteToGroup("group1", note3)
        notes = controller.getGroupByName("group1")?.notes
        assertEquals(3, notes?.size)

        controller.addNoteToGroup("group1", note4)
        notes = controller.getGroupByName("group1")?.notes
        assertEquals(4, notes?.size)
    }

    @Test
    fun addNotesToGroup() {
        val controller = Controller()

        val note1 = controller.createNote("title1", "content1")
        val note2 = controller.createNote("title2", "content2")
        val note3 = controller.createNote("title3", "content3")
        val note4 = controller.createNote("title4", "content4")
        val notes = controller.getAllNotes()

        val group1 = controller.createGroup("group1")
        var notesInGroup = controller.getGroupByName("group1")?.notes
        assertEquals(0, notesInGroup?.size)

        controller.addNotesToGroup("group1", notes)
        assertEquals(4, notesInGroup?.size)
    }

    @Test
    fun removeNoteFromGroup() {
        val controller = Controller()

        val note1 = controller.createNote("title1", "content1")
        val note2 = controller.createNote("title2", "content2")
        val note3 = controller.createNote("title3", "content3")
        val note4 = controller.createNote("title4", "content4")
        val notes = controller.getAllNotes()

        val group1 = controller.createGroup("group1", notes)
        var notesInGroup = controller.getGroupByName("group1")?.notes
        assertEquals(4, notesInGroup?.size)

        controller.removeNoteFromGroup("group1", note1)
        notesInGroup = controller.getGroupByName("group1")?.notes
        assertEquals(3, notesInGroup?.size)

        controller.removeNoteFromGroup("group1", note2)
        notesInGroup = controller.getGroupByName("group1")?.notes
        assertEquals(2, notesInGroup?.size)

        controller.removeNoteFromGroup("group1", note3)
        notesInGroup = controller.getGroupByName("group1")?.notes
        assertEquals(1, notesInGroup?.size)

        controller.removeNoteFromGroup("group1", note4)
        notesInGroup = controller.getGroupByName("group1")?.notes
        assertEquals(0, notesInGroup?.size)
    }

    @Test
    fun removeNotesFromGroup() {
        val controller = Controller()

        val note1 = controller.createNote("title1", "content1")
        val note2 = controller.createNote("title2", "content2")
        val note3 = controller.createNote("title3", "content3")
        val note4 = controller.createNote("title4", "content4")
        val notes = controller.getAllNotes()

        val group1 = controller.createGroup("group1", notes)
        var notesInGroup = controller.getGroupByName("group1")?.notes
        assertEquals(4, notesInGroup?.size)

        controller.removeNotesFromGroup("group1", notes)
        controller.getGroupByName("group1")?.notes
        assertEquals(0, notesInGroup?.size)
    }

    @Test
    fun moveNoteBetweenGroups() {
        val controller = Controller()

        val note1 = controller.createNote("title1", "content1")
        val note2 = controller.createNote("title2", "content2")
        val note3 = controller.createNote("title3", "content3")
        var notesGroup1 = controller.getAllNotes()
        val note4 = controller.createNote("title4", "content4")
        var notesGroup2 = controller.getAllNotes()

        val group1 = controller.createGroup("group1", notesGroup1)
        val group2 = controller.createGroup("group2", notesGroup2)
        var notesInGroup1 = controller.getGroupByName("group1")?.notes
        var notesInGroup2 = controller.getGroupByName("group2")?.notes
        assertEquals(3, notesInGroup1?.size)
        assertEquals(1, notesInGroup2?.size)

        controller.moveNoteBetweenGroups("group1", "group2", note1)
        notesInGroup1 = controller.getGroupByName("group1")?.notes
        notesInGroup2 = controller.getGroupByName("group2")?.notes
        assertEquals(2, notesInGroup1?.size)
        assertEquals(2, notesInGroup2?.size)

        controller.moveNoteBetweenGroups("group1", "group2", note2)
        notesInGroup1 = controller.getGroupByName("group1")?.notes
        notesInGroup2 = controller.getGroupByName("group2")?.notes
        assertEquals(1, notesInGroup1?.size)
        assertEquals(3, notesInGroup2?.size)

        controller.moveNoteBetweenGroups("group1", "group2", note3)
        notesInGroup1 = controller.getGroupByName("group1")?.notes
        notesInGroup2 = controller.getGroupByName("group2")?.notes
        assertEquals(0, notesInGroup1?.size)
        assertEquals(4, notesInGroup2?.size)
    }

    @Test
    fun moveNotesBetweenGroups() {
        val controller = Controller()

        val note1 = controller.createNote("title1", "content1")
        val note2 = controller.createNote("title2", "content2")
        val note3 = controller.createNote("title3", "content3")
        var notesGroup1 = controller.getAllNotes()
        val note4 = controller.createNote("title4", "content4")
        var notesGroup2 = controller.getAllNotes()

        val group1 = controller.createGroup("group1", notesGroup1)
        val group2 = controller.createGroup("group2", notesGroup2)
        var notesInGroup1 = controller.getGroupByName("group1")?.notes
        var notesInGroup2 = controller.getGroupByName("group2")?.notes
        assertEquals(3, notesInGroup1?.size)
        assertEquals(1, notesInGroup2?.size)

        controller.moveNotesBetweenGroups("group1", "group2", notesGroup1)
        notesInGroup1 = controller.getGroupByName("group1")?.notes
        notesInGroup2 = controller.getGroupByName("group2")?.notes
        assertEquals(0, notesInGroup1?.size)
        assertEquals(4, notesInGroup2?.size)
    }

    @Test
    fun getAllNotes() {
        val controller = Controller()
        var expectedNotesSize = 0
        assertEquals(expectedNotesSize, controller.getAllNotes().size)

        controller.createNote("title", "content")
        var allNotes = controller.getAllNotes()
        var expectedTitle = "title"
        var expectedContent = "content"
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
    fun getNoteByDateCreated() {
        val controller = Controller()
        var note1 = controller.createNote("n1", "c1")
        assertEquals(note1, controller.getNoteByDateCreated(note1.dateCreated))

        controller.createNote("n2", "c2")
        assertEquals(note1, controller.getNoteByDateCreated(note1.dateCreated))
    }

    @Test
    fun getNotesByTitle() {
        val controller = Controller()
        var expectedSize = 0
        assertEquals(expectedSize, controller.getNotesByTitle("title").size)

        controller.createNote("title", "content1")
        controller.createNote("t", "content2")
        expectedSize = 1
        var expectedTitle = "title"
        var expectedContent = "content1"
        var notesTitledTitle = controller.getNotesByTitle("title")
        assertEquals(expectedSize, notesTitledTitle.size)
        assertEquals(expectedTitle, notesTitledTitle[0].title)
        assertEquals(expectedContent, notesTitledTitle[0].content)

        controller.createNote("title", "content3")
        expectedSize = 2
        var expectedContent2 = "content3"
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
        var expectedSize = 5
        var sortedNote = controller.getSortedNotesByTitleAscending()
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
        var expectedSize = 5
        var sortedNote = controller.getSortedNotesByTitleDescending()
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
        controller.createNote("2")
        controller.createNote("3")
        controller.createNote("4")
        val note5 = controller.createNote("5")
        var expectedSize = 5
        var sortedNote = controller.getSortedNotesByModifiedDateAscending()
        assertEquals(expectedSize, sortedNote.size)
        assertEquals("1", sortedNote[0].title)
        assertEquals("2", sortedNote[1].title)
        assertEquals("3", sortedNote[2].title)
        assertEquals("4", sortedNote[3].title)
        assertEquals("5", sortedNote[4].title)

        controller.editNoteContent(note5.dateCreated, "content")
        expectedSize = 5
        sortedNote = controller.getSortedNotesByModifiedDateAscending()
        assertEquals(expectedSize, sortedNote.size)
        assertEquals("1", sortedNote[0].title)
        assertEquals("2", sortedNote[1].title)
        assertEquals("3", sortedNote[2].title)
        assertEquals("4", sortedNote[3].title)
        assertEquals("5", sortedNote[4].title)

        controller.editNoteContent(note1.dateCreated, "content")
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
        controller.createNote("2")
        controller.createNote("3")
        controller.createNote("4")
        val note5 = controller.createNote("5")
        var expectedSize = 5
        var sortedNote = controller.getSortedNotesByModifiedDateDescending()
        assertEquals(expectedSize, sortedNote.size)
        assertEquals("1", sortedNote[4].title)
        assertEquals("2", sortedNote[3].title)
        assertEquals("3", sortedNote[2].title)
        assertEquals("4", sortedNote[1].title)
        assertEquals("5", sortedNote[0].title)

        controller.editNoteContent(note5.dateCreated, "content")
        expectedSize = 5
        sortedNote = controller.getSortedNotesByModifiedDateDescending()
        assertEquals(expectedSize, sortedNote.size)
        assertEquals("1", sortedNote[4].title)
        assertEquals("2", sortedNote[3].title)
        assertEquals("3", sortedNote[2].title)
        assertEquals("4", sortedNote[1].title)
        assertEquals("5", sortedNote[0].title)

        controller.editNoteContent(note1.dateCreated, "content")
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
        controller.createNote("2")
        controller.createNote("3")
        controller.createNote("4")
        controller.createNote("5")
        var expectedSize = 5
        var sortedNote = controller.getSortedNotesByCreatedDateAscending()
        assertEquals(expectedSize, sortedNote.size)
        assertEquals("1", sortedNote[0].title)
        assertEquals("2", sortedNote[1].title)
        assertEquals("3", sortedNote[2].title)
        assertEquals("4", sortedNote[3].title)
        assertEquals("5", sortedNote[4].title)

        controller.editNoteContent(note1.dateCreated, "content")
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
        controller.createNote("2")
        controller.createNote("3")
        controller.createNote("4")
        val note5 = controller.createNote("5")
        var expectedSize = 5
        var sortedNote = controller.getSortedNotesByCreatedDateDescending()
        assertEquals(expectedSize, sortedNote.size)
        assertEquals("1", sortedNote[4].title)
        assertEquals("2", sortedNote[3].title)
        assertEquals("3", sortedNote[2].title)
        assertEquals("4", sortedNote[1].title)
        assertEquals("5", sortedNote[0].title)

        controller.editNoteContent(note5.dateCreated, "content")
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