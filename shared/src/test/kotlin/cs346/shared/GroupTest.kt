package cs346.shared

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class GroupTest {

    @Test
    fun constructor() {
        val group1 = Group("group1")
        var expectedTitle1 = "group1"
        assertEquals(expectedTitle1, group1.name)

        val group2 = Group("group2")
        var expectedTitle2 = "group2"
        group2.notes.add(UUID.randomUUID())
        val expectedNotesSize2 = 1
        assertEquals(expectedTitle2, group2.name)
        assertEquals(expectedNotesSize2, group2.notes.size)

        val group2Copy = Group(group2)
        assertEquals(group2.name, group2Copy.name)
        assertEquals(group2.notes.size, group2Copy.notes.size)
    }

    @Test
    fun getNotes() {
        val group = Group("group")
        val id1 = UUID.randomUUID()
        val id2 = UUID.randomUUID()
        val id3 = UUID.randomUUID()
        group.notes.add(id1)
        group.notes.add(id2)
        group.notes.add(id3)
        val expectedSize = 3

        val groupNotes = group.getNotes()
        assertEquals(expectedSize, groupNotes.size)
        assert(groupNotes.contains(id1))
        assert(groupNotes.contains(id2))
        assert(groupNotes.contains(id3))
    }

    @Test
    fun groupToString() {
        val group1 = Group("name")
        val expectedName = "name"

        assertEquals(expectedName, group1.toString())
    }
}