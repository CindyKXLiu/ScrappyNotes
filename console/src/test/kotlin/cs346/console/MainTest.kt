package cs346.console

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

private const val INVALID_COMMAND_MSG = "Invalid command. Type \"help\" for all options.\n"
private const val COMMAND_PROMPT_MSG = "\nEnter your command: "
private const val INVALID_ID_MSG = "No note with such id. Type \"ls\" to get the ids of all notes.\n"
private const val INVALID_GROUP_MSG = "No such group exists. Type \"ls -g\" to get the names of all groups.\n"
private const val DUPLICATE_GROUP_MSG = "A group with that name already exists. Type \"ls -g\" to get the names of all groups.\n"
private const val INVALID_ACTION_MSG = "Invalid action.\n"

internal class MainTest {
    @Test
    fun startup() {
        var expectedOutput = COMMAND_PROMPT_MSG
        val output = ByteArrayOutputStream()
        System.setOut(PrintStream(output))

        Console()
        assertEquals(expectedOutput, output.toString())
    }

    @Test
    fun invalidCommand() {
        val console = Console()
        val expectedOutput = INVALID_COMMAND_MSG

        // Invalid command
        var input = "invalid cmd"
        val output = ByteArrayOutputStream()
        System.setOut(PrintStream(output))
        console.parseArgs(input)
        assertEquals(expectedOutput, output.toString())

        // Incorrect number of arguments
        input = "mv note"
        output.reset()
        console.parseArgs(input)
        assertEquals(expectedOutput, output.toString())

        // Invalid arguments
        input = "new -r"
        output.reset()
        console.parseArgs(input)
        assertEquals(expectedOutput, output.toString())
    }

    @Test
    fun invalidNoteID() {
        val console = Console()
        val expectedOutput = INVALID_ID_MSG
        val output = ByteArrayOutputStream()
        System.setOut(PrintStream(output))

        // Deleting nonexistent note
        var input = "delete -1"
        console.parseArgs(input)
        assertEquals(expectedOutput, output.toString())

        // Renaming nonexistent note
        input = "rename note notes"
        output.reset()
        console.parseArgs(input)
        assertEquals(expectedOutput, output.toString())

        // Adding nonexistent note to group
        input = "add note group"
        output.reset()
        console.parseArgs(input)
        assertEquals(expectedOutput, output.toString())

        // Removing nonexistent note
        input = "rm note group"
        output.reset()
        console.parseArgs(input)
        assertEquals(expectedOutput, output.toString())

        // Moving nonexistent note
        input = "mv note group"
        output.reset()
        console.parseArgs(input)
        assertEquals(expectedOutput, output.toString())
    }

    @Test
    fun invalidGroupAction() {
        val console = Console()
        val expectedOutput = INVALID_GROUP_MSG
        val output = ByteArrayOutputStream()
        System.setOut(PrintStream(output))

        // Deleting nonexistent group
        var input = "delete -g group"
        console.parseArgs(input)
        assertEquals(expectedOutput, output.toString())

        // Renaming nonexistent group
        input = "rename -g old new"
        output.reset()
        console.parseArgs(input)
        assertEquals(expectedOutput, output.toString())
    }

    @Test
    fun createDuplicateGroup() {
        val console = Console()
        val expectedOutput = DUPLICATE_GROUP_MSG
        val output = ByteArrayOutputStream()
        System.setOut(PrintStream(output))

        var input = "new -g group1"
        console.parseArgs(input)

        input = "new -g group1"
        output.reset()
        console.parseArgs(input)
        assertEquals(expectedOutput, output.toString())
    }

    @Test
    fun renameDuplicateGroup() {
        val console = Console()
        val expectedOutput = DUPLICATE_GROUP_MSG
        val output = ByteArrayOutputStream()
        System.setOut(PrintStream(output))

        var input = "new -g group1"
        console.parseArgs(input)
        input = "new -g group2"
        console.parseArgs(input)

        input = "rename -g group2 group1"
        output.reset()
        console.parseArgs(input)
        assertEquals(expectedOutput, output.toString())
    }

    @Test
    fun invalidAction() {
        val console = Console()
        val expectedOutput = INVALID_ACTION_MSG
        val output = ByteArrayOutputStream()
        System.setOut(PrintStream(output))

        // No redo
        var input = "new -g test"
        console.parseArgs(input)
        input = "redo"
        output.reset()
        console.parseArgs(input)
        assertEquals(expectedOutput, output.toString())
    }
}