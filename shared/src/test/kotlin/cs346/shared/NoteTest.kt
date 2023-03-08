package cs346.shared

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class NoteTest {
    @Test
    fun constructor() {
        val note1 = Note()
        var expectedTitle = ""
        var expectedContent = ""
        assertEquals(expectedTitle, note1.title)
        assertEquals(expectedContent, note1.content)

        val note2 = Note("title")
        expectedTitle = "title"
        expectedContent = ""
        assertEquals(expectedTitle, note2.title)
        assertEquals(expectedContent, note2.content)

        val note3 = Note("title", "content")
        expectedTitle = "title"
        expectedContent = "content"
        assertEquals(expectedTitle, note3.title)
        assertEquals(expectedContent, note3.content)

        val note4 = Note(content = "content")
        expectedTitle = ""
        expectedContent = "content"
        assertEquals(expectedTitle, note4.title)
        assertEquals(expectedContent, note4.content)
    }

    @Test
    fun titleSetter() {
        val note1 = Note()
        val dateModifiedBeforeSetter = note1.dateModified
        val dateCreatedBeforeSetter = note1.dateCreated
        var expectedTitle = ""
        var expectedContent = ""

        assertEquals(expectedTitle, note1.title)
        assertEquals(expectedContent, note1.content)

        Thread.sleep(1)

        note1.title = "title"
        expectedTitle = "title"

        assertEquals(expectedTitle, note1.title)
        assertEquals(expectedContent, note1.content)
        assert(note1.dateCreated == dateCreatedBeforeSetter)
        assert(note1.dateModified > dateModifiedBeforeSetter)
    }

    @Test
    fun contentSetter() {
        val note1 = Note()
        val dateModifiedBeforeSetter = note1.dateModified
        val dateCreatedBeforeSetter = note1.dateCreated
        var expectedTitle = ""
        var expectedContent = ""

        assertEquals(expectedTitle, note1.title)
        assertEquals(expectedContent, note1.content)

        Thread.sleep(1)

        note1.content = "content"
        expectedContent = "content"

        assertEquals(expectedTitle, note1.title)
        assertEquals(expectedContent, note1.content)
        assert(note1.dateCreated == dateCreatedBeforeSetter)
        assert(note1.dateModified > dateModifiedBeforeSetter)
    }
}