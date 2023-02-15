package cs346.application

import cs346.shared.Controller
import cs346.shared.Note
import javafx.application.Application
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseButton
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
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
        val twotop = BorderPane()

        // top: menubar
        val menuBar = MenuBar()

        // FILE menubar manipulations /////////////////////////////////////////////////////////
        val fileMenu = Menu("File")
        val fileQuit = MenuItem("Quit")
        fileQuit.setOnAction { event ->
            stage.close()
        }

        // VIEW menubar manipulations /////////////////////////////////////////////////////////
        val viewMenu = Menu("View")
        val viewNext = MenuItem("Next")
        viewNext.setOnAction { event ->
            // enterDirectory()
        }
        val viewBack = MenuItem("Prev")
        viewBack.setOnAction { event ->
           // backDirectory()
        }

        // ACTIONS menubar manipulations ////////////////////////////////////////////////////////
        val actionsMenu = Menu("Actions")
        val actionsDelete = MenuItem("Delete")
        actionsDelete.setOnAction { event ->
            deleteSelectedNote()
        }
        val actionsRename = MenuItem("Rename")
        actionsRename.setOnAction { event ->
            // renameSelected()
        }
        val actionsMove = MenuItem("Move")
        actionsMove.setOnAction { event ->
            // moveSelected()
        }

        // OPTIONS menubar manipulations ///////////////////////////////////////////////////////////
        val optionsMenu = Menu("Options")
        val optionsHide = CheckMenuItem("Show Hidden")
        optionsHide.setOnAction { event ->
            /*displayHidden = !displayHidden
            updateTree()*/
        }

        fileMenu.items.add(fileQuit)
        menuBar.menus.add(fileMenu)

        viewMenu.items.add(viewNext)
        viewMenu.items.add(viewBack)
        menuBar.menus.add(viewMenu)

        actionsMenu.items.add(actionsDelete)
        actionsMenu.items.add(actionsRename)
        actionsMenu.items.add(actionsMove)
        menuBar.menus.add(actionsMenu)

        optionsMenu.items.add(optionsHide)
        menuBar.menus.add(optionsMenu)

        ///////////////////////////////////////////////////////////////////////////////////////////////
        val secondBar = HBox()

        val createButton = Button("New Note")
        createButton.setOnAction { actionEvent: ActionEvent ->
            createNote()
        }

        val saveButton = Button("Save Note")
        saveButton.setOnAction { actionEvent: ActionEvent ->
            saveSelectedNote()
        }

        val deleteButton = Button("Delete")
        deleteButton.setOnAction { event -> deleteSelectedNote() }

        val renameButton = Button("Rename")
        renameButton.setOnAction {}

        val moveButton = Button("Move")
        moveButton.setOnAction {}

        secondBar.spacing = 7.0
        secondBar.padding = Insets(3.0, 5.0, 3.0, 5.0)
        secondBar.children.addAll(createButton, saveButton, deleteButton, renameButton, moveButton)
        twotop.top = menuBar
        twotop.bottom = secondBar


        lastmodified.padding = Insets(3.0, 5.0, 3.0, 5.0)
        lastmodified.children.add(Label(""))
        ////////////////////////////////////////////////////////////////////////////////////////////////
        // left: list of notes
        updateNoteview(controller.getSortedNotesByModifiedDateAscending())

        // handle mouse clicked action
        noteview.setOnMouseClicked { event ->
            if (event.button.equals(MouseButton.PRIMARY)) {
                if (event.clickCount == 1 && noteview.items.size != 0) {
                    displayNoteContents(notedata[noteview.selectionModel.selectedIndex])
                }
            }
        }
        noteview.setOnKeyPressed { event ->
            if (event.code == KeyCode.ENTER) {
                displayNoteContents(notedata[noteview.selectionModel.selectedIndex])
            } else if (event.code == KeyCode.DELETE || event.code == KeyCode.BACK_SPACE) {
               deleteSelectedNote()
            } else {
                displayNoteContents(notedata[noteview.selectionModel.selectedIndex])
            }
        }

        // default center
        textarea.focusTraversableProperty().set(false)
        textarea.disableProperty().set(true)

        // build the scene graph
        layout.top = twotop
        layout.left = noteview
        layout.center = textarea
        layout.bottom = lastmodified

        // create and show the scene
        val scene = Scene(layout)
        stage.minWidth = 50.0
        stage.minHeight = 50.0
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
            if (selectedNote != null && indexofnote != -1) {
                noteview.scrollTo(indexofnote)
                noteview.selectionModel.select(indexofnote)
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
            layout.center = textarea
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
                layout.center = null
                updateNoteview(controller.getSortedNotesByModifiedDateDescending())
            } else {
                alert.close()
            }
        }
    }

    private fun displayNoteContents(note: Note) {
        layout.center = textarea
        textarea.text = note.content
    }

    private fun saveSelectedNote() {
        val currIndex = noteview.selectionModel.selectedIndex
        if (currIndex != -1) {
            val currNote = notedata[currIndex]
            controller.editNoteContent(currNote.id, textarea.text)
        }
    }
}