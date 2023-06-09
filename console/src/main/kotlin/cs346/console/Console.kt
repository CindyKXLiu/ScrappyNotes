package cs346.console

import cs346.shared.*
import java.util.*
import kotlin.collections.HashMap

private const val PAD = 45
private const val PAD_SMALL = 25
private const val HELP_MSG = "OPTIONS:\n" +
        "ls, list                   List all notes\n" +
        "ls, list -g                List all groups\n" +
        "n, new [title]             Create new empty note with title [title]\n" +
        "n, new -g [group]          Create new group named [group]\n" +
        "d, delete [noteID]         Delete note with id [noteID]\n" +
        "d, delete -g [group]       Delete group with name [group]\n" +
        "o, open [noteID]           Opens the note with id [noteID] for viewing and editing\n" +
        "rename [noteID] [new]      Rename the title of the note with id [noteID] to [new]\n" +
        "rename -g [old] [new]      Rename group from [old] to [new]\n" +
        "add [noteID] [group]       Add note with id [noteID] to [group]\n" +
        "rm [noteID] [group]        Remove note with id [noteID] from [group]\n" +
        "mv [noteID] [newGroup]     Moves note with id [noteID] to [newGroup]\n" +
        "undo                       Undo previous action\n" +
        "redo                       Redo previous action\n" +
        "save                       Save state to databases (this action can not be undone/redone)\n" +
        "update                     Update the state (this action can not be undone/redone)\n" +
        "h, help                    Print this message\n" +
        "quit                       Exit\n"
private const val INVALID_COMMAND_MSG = "Invalid command. Type \"help\" for all options.\n"
private const val COMMAND_PROMPT_MSG = "\nEnter your command: "
private const val INVALID_ID_MSG = "No note with such id. Type \"ls\" to get the ids of all notes.\n"
private const val INVALID_GROUP_MSG = "No such group exists. Type \"ls -g\" to get the names of all groups.\n"
private const val DUPLICATE_GROUP_MSG = "A group with that name already exists. Type \"ls -g\" to get the names of all groups.\n"
private const val INVALID_ACTION_MSG = "Invalid action.\n"

/**
 * This class is for the console application
 *
 * @property model is the name of the model that is going to be used to interact with the backend
 *
 * @constructor creates and starts the console application
 */
class Console {
    private val model = Model()
    private val noteEditorLauncher = NoteEditorLauncher()
    private var nextID = 0
    private val aliasIds = HashMap<Int, UUID>()

    /**
     * Prompts user to enter commands until the quit command is entered
     */
    init {
        // populate alias_id
        resetAliasId()

        // start prompting for user input
        while (true) {
            print(COMMAND_PROMPT_MSG)
            val input = readlnOrNull() ?: break
            parseArgs(input)
        }
    }

    /**
     * Processes command passed through the console
     */
    fun parseArgs(command: String) {
        val args = command.split("\\s".toRegex())

        when (args.first()) {
            "ls", "list" -> { // List command
                if (args.size == 1) { // List all notes
                    listNotes()
                } else if (args.size == 2 && args[1] == "-g") { // List all groups
                    listGroups()
                } else {
                    print(INVALID_COMMAND_MSG)
                }
            }

            "n", "new" -> { // Create command
                if (args.size == 2 && !Regex("^-").containsMatchIn(args[1])) { // Create new note
                    createNewNote(args[1])
                } else if (args.size == 3 && args[1] == "-g") { // Create new group
                    createNewGroup(args[2])
                } else {
                    print(INVALID_COMMAND_MSG)
                }
            }

            "d", "delete" -> { // Delete command
                if (args.size == 2) { // Delete note
                    try {
                        deleteNote(args[1].toInt())
                    } catch (e: NumberFormatException) {
                        print(INVALID_ID_MSG)
                    }
                } else if (args.size == 3 && args[1] == "-g") { // Delete group
                    deleteGroup(args[2])
                } else {
                    print(INVALID_COMMAND_MSG)
                }
            }

            "o", "open" -> { // Open note
                if (args.size == 2) {
                    try {
                        openNote(args[1].toInt())
                    } catch (e: NumberFormatException) {
                        print(INVALID_ID_MSG)
                    }
                } else {
                    print(INVALID_COMMAND_MSG)
                }
            }

            "rename" -> { // Rename command
                if (args.size == 3) { // Rename note
                    try {
                        renameNote(args[1].toInt(), args[2])
                    } catch (e: NumberFormatException) {
                        print(INVALID_ID_MSG)
                    }
                } else if (args.size == 4 && args[1] == "-g") { // Rename group
                    renameGroup(args[2], args[3])
                } else {
                    print(INVALID_COMMAND_MSG)
                }
            }

            "add" -> { // Add note to group
                if (args.size == 3) {
                    try {
                        addNoteToGroup(args[1].toInt(), args[2])
                    } catch (e: NumberFormatException) {
                        print(INVALID_ID_MSG)
                    }
                } else {
                    print(INVALID_COMMAND_MSG)
                }
            }

            "rm" -> { // Remove note from group
                if (args.size == 3) {
                    try {
                        removeNoteFromGroup(args[1].toInt(), args[2])
                    } catch (e: NumberFormatException) {
                        print(INVALID_ID_MSG)
                    }
                } else {
                    print(INVALID_COMMAND_MSG)
                }
            }

            "mv" -> { // Move note to new group
                if (args.size == 3) {
                    try {
                        moveNoteToGroup(args[1].toInt(), args[2])
                    } catch (e: NumberFormatException) {
                        print(INVALID_ID_MSG)
                    }
                } else {
                    print(INVALID_COMMAND_MSG)
                }
            }

            "undo" -> { // Undo previous action
                if (args.size == 1) {
                    undoAction()
                } else {
                    print(INVALID_COMMAND_MSG)
                }
            }

            "redo" -> { // Redo previous action
                if (args.size == 1) {
                    redoAction()
                } else {
                    print(INVALID_COMMAND_MSG)
                }
            }

            "save" -> {
                if (args.size == 1) {
                    save()
                } else {
                    print(INVALID_COMMAND_MSG)
                }
            }

            "update" -> {
                if (args.size == 1) {
                    update()
                } else {
                    print(INVALID_COMMAND_MSG)
                }
            }

            // Print help message
            "h", "help" -> print(HELP_MSG)

            // Quit
            "quit" -> quit()

            else -> print(INVALID_COMMAND_MSG)
        }
    }

    /**
     * Clears alias id and remap each note in model with an alias id
     */
    private fun resetAliasId() {
        // reset alias id and counter
        nextID = 0
        aliasIds.clear()

        // populate alias id and counter
        val notes = model.getAllNotes()
        for (note in notes) {
            aliasIds[nextID] = note.id
            ++nextID
        }
    }
    /**
     * Returns the UUID of the note with alias ID [aliasId],
     *      if no such note exist, print error message and null is returned
     *
     * @param aliasID the alias id of the note
     * @return returns the UUID of the note, null if note does not exist
     */
    private fun getUUID(aliasId: Int): UUID? {
        if (aliasIds.containsKey(aliasId)) {
            return aliasIds[aliasId]!!
        }

        print(INVALID_ID_MSG)
        return null
    }

    /**
     * Opens a rich editor with titled [title] containing [content]
     *
     * @param title the title of the note editor (this should be set to the note title)
     * @param content the content of the note editor (this should be set to the note content)
     *
     * @return returns the content of the note editor upon closing
     */
    private fun launchNoteEditor(title: String, content: String = ""): String {

        // Set editor settings
        NoteEditorLauncher.Setting.active = true
        NoteEditorLauncher.Setting.noteTitle = title
        NoteEditorLauncher.Setting.noteContent = content

        noteEditorLauncher.launch()

        // wait until user finishes editing note/close editor
        while (NoteEditorLauncher.Setting.active) Thread.sleep(1000)

        // return edited content
        return NoteEditorLauncher.Setting.noteContent
    }

    /**
     * Prints a nicely formatted list of notes to console with their titles, creation, and modification dates
     */
    private fun listNotes() {
        val notes = model.getAllNotes()

        // print heading
        println("NUMBER OF NOTES: ${notes.size}")
        println("TITLE".padEnd(PAD) + "ID".padEnd(PAD_SMALL) +
                "DATE CREATED".padEnd(PAD_SMALL) +
                "DATE MODIFIED"
        )

        // print all existing notes
        for (note in notes) {
            println(note.title.padEnd(PAD) +
                    aliasIds.entries.find { it.value == note.id }!!.key.toString().padEnd(PAD_SMALL)+
                    note.dateCreated.toString().split("T")[0].padEnd(PAD_SMALL) +
                    note.dateModified.toString().split("T")[0]
            )
        }
    }

    /**
     * Prints a nicely formatted list of groups to console with their notes
     */
    private fun listGroups() {
        val groups = model.getAllGroups()

        // print heading
        println("NUMBER OF GROUPS: ${groups.size}")
        println("GROUP NAME".padEnd(PAD) +
                "NOTE ID".padEnd(PAD_SMALL) +
                "NOTE TITLE")

        // print all existing groups and the notes in that group
        for (group in groups) {
            println(group.name)

            // print the notes that are in the group
            for (noteID in group.getNotes()) {
                val note = model.getNoteByID(noteID)
                println("".padEnd(PAD) +
                        aliasIds.entries.find { it.value == note.id }!!.key.toString().padEnd(PAD_SMALL) +
                        note.title)
            }
        }
    }

    /**
     * Creates a new note titled [title]
     *
     * @param title is the title of the new note created
     */
    private fun createNewNote(title: String) {
        val noteContent = launchNoteEditor(title)
        val note = model.createNote(title, noteContent)
        aliasIds[nextID] = note.id
        ++nextID
        println("!Created new note \"$title\".")
    }

    /**
     * Creates a new group named [name]
     *
     * @param name is the name of the new group created
     */
    private fun createNewGroup(name: String) {
        try {
            model.createGroup(name)
            println("Created new group \"$name\".")
        } catch (e: DuplicateGroupException) {
            print(DUPLICATE_GROUP_MSG)
        }

    }

    /**
     * Deletes the note with the alias id [id]
     *
     * @param id is the alias id of the note to be deleted
     */
    private fun deleteNote(id: Int) {
        val uuid = getUUID(id) ?: return

        try{
            model.deleteNote(uuid)
            aliasIds.remove(id)
            println("Deleted note with id $id.")
        } catch (e: NonExistentNoteException) {
            print(INVALID_ID_MSG)
        }
    }

    /**
     * Deletes the group named [name]
     *
     * @param name is the name of the group to be deleted
     */
    private fun deleteGroup(name: String) {
        try{
            model.deleteGroup(name)
            println("Deleted group \"$name\".")
        } catch (e: NonExistentGroupException) {
            print(INVALID_GROUP_MSG)
        }
    }

    /**
     * Opens an editor containing the content of the note with alias id [id]
     *
     * @param id is the alias id of the note
     */
    private fun openNote(id: Int) {
        val uuid = getUUID(id) ?: return

        try {
            val note = model.getNoteByID(uuid)
            val newContent = launchNoteEditor(note.title, note.content)
            model.editNoteContent(note.id, newContent)
        } catch (e: NonExistentNoteException) {
            println(INVALID_ID_MSG)
        }
    }

    /**
     * Rename the title of the note with alias id [id] to [newTitle]
     *
     * @param id is the alias id of the note
     * @param newTitle is the new title of the note
     */
    private fun renameNote(id: Int, newTitle: String){
        val uuid = getUUID(id) ?: return

        try{
            model.editNoteTitle(uuid, newTitle)
            println("Renamed note with id $id to \"$newTitle\".")
        } catch (e: NonExistentNoteException) {
            println(INVALID_ID_MSG)
        }
    }

    /**
     * Rename the group [oldName] to [newName]
     *
     * @param oldName is the current name of the group
     * @param newName is the new name of the group
     */
    private fun renameGroup(oldName: String, newName: String){
        try{
            model.editGroupName(oldName, newName)
            println("Renamed group \"$oldName\" to \"$newName\".")
        } catch (e: NonExistentGroupException) {
            print(INVALID_GROUP_MSG)
        } catch (e: DuplicateGroupException) {
            print(DUPLICATE_GROUP_MSG)
        }
    }


    /**
     * Adds the note with alias id [id] to the group [groupName]
     *
     * @param id is the alias id of the note to be added
     * @param groupName is the name of the group to add the note to
     */
    private fun addNoteToGroup(id: Int, groupName: String) {
        val uuid = getUUID(id) ?: return

        try{
            model.addNoteToGroup(groupName, uuid)
            println("Added note with id $id to group \"$groupName\".")
        } catch (e: NonExistentNoteException) {
            print(INVALID_ID_MSG)
        } catch (e: NonExistentGroupException) {
            print(INVALID_GROUP_MSG)
        }
    }

    /**
     * Removes the notes with alias id [id] from the group [groupName]
     *
     * @param id is the alias id of the note to be removed from [groupName]
     * @param groupName is the name of the group
     */
    private fun removeNoteFromGroup(id: Int, groupName: String) {
        val uuid = getUUID(id) ?: return

        try {
            model.removeNoteFromGroup(groupName, uuid)
            println("Removed note with id $id from group \"$groupName\".")
        } catch (e: NonExistentNoteException) {
            print(INVALID_ID_MSG)
        } catch (e: NonExistentGroupException) {
            print(INVALID_GROUP_MSG)
        }
    }

    /**
     * Move the note with alias id [id] to the group [newGroup]
     *
     * @param id is the alias id of the note
     * @param newGroup is the name of the group the note will be moved to
     */
    private fun moveNoteToGroup(id: Int, newGroup: String){
        val uuid = getUUID(id) ?: return

        try{
            model.moveNoteToGroup(newGroup, uuid)
            println("Moved note with id $id to group \"$newGroup\"")
        } catch (e: NonExistentNoteException) {
            print(INVALID_ID_MSG)
        } catch (e: NonExistentGroupException) {
            print(INVALID_GROUP_MSG)
        }
    }

    /**
     * Undo previous action
     */
    private fun undoAction() {
        try {
            model.undo()
            println("Undid previous action")
        } catch (e: NoUndoException) {
            print(INVALID_ACTION_MSG)
        }
    }

    /**
     * Redo previous action
     */
    private fun redoAction() {
        try {
            model.redo()
            println("Restored previous action")
        } catch (e: NoRedoException) {
            print(INVALID_ACTION_MSG)
        }
    }

    /**
     * Save the current model state to databases
     */
    private fun save() {
        model.saveToDatabase()
    }

    /**
     * Updates the model state with the synced state
     */
    private fun update() {
        model.updateDatabase()
        resetAliasId()
    }
    /**
     * Saves the data to database and closes console app
     */
    private fun quit() {
        model.saveToDatabase()
        kotlin.system.exitProcess(0)
    }
}
