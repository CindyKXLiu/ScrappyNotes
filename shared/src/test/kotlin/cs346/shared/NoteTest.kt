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

        // copy constructor
        val note4Copy = Note(note4)
        assertEquals(note4.id, note4Copy.id)
        assertEquals(note4.title, note4Copy.title)
        assertEquals(note4.content, note4Copy.content)
        assertEquals(note4.dateCreated, note4Copy.dateCreated)
        assertEquals(note4.dateModified, note4Copy.dateModified)
        assertEquals(note4.groupName, note4Copy.groupName)
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

    @Test
    fun noteToString() {
        val note1 = Note("title")
        val expectedTitle = "title"

        assertEquals(expectedTitle, note1.toString())
    }
}