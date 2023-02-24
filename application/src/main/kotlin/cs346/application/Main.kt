package cs346.application

import cs346.shared.*
import javafx.application.Application
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.util.*


class Main : Application() {
    private val noteview = TreeView<Any>()
    // private val notedata = FXCollections.observableArrayList<Note>()
    private val textarea = TextArea()
    private val lastmodified = HBox()
    private val layout = BorderPane()
    private val controller = Controller()
    override fun start(stage: Stage) {
        /**
         * Set up all drop down menus and functionality for top most bar
         */
        val menuBar = MenuBar()

        // FILE menubar manipulations /////////////////////////////////////////////////////////
        val fileMenu = Menu("File")
        val fileQuit = MenuItem("Quit")
        fileQuit.setOnAction { event -> stage.close() }

        val newNote = MenuItem("New Note")
        newNote.setOnAction { event -> createNote() }

        val deleteObject = MenuItem("Delete")
        deleteObject.setOnAction { event -> deleteSelectedNote() }

        val newGroup = MenuItem("New Group")
        newGroup.setOnAction { event -> createGroup() }

        fileMenu.items.addAll(fileQuit, newNote, newGroup, deleteObject)
        menuBar.menus.add(fileMenu)

        // ACTIONS menubar manipulations ////////////////////////////////////////////////////////
        val actionsMenu = Menu("Actions")

        val actionsRename = MenuItem("Rename")
        actionsRename.setOnAction { event -> renameSelectedNote() }

        val actionsGroup = MenuItem("Add to Group")
        actionsGroup.setOnAction { event ->
            addSelectedNoteToGroup()
        }

        val actionsRemove = MenuItem("Remove from Group")
        actionsRemove.setOnAction { event ->
            removeSelectedNoteFromGroup()
        }

        actionsMenu.items.addAll(actionsRename, actionsGroup, actionsRemove)
        menuBar.menus.add(actionsMenu)

        // OPTIONS menubar manipulations ///////////////////////////////////////////////////////////
        val optionsMenu = Menu("Options")
        val optionsHide = CheckMenuItem("Select Theme")
        optionsHide.setOnAction { event -> }

        optionsMenu.items.add(optionsHide)
        menuBar.menus.add(optionsMenu)

        /**
         * Set up Bottom Panel displaying last modification date of current file
         */
        lastmodified.padding = Insets(3.0, 5.0, 3.0, 5.0)
        lastmodified.children.add(Label(""))

        /**
         * Set up for left side note list display
         */
        updateNoteview(controller.getSortedNotesByModifiedDateAscending())
        noteview.setShowRoot(false)
        noteview.selectionModel.selectedItemProperty().addListener { observable, oldValue, newValue ->
            // val currIndex = noteview.selectionModel.selectedIndex
            // if (currIndex != -1)
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

        noteview.setOnMouseClicked { event ->
            if (event.button.equals(MouseButton.PRIMARY)) {
                if (event.clickCount == 1) {
                    val currSelection = noteview.selectionModel.selectedItem
                    if (currSelection != null)
                        if (currSelection.value is Note) {
                            displayNoteContents(currSelection.value as Note)
                            textarea.disableProperty().set(false)
                        }
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

        mainarea.children.addAll(texttools, textarea)

        /**
         * Add all panels to scene and show
         */
        layout.top = menuBar
        layout.left = leftside
        layout.center = mainarea
        layout.bottom = lastmodified

        val scene = Scene(layout)
        stage.minWidth = 400.0
        stage.minHeight = 300.0
        stage.width = 800.0
        stage.height = 600.0
        stage.scene = scene
        stage.isResizable = true
        stage.title = "Notes Application"
        stage.show()
    }

    private fun updateNoteview(listofnotes : List<Note>? = controller.getAllNotes(), selectedNote : Note? = null,
                               listofgroups : List<Group> ? = controller.getAllGroups())
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

                for (note in group.notes) {
                    newgroup.children.add(TreeItem(note.value))
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
            updateNoteview(controller.getSortedNotesByModifiedDateDescending(), newnote)
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
                updateNoteview(controller.getSortedNotesByModifiedDateDescending(), null,
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
                updateNoteview(controller.getSortedNotesByModifiedDateDescending(), null,
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
            updateNoteview(controller.getSortedNotesByModifiedDateDescending(), null)
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
                updateNoteview(controller.getSortedNotesByModifiedDateDescending(), currNote)
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
                updateNoteview(controller.getSortedNotesByModifiedDateDescending())
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
            updateNoteview(controller.getSortedNotesByModifiedDateDescending(), null,
                controller.getAllGroups())
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
            td.headerText = "Add Note to Which Group"
            val result: Optional<String> = td.showAndWait()
            if (result.isPresent) {
                controller.addNoteToGroup(result.get(), currItem.value as Note)
                updateNoteview(controller.getSortedNotesByModifiedDateDescending(), null,
                    controller.getAllGroups())
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
            if (currItem.parent != null) {
                val parentGroup = currItem.parent.value as Group
                controller.removeNoteFromGroup(parentGroup.name, currItem.value as Note)
                updateNoteview(controller.getSortedNotesByModifiedDateDescending(), null)
            } else {
                val alert = Alert(AlertType.WARNING)
                alert.title = "Warning"
                alert.headerText = "Current note ${(currItem.value as Note).title} is not in a group}"
                alert.showAndWait()
            }
        }
    }

}