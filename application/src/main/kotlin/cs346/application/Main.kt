package cs346.application

import cs346.shared.Controller
import cs346.shared.Note
import javafx.application.Application
import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.stage.Stage
import java.util.*


class Main : Application() {
    private val noteview = ListView<String>()
    private val notedata = FXCollections.observableArrayList<Note>()
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

        val newNote = MenuItem("New")
        newNote.setOnAction { event -> createNote() }

        val deleteNote = MenuItem("Delete")
        deleteNote.setOnAction { event -> deleteSelectedNote() }

        fileMenu.items.addAll(fileQuit, newNote, deleteNote)
        menuBar.menus.add(fileMenu)

        // ACTIONS menubar manipulations ////////////////////////////////////////////////////////
        val actionsMenu = Menu("Actions")

        val actionsRename = MenuItem("Rename")
        actionsRename.setOnAction { event -> renameSelectedNote() }
        val actionsMove = MenuItem("Move")
        actionsMove.setOnAction { event ->
            // moveSelected()
        }

        actionsMenu.items.add(actionsRename)
        actionsMenu.items.add(actionsMove)
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
        noteview.selectionModel.selectedItemProperty().addListener { observableValue: ObservableValue<out String>, old_val: String?, new_val: String? ->
            val currIndex = noteview.selectionModel.selectedIndex
            if (currIndex != -1) displayNoteContents(notedata[currIndex])
            textarea.disableProperty().set(false)
        }

        noteview.setOnMouseClicked { event ->
            if (event.button.equals(MouseButton.PRIMARY)) {
                if (event.clickCount == 1 && noteview.items.size != 0) {
                    val currIndex = noteview.selectionModel.selectedIndex
                    if (currIndex != -1) displayNoteContents(notedata[currIndex])
                }
            }
        }

        noteview.setOnKeyPressed { event ->
            if (event.code == KeyCode.ENTER) {
                val currIndex = noteview.selectionModel.selectedIndex
                if (currIndex != -1) displayNoteContents(notedata[currIndex])
            } else if (event.code == KeyCode.DELETE || event.code == KeyCode.BACK_SPACE) {
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

    private fun updateNoteview(listofnotes : List<Note>? = controller.getAllNotes(), selectedNote : Note? = null)
    {
        if (listofnotes != null)
        {
            noteview.selectionModel.clearSelection()
            val newview = FXCollections.observableArrayList<String>()
            notedata.clear()
            var indexofnote = -1
            listofnotes.forEachIndexed { index, note ->
                notedata.add(note)
                newview.add(note.title)
                if (selectedNote != null && selectedNote == note)
                {
                    indexofnote = index
                }
            }
            noteview.items = newview
            if (selectedNote != null) {
                if (indexofnote != -1) {
                    noteview.scrollTo(indexofnote)
                    noteview.selectionModel.select(indexofnote)
                }
            } else {
                textarea.text = null
                textarea.disableProperty().set(true)
            }
        }
    }

    private fun createNote()
    {
        val td = TextInputDialog("Enter a title for your note")
        td.headerText = "Create a new note"
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
        val currSelection = noteview.selectionModel.selectedItem
        val currIndex = noteview.selectionModel.selectedIndex
        if (currSelection != null) {
            val alert = Alert(Alert.AlertType.CONFIRMATION)
            alert.title = "Warning: Delete?"
            alert.headerText = "Are you sure you want to delete ${currSelection}"
            alert.contentText = "This cannot be undone!"

            val result = alert.showAndWait()
            if (result.get() == ButtonType.OK) {
                controller.deleteNote(notedata[currIndex].id)
                textarea.text = null
                textarea.disableProperty().set(true)
                notedata.removeAt(noteview.selectionModel.selectedIndex)
                noteview.items.removeAt(noteview.selectionModel.selectedIndex)
                updateNoteview(notedata.toList())
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
        val currIndex = noteview.selectionModel.selectedIndex
        if (currIndex != -1) {
            val currNote = notedata[currIndex]
            controller.editNoteContent(currNote.id, textarea.text)
        }
    }

    private fun searchNotes(search : String) {
        val notes = mutableListOf<Note>()
        if (search.isNotEmpty()) {
            notes.addAll(controller.getNotesByTitle(search))
            notes.addAll(controller.getNotesByContent(search))
            updateNoteview(notes)
        } else {
            updateNoteview(controller.getSortedNotesByModifiedDateDescending())
        }
    }

    private fun renameSelectedNote() {
        val currIndex = noteview.selectionModel.selectedIndex
        if (currIndex != -1) {
            val td = TextInputDialog(notedata[currIndex].title)
            td.headerText = "Enter a new title for your note."
            val result: Optional<String> = td.showAndWait()
            if (result.isPresent) {
                controller.editNoteTitle(notedata[currIndex].id, result.get())
                textarea.disableProperty().set(false)
                displayNoteContents(notedata[currIndex])
                updateNoteview(controller.getSortedNotesByModifiedDateDescending(), notedata[currIndex])
            }
        }
    }

}