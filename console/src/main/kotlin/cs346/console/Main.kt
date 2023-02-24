package cs346.console

import cs346.shared.*

private const val PAD = 45
private const val HELP_MSG = "OPTIONS:\n" +
        "ls, list                   List all notes\n" +
        "ls, list -g                List all groups\n" +
        "n, new [name]              Create new empty note with title [name]\n" +
        "d, delete [name]           Delete note with title [name]\n" +
        "p, print [name]            Print contents of note [name]\n" +
        "rename [old] [new]         Rename note from [old] to [new]\n" +
        "rename -g [old] [new]      Rename group from [old] to [new]\n" +
        "n, new -g [group]          Create new group named [group]\n" +
        "d, delete -g [group]       Delete group with name [group]\n" +
        "add [note] [group]         Add [note] to [group]\n" +
        "rm [note] [group]          Remove [note] from [group]\n" +
        "h, help                    Print this message\n" +
        "quit                       Exit"
private const val INVALID_COMMAND_MSG = "Invalid command. Type \"help\" for all options."
private const val COMMAND_PROMPT_MSG = "\nEnter your command: "

fun main() {
   Console()
}

/**
 * This class is for the console application
 *
 * @property controller is the name of the Controller that is going to be used to interact with the backend
 *
 * @constructor creates and starts the console application
 */
class Console {
   private val controller = Controller()

   /**
    * Prompts user to enter commands until the quit command is entered
    */
   init {
      while (true) {
         print(COMMAND_PROMPT_MSG)
         val input = readlnOrNull() ?: break
         parseArgs(input)
      }
   }

   /**
    * Processes command passed through the console
    */
   private fun parseArgs(command: String) {
      val args = command.split("\\s".toRegex())

      when (args.first()) {
         "n", "new" -> { // Create command
            if (args.size == 2) { // Create new note
               createNewNote(args[1])
            } else if (args.size == 3 && args[1] == "-g") { // Create new group
               createNewGroup(args[2])
            } else {
               println(INVALID_COMMAND_MSG)
            }
         }

         "d", "delete" -> { // Delete command
            if (args.size == 2) { // Delete note
               deleteNote(args[1])
            } else if (args.size == 3 && args[1] == "-g") { // Delete group
               deleteGroup(args[2])
            } else {
               print(INVALID_COMMAND_MSG)
            }
         }

         "add" -> { // Add note to group
            if (args.size == 3) {
               addNotesToGroup(args[1], args[2])
            } else {
               print(INVALID_COMMAND_MSG)
            }
         }

         "rm" -> { // Remove note from group
            if (args.size == 3) {
               removeNotesFromGroup(args[1], args[2])
            } else {
               print(INVALID_COMMAND_MSG)
            }
         }

         "ls", "list" -> { // List command
            if (args.size == 1) { // List all notes
               listNotes()
            } else if (args.size == 2 && args[1] == "-g") { // List all groups
               listGroups()
            } else {
               print(INVALID_COMMAND_MSG)
            }
         }

         "p", "print" -> { // Print note
            if (args.size == 2) {
               printNoteContent(args[1])
            } else {
               print(INVALID_COMMAND_MSG)
            }
         }

         "rename" -> { // Rename command
            if (args.size == 3) { // Rename note
               renameNote(args[1], args[2])
            } else if (args.size == 4 && args[1] == "-g") { // Rename group
               renameGroup(args[2], args[3])
            } else {
               print(INVALID_COMMAND_MSG)
            }
         }

         // Print help message
         "h", "help" -> println(HELP_MSG)

         // Quit
         "quit" -> kotlin.system.exitProcess(0)

         else -> println(INVALID_COMMAND_MSG)
      }
   }

   /**
    * Creates a new note titled [title]
    *
    * @param title is the title of the new note created
    */
   private fun createNewNote(title: String) {
      controller.createNote(title, "")
      println("Created new note \"$title\".")
   }

   /**
    * Creates a new group named [name]
    *
    * @param name is the name of the new group created
    */
   private fun createNewGroup(name: String) {
      controller.createGroup(name)
      println("Created new group \"$name\".")
   }

   /**
    * Deletes note(s) with the title [title]
    *
    * @param title is the title of the note(s) to be deleted
    */
   private fun deleteNote(title: String) {
      val notes = controller.getNotesByTitle(title)
      for (note in notes) {
         controller.deleteNote(note.id)
      }
      println("Deleted note \"$title\".")
   }

   /**
    * Deletes the group named [name]
    *
    * @param name is the name of the group to be deleted
    */
   private fun deleteGroup(name: String) {
      controller.deleteGroup(name)
      println("Deleted group \"$name\".")
   }

   /**
    * Adds the notes titled [noteTitle] to the group [groupName]
    *
    * @param noteTitle is the title of the note to be added to [groupName]
    * @param groupName is the name of the group
    */
   private fun addNotesToGroup(noteTitle: String, groupName: String) {
      // check that the note exists
      val notes = controller.getNotesByTitle(noteTitle)
      if (notes.isEmpty()) {
         println("No note titled \"$noteTitle\".")
         return
      }

      //check that the group exists
      if (controller.getGroupByName(groupName) == null) {
         println("The group \"$groupName\" does not exist.")
         return
      }

      // add notes to the group
      controller.addNotesToGroup(groupName, notes)
      println("Added \"$noteTitle\" to \"$groupName\".")
   }

   /**
    * Removes the notes titled [noteTitle] from the group [groupName]
    *
    * @param noteTitle is the title of the note to be removed from [groupName]
    * @param groupName is the name of the group
    */
   private fun removeNotesFromGroup(noteTitle: String, groupName: String) {
      // check that the note exists
      val notes = controller.getNotesByTitle(noteTitle)
      if (notes.isEmpty()) {
         println("No note titled \"$noteTitle\".")
         return
      }

      //check that the group exists
      if (controller.getGroupByName(groupName) == null) {
         println("The group \"$groupName\" does not exist.")
         return
      }

      // remove notes from the group
      controller.removeNotesFromGroup(groupName, notes)
      println("Removed \"$noteTitle\" from \"$groupName\".")
   }

   /**
    * Prints a nicely formatted list of notes to console with their titles, creation, and modification dates
    */
   private fun listNotes() {
      val notes = controller.getAllNotes()

      println("NUMBER OF NOTES: ${notes.size}")
      // print heading
      println(
         "TITLE".padEnd(PAD) +
                 "DATE CREATED".padEnd(PAD) +
                 "DATE MODIFIED"
      )
      // print all existing notes
      for (note in notes) {
         println(
            note.title.padEnd(PAD) +
                    note.dateCreated.toString().split("T")[0].padEnd(PAD) +
                    note.dateModified.toString().split("T")[0]
         )
      }
   }

   /**
    * Prints a nicely formatted list of groups to console with their notes
    */
   private fun listGroups() {
      val groups = controller.getAllGroups()

      println("NUMBER OF GROUPS: ${groups.size}")
      // print heading
      println(
         "GROUP NAME".padEnd(PAD) +
                 "NOTES"
      )
      // print all existing groups and the notes in that group
      for (group in groups) {
         println(group.name)

         // print the notes that are in the group
         for ((_, note) in group.notes) {
            println("".padEnd(PAD) + note.title)
         }
      }
   }

   /**
    * Prints the content of the note titled [title]
    *
    * @property title is the title of the note
    */
   private fun printNoteContent(title: String) {
      val notes = controller.getNotesByTitle(title)
      if (notes.isEmpty()) {
         println("Could not find note named \"$title\".")
         return
      }

      println(notes[0].content)
   }

   /**
    * Rename note titled [oldTitle] to [newTitle]
    *
    * @property oldTitle is the current name of the note
    * @property newTitle is the new title of the note
    */
   private fun renameNote(oldTitle: String, newTitle: String){
      val notes = controller.getNotesByTitle(oldTitle)
      if (notes.isEmpty()) {
         println("Could not find note named \"$oldTitle\".")
         return
      }

      for (note in notes) {
         controller.editNoteTitle(note.id, newTitle)
      }
      println("Renamed note \"$oldTitle\" to \"$newTitle\".")
   }

   private fun renameGroup(oldName: String, newName: String){
      val group = controller.getGroupByName(oldName)
      if (group == null) {
         println("Could not find group named \"$oldName\".")
         return
      }

      controller.editGroupName(oldName, newName)
      println("Renamed group \"$oldName\" to \"$newName\".")
   }
}