package cs346.console

import cs346.shared.*

fun main() {
    val controller = Controller()
    // printHelp()
        // TODO Runtime.getRuntime().exec("notepad")
    while (true) {
        println("")
        print("Enter your command: ")
        val input = readLine()
        if (input != null) {
            parseArgs(input, controller)
        }
    }

}

/**
 * Print help message to console.
 */
fun printHelp() {
    println("OPTIONS:")
    println("ls, list                   List all notes")
    println("ls, list -g                List all groups")
    println("ls, list [group]           List all notes under [group]")
    println("n, new [name]              Create new empty note with title [name]")
    println("d, delete [name]           Delete note with title [name]")
    println("p, print [name]            Print contents of note [name]") //TODO
    println("rename [old] [new]         Rename note from [old] to [new]")
    println("rename -g [old] [new]      Rename group from [old] to [new]")
    println("n, new -g [group]          Create new group named [group]")
    println("d, delete -g [group]       Delete group with name [group]")
    println("add [group] [note]         Add [note] to [group]")
    println("rm [group] [note]          Remove [note] from [group]")
    println("h, help                    Print this message")
    println("quit                       Exit")
}

/**
 * Process command passed through console.
 */
fun parseArgs(command: String, controller: Controller) {
    val args = command.split("\\s".toRegex())
    when (args.first()) {
        // Create command
        "n", "new" -> {
            // Create new note
            if (args.size == 2) {
                val title = args[1]
                val note = controller.createNote(title, "")
                println("Created new note \"" + note.title + "\".")
            // Create new group
            } else if (args.size == 3) {
                if (args[1] == "-g") {
                    val groupname = args[2]
                    val group = controller.createGroup(groupname)
                    println("Created new group \"" + group.name + "\".")
                } else {
                    printInvalidCommand()
                }
            } else {
                printInvalidCommand()
            }
        }

        // Delete command
        "d", "delete" -> {
            // Delete note
            if (args.size == 2) {
                val title = args[1]
                val notes = controller.getNotesByTitle(title)
                for (note in notes) {
                    controller.deleteNote(note.id)
                    println("Deleted note \"" + note.title + "\". It's gone!")
                }
            // Delete group
            } else if (args.size == 3) {
                if (args[1] == "-g") {
                    val groupname = args[2]
                    controller.deleteGroup(groupname)
                    println("Deleted group \"$groupname\". It's gone!")
                } else {
                    printInvalidCommand()
                }
            } else {
                printInvalidCommand()
            }
        }

        // Add note to group
        "add" -> {
            if (args.size == 3) {
                // Check if group exists
                val groupname = args[1]
                if (controller.getGroupByName(groupname) == null) {
                    println("Oops! The group \"$groupname\" does not exist.")
                } else {
                    // Check if note exists
                    val notename = args[2]
                    val notes = controller.getNotesByTitle(notename)
                    if (notes.isNotEmpty()) {
                        controller.addNotesToGroup(groupname, notes)
                        println("Added \"$notename\" to \"$groupname\".")
                    } else {
                        println("Could not add \"$notename\" to \"$groupname\".")
                    }
                }
            } else {
                printInvalidCommand()
            }
        }

        // Remove note from group
        "rm" -> {
            if (args.size == 3) {
                // Check if group exists
                val groupname = args[1]
                if (controller.getGroupByName(groupname) == null) {
                    println("Oops! The group \"$groupname\" does not exist.")
                } else {
                    // Check if note exists
                    val notename = args[2]
                    val notes = controller.getNotesByTitle(notename)
                    if (notes.isNotEmpty()) {
                        controller.removeNotesFromGroup(groupname, notes)
                        println("Removed \"$notename\" from \"$groupname\".")
                    } else {
                        println("Could not remove \"$notename\" from \"$groupname\".")
                    }
                }
            } else {
                printInvalidCommand()
            }
        }

        // List command
        "ls", "list" -> {
            // List all notes
            if (args.size == 1) {
                val notes = controller.getAllNotes()
                println("You have " + notes.size + " note${if (notes.size == 1) "" else "s"}:")
                printNotes(notes)
            } else if (args.size == 2) {
                val groups = controller.getAllGroups()

                // List all groups
                if (args[1] == "-g") {
                    printGroups(groups)

                // List all notes under specific group
                } else {
                    val groupname = args[1]
                    val group = controller.getGroupByName(groupname)
                    if (group != null) {
                        val notes = group.notes.values.toList()
                        println("You have " + notes.size + " note${if (notes.size == 1) "" else "s"} under \"$groupname\":")
                        printNotes(notes)
                    } else {
                        println("Oops! The group \"$groupname\" does not exist.")
                        printGroups(groups)
                    }
                }
            } else {
                printInvalidCommand()
            }
        }

        // Print note
        "p", "print" -> {
            if (args.size == 2) {
                // Check if note exists
                val name = args[1]
                val notes = controller.getNotesByTitle(name)
                // If it exists, print its contents
                if (notes.isNotEmpty()) {
                    println("$name:")
                    print(notes[0].content)
                } else {
                    println("Could not find note named \"$name\".")
                }
            } else {
                printInvalidCommand()
            }
        }

        // Rename command
        "rename" -> {
            // Rename note
            if (args.size == 3) {
                // Check if note with old name exists
                val oldname = args[1]
                val notes = controller.getNotesByTitle(oldname)
                // If it exists, rename with new name
                if (notes.isNotEmpty()) {
                    val newname = args[2]
                    for (note in notes) {
                        controller.editNoteTitle(note.id, newname)
                        println("Renamed note \"$oldname\" to \"$newname\".")
                    }
                } else {
                    println("Could not find note named \"$oldname\".")
                }
            // Rename group
            } else if (args.size == 4) {
                if (args[1] == "-g") {
                    // Check if group with old name exists
                    val oldname = args[2]
                    val group = controller.getGroupByName(oldname)
                    // If it exists, rename with new name
                    if (group == null) {
                        println("Could not find group named \"$oldname\".")
                    } else {
                        val newname = args[3]
                        controller.editGroupName(oldname, newname)
                        println("Renamed group \"$oldname\" to \"$newname\".")
                    }
                } else {
                    printInvalidCommand()
                }
            } else {
                printInvalidCommand()
            }
        }

        // Print help message
        "h", "help" -> printHelp()

        // Quit
        "quit" -> kotlin.system.exitProcess(0)

        else -> printInvalidCommand()
    }
}

/**
 * Prints a nicely formatted list of notes to console with their titles, creation, and modification dates.
 */
fun printNotes(notes: List<Note>) {
    val headings = listOf("TITLE", "DATE CREATED", "DATE MODIFIED")
    println("${headings[0].padEnd(45)} ${headings[1].padEnd(20)} ${headings[2]}")

    for (note in notes) {
        println("${note.title.padEnd(45)} " +
                "${note.dateCreated.toString().split("T")[0].padEnd(20)} " +
                note.dateModified.toString().split("T")[0]
        )
    }
}

/**
 * Prints a formatted list of groups to console.
 */
fun printGroups(groups: List<Group>) {
    println("You have " + groups.size + " group${if (groups.size == 1) "" else "s"}:")
    for (group in groups) println("- " + group.name)
}

fun printInvalidCommand() {
    println("Invalid command. Type \"help\" for all options.")
}
