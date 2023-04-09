package cs346.shared

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SortTest {
    @Test
    fun sortByTitle() {
        val note1 = Note("c")
        val note2 = Note("b")
        val note3 = Note("d")
        val note4 = Note("a")

        val noteList: List<Note> = listOf(note1, note2, note3, note4)
        val ascendingSortList = Sort.sortByTitle(noteList, Sort.Order.ASC)

        assertEquals(note4.title, ascendingSortList[0].title)
        assertEquals(note2.title, ascendingSortList[1].title)
        assertEquals(note1.title, ascendingSortList[2].title)
        assertEquals(note3.title, ascendingSortList[3].title)

        val descendingSortList = Sort.sortByTitle(noteList, Sort.Order.DESC)

        assertEquals(note3.title, descendingSortList[0].title)
        assertEquals(note1.title, descendingSortList[1].title)
        assertEquals(note2.title, descendingSortList[2].title)
        assertEquals(note4.title, descendingSortList[3].title)
    }

    @Test
    fun sortByDateModified() {
        val note1 = Note("note1")
        Thread.sleep(1)
        val note2 = Note("note2")
        Thread.sleep(1)
        val note3 = Note("note3")
        Thread.sleep(1)
        val note4 = Note("note4")
        Thread.sleep(1)

        note2.content = "content"
        Thread.sleep(1)
        note4.content = "content"
        Thread.sleep(1)

        val noteList: List<Note> = listOf(note1, note2, note3, note4)
        val ascendingSortList = Sort.sortByDateModified(noteList, Sort.Order.ASC)

        assertEquals(note1.title, ascendingSortList[0].title)
        assertEquals(note3.title, ascendingSortList[1].title)
        assertEquals(note2.title, ascendingSortList[2].title)
        assertEquals(note4.title, ascendingSortList[3].title)

        val descendingSortList = Sort.sortByDateModified(noteList, Sort.Order.DESC)

        assertEquals(note4.title, descendingSortList[0].title)
        assertEquals(note2.title, descendingSortList[1].title)
        assertEquals(note3.title, descendingSortList[2].title)
        assertEquals(note1.title, descendingSortList[3].title)
    }

    @Test
    fun sortByDateCreated() {
        val note4 = Note("note4")
        Thread.sleep(1)
        val note1 = Note("note1")
        Thread.sleep(1)
        val note3 = Note("note3")
        Thread.sleep(1)
        val note2 = Note("note2")
        Thread.sleep(1)

        val noteList: List<Note> = listOf(note1, note2, note3, note4)
        val ascendingSortList = Sort.sortByDateCreated(noteList, Sort.Order.ASC)

        assertEquals(note4.title, ascendingSortList[0].title)
        assertEquals(note1.title, ascendingSortList[1].title)
        assertEquals(note3.title, ascendingSortList[2].title)
        assertEquals(note2.title, ascendingSortList[3].title)

        val descendingSortList = Sort.sortByDateCreated(noteList, Sort.Order.DESC)

        assertEquals(note2.title, descendingSortList[0].title)
        assertEquals(note3.title, descendingSortList[1].title)
        assertEquals(note1.title, descendingSortList[2].title)
        assertEquals(note4.title, descendingSortList[3].title)
    }

    @Test
    fun sortByID() {
        val note1 = Note("note1")
        val note2 = Note("note2")
        val note3 = Note("note3")
        val note4 = Note("note4")

        val noteList: List<Note> = listOf(note1, note2, note3, note4)
        val ascendingSortList = Sort.sortByID(noteList, Sort.Order.ASC)

        assert(ascendingSortList[0].id < ascendingSortList[1].id)
        assert(ascendingSortList[1].id < ascendingSortList[2].id)
        assert(ascendingSortList[2].id < ascendingSortList[3].id)

        val descendingSortList = Sort.sortByID(noteList, Sort.Order.DESC)

        assert(descendingSortList[0].id > descendingSortList[1].id)
        assert(descendingSortList[1].id > descendingSortList[2].id)
        assert(descendingSortList[2].id > descendingSortList[3].id)
    }
}