package cs346.application

import cs346.shared.*
import javafx.application.Application
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.input.KeyCode
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.scene.web.HTMLEditor
import javafx.stage.Stage
import javafx.stage.WindowEvent
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.format.DateTimeFormatter
import java.util.*

private const val APP_SIZE_FILE = "appSizing.json"

@Serializable
data class AppSizing(val posX: Double, val posY: Double, val height: Double, val width: Double)
class Main : Application() {
    private val defaultHeight = 600.0
    private val defaultWidth = 1000.0

    private val noteview = TreeView<Any>()
    private val textarea = HTMLEditor()
    private val lastmodified = Text()
    private val layout = BorderPane()
    private val model = Model()

    /**
     * NoteFilterType used to track what filtering option is currently
     * used by display noteview.
     * TITLE = by title, CONTENT = by content
     */
    enum class NoteFilterType {
        TITLE, CONTENT, DEFAULT
    }

    /**
     * NoteSortType used to track what sorting option is currently
     * used by display noteview.
     * ALPHA = alphabetical order, CREATED = date created,
     * MASCENDING = last modified ascending
     * MDESCENDING = last modified descending
     */
    enum class NoteSortType {
        ALPHA, CREATED, MASCENDING, MDESCENDING, DEFAULT
    }

    private var currentFilterType = NoteFilterType.DEFAULT
    private var currentSortType = NoteSortType.DEFAULT

    override fun start(stage: Stage) {
        /**
         * Set up all drop down menus and functionality for top most bar
         */
        val menuBar = MenuBar()

        // FILE menubar manipulations /////////////////////////////////////////////////////////
        val fileMenu = Menu("File")
        val fileQuit = MenuItem("Quit")
        fileQuit.setOnAction { _ -> stop() }

        val newNote = MenuItem("New Note (CTRL+N)")
        newNote.setOnAction { _ -> createNote() }

        val deleteObject = MenuItem("Delete")
        deleteObject.setOnAction { _ -> deleteSelectedNote() }

        val newGroup = MenuItem("New Group (CTRL+G)")
        newGroup.setOnAction { _ -> createGroup() }

        fileMenu.items.addAll(fileQuit, newNote, newGroup, deleteObject)
        menuBar.menus.add(fileMenu)

        // ACTIONS menubar manipulations ////////////////////////////////////////////////////////
        val actionsMenu = Menu("Actions")

        val actionsRename = MenuItem("Rename")
        actionsRename.setOnAction { _ -> renameSelectedNote() }

        val actionsGroup = MenuItem("Move to Group")
        actionsGroup.setOnAction { _ ->
            moveSelectedNoteToGroup()
        }

        val actionsRemove = MenuItem("Remove from Group")
        actionsRemove.setOnAction { _ ->
            removeSelectedNoteFromGroup()
        }

        val actionsUndo = MenuItem("Undo (CTRL+Z)")
        actionsUndo.setOnAction { _ -> undo() }

        val actionsRedo = MenuItem("Redo (CTRL+Y)")
        actionsRedo.setOnAction { _ -> redo() }

        actionsMenu.items.addAll(actionsRename, actionsGroup, actionsRemove, actionsUndo,
            actionsRedo)
        menuBar.menus.add(actionsMenu)

        // OPTIONS menubar manipulations ///////////////////////////////////////////////////////////
        val optionsMenu = Menu("Options")
        val optionsTheme = Menu("Select Theme")
        val nordDark = MenuItem("Nord Dark")
        nordDark.setOnAction { _ -> setUserAgentStylesheet("nord-dark.css") }
        val nordLight = MenuItem("Nord Light")
        nordLight.setOnAction { _ -> setUserAgentStylesheet("nord-light.css") }
        val primerDark = MenuItem("Primer Dark")
        primerDark.setOnAction { _ -> setUserAgentStylesheet("primer-dark.css") }
        val primerLight = MenuItem("Primer Light")
        primerLight.setOnAction { _ -> setUserAgentStylesheet("primer-light.css") }

        optionsTheme.items.addAll(nordDark, nordLight, primerDark, primerLight)
        optionsMenu.items.add(optionsTheme)
        menuBar.menus.add(optionsMenu)


        /**
         * Set up for left side note list display
         */
        updateNoteview()
        noteview.isShowRoot = false
        noteview.selectionModel.selectedItemProperty().addListener { _, _, _ ->
            val currSelection = noteview.selectionModel.selectedItem
            if (currSelection != null)
            {
                if (currSelection.value is Note) {
                    displayNoteContents(currSelection.value as Note)
                    textarea.disableProperty().set(false)
                } else if (currSelection.value is Group) {
                    textarea.htmlText = ""
                    textarea.disableProperty().set(true)
                }
                updateTime()
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

        /**
         * Set up for text fields for last modified and time
         */
        val bottomLine = BorderPane()
        bottomLine.left = lastmodified
        bottomLine.padding = Insets(3.0, 5.0, 3.0, 5.0)

        /**
         * Dropdown menus for search filtering
         */
        val searchfilter = ChoiceBox(FXCollections.observableArrayList("Filter results by...", "Title", "Content")).apply {
            selectionModel.select(0)
            // handles switch to selected filter type
            selectionModel.selectedItemProperty().addListener {
                    _, _, newFilter ->
                currentFilterType = if (newFilter == "Title") {
                    NoteFilterType.TITLE
                } else if (newFilter == "Content") {
                    NoteFilterType.CONTENT
                } else {
                    NoteFilterType.DEFAULT
                }
            }
        }
        val searchsort = ChoiceBox(FXCollections.observableArrayList("Display by...", "Title", "Date created", "Last modified asc.", "Last modified desc.")).apply {
            selectionModel.select(0)
            // handles switch to selected sort order
            selectionModel.selectedItemProperty().addListener {
                    _, _, newSort ->
                currentSortType = if (newSort == "Title") {
                    NoteSortType.ALPHA
                } else if (newSort == "Date created") {
                    NoteSortType.CREATED
                } else if (newSort == "Last modified asc.") {
                    NoteSortType.MASCENDING
                } else if (newSort == "Last modified desc.") {
                    NoteSortType.MDESCENDING
                } else {
                    NoteSortType.DEFAULT
                }
            }
        }

        // container for search filters
        val filters = HBox()
        HBox.setHgrow(searchfilter, Priority.ALWAYS)
        HBox.setHgrow(searchsort, Priority.ALWAYS)
        filters.children.addAll(searchfilter, searchsort)

        val leftside = VBox()
        leftside.spacing = 0.0
        VBox.setVgrow(noteview, Priority.ALWAYS)
        leftside.prefWidth = 250.0
        leftside.children.addAll(searchbox, filters, noteview)

        textarea.focusTraversableProperty().set(false)
        textarea.htmlText = ""
        textarea.disableProperty().set(true)
        textarea.setOnMouseExited { _ ->
            saveSelectedNote()
            updateTime()
        }

        // MAIN scene set up ////////////////////////////////////////////////////////////////////////

        /**
         * Check for app sizing and positioning
         */

        val appSpecifications = File(APP_SIZE_FILE)
        if (appSpecifications.exists()) {
            val appSpecs = appSpecifications.readText(Charsets.UTF_8)
            val specifications = Json.decodeFromString<AppSizing>(appSpecs)
            stage.x = specifications.posX
            stage.y = specifications.posY
            stage.width = specifications.width
            stage.height = specifications.height
        } else {
            stage.width = defaultWidth
            stage.height = defaultHeight
        }

        stage.setOnCloseRequest { _: WindowEvent? ->
            val json = Json.encodeToString(AppSizing(stage.x, stage.y, stage.height, stage.width))
            File(APP_SIZE_FILE).bufferedWriter().use { out ->
                out.flush()
                out.write(json)
            }
            model.saveToDatabase()
        }
        /**
         * Add all panels to scene and show
         */
        layout.top = menuBar
        layout.left = leftside
        layout.center = textarea
        layout.bottom = lastmodified

        val scene = Scene(layout)

        /**
         * Set up themes and stylesheet for scene
         */
        setUserAgentStylesheet("nord-light.css")

        /**
         * Set up hotkeys for scene
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
        stage.scene = scene
        stage.isResizable = true
        stage.title = "Notes Application"

        updateNoteview()
        stage.show()
    }

    private fun updateNoteview(listofnotes : List<Note>? = model.getAllUngroupedNotes(), selectedNote : Note? = null,
                               listofgroups : List<Group>? = model.getAllGroups())
    {
        val rootitem = TreeItem<Any>()

        if (listofnotes != null) {
            noteview.selectionModel.clearSelection()
            var treeitemofnote : TreeItem<Any>? = null
            listofnotes.forEachIndexed { _, note ->
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
                textarea.htmlText = ""
                textarea.disableProperty().set(true)
            }
        }

        listofgroups?.forEachIndexed { _, group ->
            val newgroup = TreeItem<Any>(group)

            for (note in group.getNotes()) {
                try {
                    newgroup.children.add(TreeItem(model.getNoteByID(note)))
                } catch (error : NonExistentNoteException) {
                    print("Failed to add note id: " + note + " to group " + group.name)
                }
            }
            rootitem.children.add(newgroup)
        }
        noteview.root = rootitem
    }

    private fun createNote()
    {
        val td = TextInputDialog("Name for Note")
        td.headerText = "Create a new Note"
        val result: Optional<String> = td.showAndWait()
        if (result.isPresent) {
            val newnote = model.createNote(result.get(),"")
            textarea.disableProperty().set(false)
            displayNoteContents(newnote)
            updateNoteview(model.getAllUngroupedNotes(), newnote)
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
                model.deleteNote(currNote.id)
                textarea.htmlText = ""
                textarea.disableProperty().set(true)
                currSelection.parent.children.remove(currSelection)
                updateNoteview(model.getAllUngroupedNotes(), null,
                    model.getAllGroups())
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
                model.deleteGroup(currGroup.name)
                textarea.htmlText = ""
                textarea.disableProperty().set(true)
                currSelection.parent.children.remove(currSelection)
                updateNoteview(model.getAllUngroupedNotes(), null,
                    model.getAllGroups())
            } else {
                alert.close()
            }
        }
    }

    private fun displayNoteContents(note: Note) {
        textarea.disableProperty().set(false)
        textarea.htmlText = note.content
    }

    private fun saveSelectedNote() {
        val currItem = noteview.selectionModel.selectedItem
        if (currItem != null && currItem.value is Note) {
            model.editNoteContent((currItem.value as Note).id, textarea.htmlText)
        }
    }

    private fun searchNotes(search : String) {
        var notes = mutableListOf<Note>()
        if (search.isNotEmpty()) {
            // handle search filtering by title or content
            when (currentFilterType) {
                NoteFilterType.TITLE -> {
                    notes.addAll(model.getNotesByTitle(search))
                }
                NoteFilterType.CONTENT -> {
                    notes.addAll(model.getNotesByContent(search))
                }
                else -> {
                    notes.addAll(model.getNotesByTitle(search))
                    notes.addAll(model.getNotesByContent(search))
                }
            }
            //updateNoteview(notes, null, null)
        } else {
            notes = model.getAllUngroupedNotes() as MutableList<Note>
            //updateNoteview(model.getAllUngroupedNotes(), null)
        }
        // handle search results sorting (MESSY AND NEEDS TO BE FIXED LATER)
        if (notes.isNotEmpty()) {
            when (currentSortType) {
                NoteSortType.ALPHA -> {
                    notes = Sort.sortByTitle(notes, Sort.Order.ASC) as MutableList<Note>
                }
                NoteSortType.CREATED -> {
                    notes = Sort.sortByDateCreated(notes, Sort.Order.ASC) as MutableList<Note>
                }
                NoteSortType.MASCENDING -> {
                    notes = Sort.sortByDateModified(notes, Sort.Order.ASC) as MutableList<Note>
                }
                NoteSortType.MDESCENDING -> {
                    notes = Sort.sortByDateModified(notes, Sort.Order.DESC) as MutableList<Note>
                }
                else -> {}
            }
        }
        updateNoteview(notes, null)
    }

    private fun renameSelectedNote() {
        val currItem = noteview.selectionModel.selectedItem
        if (currItem != null && currItem.value is Note) {
            val currNote = currItem.value as Note
            val td = TextInputDialog(currNote.title)
            td.headerText = "Enter a new title for your note."
            val result: Optional<String> = td.showAndWait()
            if (result.isPresent) {
                model.editNoteTitle(currNote.id, result.get())
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
                model.editGroupName(currGroup.name, result.get())
                textarea.htmlText = ""
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
            model.createGroup(result.get())
            textarea.htmlText = ""
            textarea.disableProperty().set(true)
            updateNoteview()
        }
    }

    private fun moveSelectedNoteToGroup() {
        val currItem = noteview.selectionModel.selectedItem
        if (currItem != null && currItem.value is Note) {
            val currNote = currItem.value as Note
            val groupList = mutableListOf<String>()
            for (group in model.getAllGroups()) {
                groupList.add(group.name)
            }
            val td = ChoiceDialog<String>("Select a group", groupList)

            if (currNote.groupName == null) {
                td.headerText = "Add ${(currItem.value as Note).title} to which group?"
            } else {
                td.headerText = "Move ${(currItem.value as Note).title} to which group?"
            }
            val result: Optional<String> = td.showAndWait()
            if (result.isPresent) {
                try {
                    model.moveNoteToGroup(result.get(), (currItem.value as Note).id)
                    updateNoteview()
                } catch (exception : NonExistentGroupException) {
                    val alert = Alert(AlertType.WARNING)
                    alert.title = "Warning"
                    alert.headerText = "Cannot add selected item to group"
                    alert.showAndWait()
                }
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
                model.removeNoteFromGroup(parentGroup.name, (currItem.value as Note).id)
                updateNoteview()
            } else {
                val alert = Alert(AlertType.WARNING)
                alert.title = "Warning"
                alert.headerText = "Current note ${(currItem.value as Note).title} is not in a group."
                alert.showAndWait()
            }
        }
    }

    private fun updateTime() {
        val currItem = noteview.selectionModel.selectedItem
        if (currItem != null && currItem.value is Note) {
            lastmodified.isVisible = true
            val currNote = currItem.value as Note
            val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd / MM / yyyy HH:mm")
            val output = "Last Modified: " + formatter.format(currNote.dateModified)
            lastmodified.text = output
        } else {
            lastmodified.isVisible = false
        }
    }

    private fun undo() {
        model.undo()
        updateNoteview()
    }

    private fun redo() {
        model.redo()
        updateNoteview()
    }
}

/**
 * Launches the GUI application (for use outside this module)
 */
fun launch() {
    Application.launch(Main::class.java)
}