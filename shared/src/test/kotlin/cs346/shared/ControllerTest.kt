package cs346.shared

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

internal class ControllerTest {
    @Test
    fun createNote() {
        val controller = Controller()
        var expectedNotesSize = 0
        assert(controller.getAllNotes().size == expectedNotesSize)

        var note = controller.createNote()
        expectedNotesSize = 1
        var expectedTitle = ""
        var expectedContent = ""
        assert(controller.getAllNotes().size == expectedNotesSize)
        assert(controller.getNoteByDateCreated(note.dateCreated) != null)
        assertEquals(expectedTitle, note.title)
        assertEquals(expectedContent, note.content)

        note = controller.createNote("title")
        expectedNotesSize = 2
        expectedTitle = "title"
        expectedContent = ""
        assert(controller.getAllNotes().size == expectedNotesSize)
        assert(controller.getNoteByDateCreated(note.dateCreated) != null)
        assertEquals(expectedTitle, note.title)
        assertEquals(expectedContent, note.content)

        note = controller.createNote(content = "content")
        expectedNotesSize = 3
        expectedTitle = ""
        expectedContent = "content"
        assert(controller.getAllNotes().size == expectedNotesSize)
        assert(controller.getNoteByDateCreated(note.dateCreated) != null)
        assertEquals(expectedTitle, note.title)
        assertEquals(expectedContent, note.content)

        note = controller.createNote("title", "content")
        expectedNotesSize = 4
        expectedTitle = "title"
        expectedContent = "content"
        assert(controller.getAllNotes().size == expectedNotesSize)
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
        assert(controller.getAllNotes().size == expectedNotesSize)

        controller.deleteNote(Instant.now())
        assert(controller.getAllNotes().size == expectedNotesSize)

        controller.deleteNote(id1)
        expectedNotesSize = 2
        assert(controller.getAllNotes().size == expectedNotesSize)

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