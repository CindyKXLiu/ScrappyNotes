package cs346.application

import cs346.shared.*
import javafx.application.Application
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.input.KeyCode
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.util.*


class Main : Application() {
    private val noteview = TreeView<Any>()
    private val textarea = TextArea()
    private val lastmodified = HBox()
    private val layout = BorderPane()
    private val controller = Controller()

    /**
     * NoteSortType used to track what sorting option is currently
     * used by display noteview.
     * ALPHA = alphabetical order, CREATED = date created,
     * MASCENDING = last modified ascending
     * MDESCENDING = last modified descending
     */
    /*
    enum class NoteSortType {
        ALPHA, CREATED, MASCENDING, MDESCENDING, DEFAULT
    }
    private val currentSortType = NoteSortType.DEFAULT */
    override fun start(stage: Stage) {
        /**
         * Set up all drop down menus and functionality for top most bar
         */
        val menuBar = MenuBar()

        // FILE menubar manipulations /////////////////////////////////////////////////////////
        val fileMenu = Menu("File")
        val fileQuit = MenuItem("Quit")
        fileQuit.setOnAction { event -> stage.close() }

        val newNote = MenuItem("New Note (CTRL+N)")
        newNote.setOnAction { event -> createNote() }

        val deleteObject = MenuItem("Delete")
        deleteObject.setOnAction { event -> deleteSelectedNote() }

        val newGroup = MenuItem("New Group (CTRL+G)")
        newGroup.setOnAction { event -> createGroup() }

        fileMenu.items.addAll(fileQuit, newNote, newGroup, deleteObject)
        menuBar.menus.add(fileMenu)

        // ACTIONS menubar manipulations ////////////////////////////////////////////////////////
        val actionsMenu = Menu("Actions")

        val actionsRename = MenuItem("Rename")
        actionsRename.setOnAction { _ -> renameSelectedNote() }

        val actionsGroup = MenuItem("Add to Group")
        actionsGroup.setOnAction { _ ->
            addSelectedNoteToGroup()
        }

        val actionsRemove = MenuItem("Remove from Group")
        actionsRemove.setOnAction { _ ->
            removeSelectedNoteFromGroup()
        }

        val actionsUndo = MenuItem("Undo (CTRL+Z)")
        actionsUndo.setOnAction { _ -> undo()
        }

        val actionsRedo = MenuItem("Redo (CTRL+Y)")
        actionsRedo.setOnAction { _ -> redo()
        }

        actionsMenu.items.addAll(actionsRename, actionsGroup, actionsRemove, actionsUndo,
            actionsRedo)
        menuBar.menus.add(actionsMenu)

        // OPTIONS menubar manipulations ///////////////////////////////////////////////////////////
        val optionsMenu = Menu("Options")
        val optionsTheme = CheckMenuItem("Select Theme")
        optionsTheme.setOnAction { event -> }

        optionsMenu.items.add(optionsTheme)
        menuBar.menus.add(optionsMenu)

        /**
         * Set up Bottom Panel displaying last modification date of current file
         */
        lastmodified.padding = Insets(3.0, 5.0, 3.0, 5.0)
        lastmodified.children.add(Label(""))

        /**
         * Set up for left side note list display
         */
        updateNoteview(null)
        noteview.setShowRoot(false)
        noteview.selectionModel.selectedItemProperty().addListener { observable, oldValue, newValue ->
            val currSelection = noteview.selectionModel.selectedItem
            if (currSelection != null)
            {
                if (currSelection.value is Note) {
                    displayNoteContents(currSelection.value as Note)
                    textarea.disableProperty().set(false)
                } else if (currSelection.value is Group) {
                    textarea.text = ""
                    textarea.disableProperty().set(true)
                }
            }
        }

        noteview.setOnKeyPressed { event ->
            if (event.code == KeyCode.DELETE || event.code == KeyCode.BACK_SPACE) {
                deleteSelectedNote()
            }
        }

        /**
         * Set up for left side search bar
         */
        val searchbox = TextField()
        searchbox.promptText = "search"
        searchbox.setOnKeyPressed { event ->
            if (event.code == KeyCode.ENTER) {
                searchNotes(searchbox.text)
            }
        }

        val leftside = VBox()
        leftside.spacing = 0.0
        VBox.setVgrow(noteview, Priority.ALWAYS)
        leftside.children.addAll(searchbox, noteview)

        /**
         * Set up for focus area text and toolbar
         */
        val mainarea = VBox()

        val texttools = HBox()
        val boldButton = Button("Bold")
        boldButton.setOnAction { actionEvent: ActionEvent -> }

        val italicizeButton = Button("Italics")
        italicizeButton.setOnAction { actionEvent: ActionEvent -> }

        val underlineButton = Button("Underline")
        underlineButton.setOnAction { }

        val saveButton = Button("Save Note")
        saveButton.setOnAction { actionEvent: ActionEvent -> saveSelectedNote() }

        texttools.spacing = 7.0
        texttools.padding = Insets(3.0, 5.0, 3.0, 5.0)
        texttools.children.addAll(boldButton, italicizeButton, underlineButton, saveButton)
        VBox.setVgrow(textarea, Priority.ALWAYS)
        textarea.focusTraversableProperty().set(false)
        textarea.text = ""
        textarea.disableProperty().set(true)

        mainarea.children.addAll(texttools, textarea)

        /**
         * Add all panels to scene and show
         */
        layout.top = menuBar
        layout.left = leftside
        layout.center = mainarea
        layout.bottom = lastmodified

        val scene = Scene(layout)

        /**
         * Set up hotkeys for app
         */
        scene.setOnKeyPressed { event ->
            if (event.isControlDown) {
                when (event.code) {
                    KeyCode.N -> createNote()
                    KeyCode.Z -> undo()
                    KeyCode.Y -> redo()
                    KeyCode.S -> saveSelectedNote()
                    KeyCode.G -> createGroup()
                    else -> {}
                }
            }
        }

        stage.minWidth = 400.0
        stage.minHeight = 300.0
        stage.width = 800.0
        stage.height = 600.0
        stage.scene = scene
        stage.isResizable = true
        stage.title = "Notes Application"
        stage.show()
    }

    private fun updateNoteview(listofnotes : List<Note>? = controller.getAllUngroupedNotes(), selectedNote : Note? = null,
                               listofgroups : List<Group>? = controller.getAllGroups())
    {
        val rootitem = TreeItem<Any>()

        if (listofnotes != null)
        {
            noteview.selectionModel.clearSelection()
            var treeitemofnote : TreeItem<Any>? = null
            listofnotes.forEachIndexed { index, note ->
                val newitem = TreeItem<Any>(note)
                rootitem.children.add(newitem)
                if (selectedNote != null && selectedNote == note) {
                    treeitemofnote = newitem
                }
            }
            if (selectedNote != null) {
                if (treeitemofnote != null) {
                    noteview.selectionModel.select(treeitemofnote)
                }
            } else {
                textarea.text = null
                textarea.disableProperty().set(true)
            }
        }

        if (listofgroups != null) {
            listofgroups.forEachIndexed { index, group ->
                val newgroup = TreeItem<Any>(group)

                for (note in group.getNotes()) {
                    newgroup.children.add(TreeItem(controller.getNoteByID(note)))
                }
                rootitem.children.add(newgroup)
            }
        }
        noteview.root = rootitem
    }

    private fun createNote()
    {
        val td = TextInputDialog("Name for Note")
        td.headerText = "Create a new Note"
        val result: Optional<String> = td.showAndWait()
        if (result.isPresent) {
            val newnote = controller.createNote(result.get(),"blank")
            textarea.disableProperty().set(false)
            displayNoteContents(newnote)
            updateNoteview(controller.getAllUngroupedNotes(), newnote)
        }
    }

    private fun deleteSelectedNote()
    {
        val currSelection = noteview.selectionModel.selectedItem ?: return

        if (currSelection.value is Note) {
            val currNote = currSelection.value as Note
            val alert = Alert(AlertType.CONFIRMATION)
            alert.title = "Warning: Delete?"
            alert.headerText = "Are you sure you want to delete the note ${currNote.title}"
            alert.contentText = "This cannot be undone!"

            val result = alert.showAndWait()
            if (result.get() == ButtonType.OK) {
                controller.deleteNote(currNote.id)
                textarea.text = null
                textarea.disableProperty().set(true)
                currSelection.parent.children.remove(currSelection)
                updateNoteview(controller.getAllUngroupedNotes(), null,
                    controller.getAllGroups())
            } else {
                alert.close()
            }
        } else if (currSelection.value is Group) {
            val currGroup = currSelection.value as Group
            val alert = Alert(AlertType.CONFIRMATION)
            alert.title = "Warning: Delete?"
            alert.headerText = "Are you sure you want to delete the group ${currGroup.name}"
            alert.contentText = "This cannot be undone!"

            val result = alert.showAndWait()
            if (result.get() == ButtonType.OK) {
                controller.deleteGroup(currGroup.name)
                textarea.text = null
                textarea.disableProperty().set(true)
                currSelection.parent.children.remove(currSelection)
                updateNoteview(controller.getAllUngroupedNotes(), null,
                    controller.getAllGroups())
            } else {
                alert.close()
            }
        }
    }

    private fun displayNoteContents(note: Note) {
        textarea.disableProperty().set(false)
        textarea.text = note.content
    }

    private fun saveSelectedNote() {
        val currItem = noteview.selectionModel.selectedItem
        if (currItem != null && currItem.value is Note) {
            controller.editNoteContent((currItem.value as Note).id, textarea.text)
        }
    }

    private fun searchNotes(search : String) {
        val notes = mutableListOf<Note>()
        if (search.isNotEmpty()) {
            notes.addAll(controller.getNotesByTitle(search))
            notes.addAll(controller.getNotesByContent(search))
            updateNoteview(notes, null, null)
        } else {
            updateNoteview(controller.getAllUngroupedNotes(), null)
        }
    }

    private fun renameSelectedNote() {
        val currItem = noteview.selectionModel.selectedItem
        if (currItem != null && currItem.value is Note) {
            val currNote = currItem.value as Note
            val td = TextInputDialog(currNote.title)
            td.headerText = "Enter a new title for your note."
            val result: Optional<String> = td.showAndWait()
            if (result.isPresent) {
                controller.editNoteTitle(currNote.id, result.get())
                textarea.disableProperty().set(false)
                displayNoteContents(currNote)
                updateNoteview(null, currNote)
            }
        } else if (currItem != null && currItem.value is Group) {
            val currGroup = currItem.value as Group
            val td = TextInputDialog(currGroup.name)
            td.headerText = "Enter a new title for your group."
            val result: Optional<String> = td.showAndWait()
            if (result.isPresent) {
                controller.editGroupName(currGroup.name, result.get())
                textarea.text = ""
                textarea.disableProperty().set(true)
                updateNoteview()
            }
        }
    }

    private fun createGroup() {
        val td = TextInputDialog("Name for Group")
        td.headerText = "Create a new Group"
        val result: Optional<String> = td.showAndWait()
        if (result.isPresent) {
            controller.createGroup(result.get())
            textarea.text = ""
            textarea.disableProperty().set(true)
            updateNoteview()
        }
    }

    private fun addSelectedNoteToGroup() {
        val currItem = noteview.selectionModel.selectedItem
        if (currItem != null && currItem.value is Note) {
            val groupList = mutableListOf<String>()
            for (group in controller.getAllGroups()) {
                groupList.add(group.name)
            }
            val td = ChoiceDialog<String>("Select a group", groupList)
            td.headerText = "Add ${(currItem.value as Note).title} to which group?"
            val result: Optional<String> = td.showAndWait()
            if (result.isPresent) {
                controller.addNoteToGroup(result.get(), currItem.value as Note)
                updateNoteview()
            }
        } else {
            val alert = Alert(AlertType.WARNING)
            alert.title = "Warning"
            alert.headerText = "Cannot add selected item to group"
            alert.showAndWait()
        }

    }

    private fun removeSelectedNoteFromGroup() {
        val currItem = noteview.selectionModel.selectedItem
        if (currItem != null && currItem.value is Note) {
            if (currItem.parent.value != null) {
                val parentGroup = currItem.parent.value as Group
                controller.removeNoteFromGroup(parentGroup.name, currItem.value as Note)
                updateNoteview()
            } else {
                val alert = Alert(AlertType.WARNING)
                alert.title = "Warning"
                alert.headerText = "Current note ${(currItem.value as Note).title} is not in a group."
                alert.showAndWait()
            }
        }
    }

    private fun undo() {
        controller.undo()
        updateNoteview()
    }

    private fun redo() {
        controller.redo()
        updateNoteview()
    }

}