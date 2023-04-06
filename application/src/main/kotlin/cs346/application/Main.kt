package cs346.application

import cs346.shared.*
import javafx.application.Application
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Orientation
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
import javafx.scene.web.WebView
import javafx.stage.FileChooser
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

private const val NORD_DARK_CSS_HTML_EDITOR = "data:text/css," +
        "body {" +
        "  background-color: #2E3440;" +
        "  color: #ECEFF4;" +
        "}"
private const val NORD_LIGHT_CSS_HTML_EDITOR = "data:text/css," +
        "body {" +
        "  background-color: #fafafc;" +
        "  color: #2E3440;" +
        "}"
private const val PRIMER_LIGHT_CSS_HTML_EDITOR = "data:text/css," +
        "body {" +
        "  background-color: #ffffff;" +
        "  color: #24292f;" +
        "}"
private const val PRIMER_DARK_CSS_HTML_EDITOR = "data:text/css," +
        "body {" +
        "  background-color: #0d1117;" +
        "  color: #c9d1d9;" +
        "}"

@Serializable
data class AppSizing(val posX: Double, val posY: Double, val height: Double, val width: Double, val theme: String)

class Main : Application() {
    private val defaultHeight = 600.0
    private val defaultWidth = 1000.0
    private var currentTheme = "nord-light"

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
        val fileQuit = MenuItem("Close")
        fileQuit.setOnAction { _ -> stop() }

        val newNote = MenuItem("New Note (CTRL+N)")
        newNote.setOnAction { _ -> createNote() }

        val deleteObject = MenuItem("Delete (CTRL+D)")
        deleteObject.setOnAction { _ -> deleteSelectedNote() }

        val newGroup = MenuItem("New Group (CTRL+G)")
        newGroup.setOnAction { _ -> createGroup() }

        fileMenu.items.addAll(newNote, newGroup,
            SeparatorMenuItem(), deleteObject,
            SeparatorMenuItem(), fileQuit)
        menuBar.menus.add(fileMenu)

        // EDIT menubar manipulations ////////////////////////////////////////////////////////
        val actionsMenu = Menu("Edit")

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

        actionsMenu.items.addAll(actionsUndo, actionsRedo,
            SeparatorMenuItem(), actionsRename,
            SeparatorMenuItem(), actionsGroup, actionsRemove)
        menuBar.menus.add(actionsMenu)

        // VIEW menubar manipulations ///////////////////////////////////////////////////////////
        val viewMenu = Menu("View")
        val themeOptions = Menu("Themes")
        val nordDark = MenuItem("Nord Dark")
        nordDark.setOnAction { _ ->
            currentTheme = "nord-dark"
            setUserAgentStylesheet("$currentTheme.css")
            applyThemeToHTMLEditorWebView(currentTheme)
        }
        val nordLight = MenuItem("Nord Light")
        nordLight.setOnAction { _ ->
            currentTheme = "nord-light"
            setUserAgentStylesheet("$currentTheme.css")
            applyThemeToHTMLEditorWebView(currentTheme)
        }
        val primerDark = MenuItem("Primer Dark")
        primerDark.setOnAction { _ ->
            currentTheme = "primer-dark"
            setUserAgentStylesheet("$currentTheme.css")
            applyThemeToHTMLEditorWebView(currentTheme)
        }
        val primerLight = MenuItem("Primer Light")
        primerLight.setOnAction { _ ->
            currentTheme = "primer-light"
            setUserAgentStylesheet("$currentTheme.css")
            applyThemeToHTMLEditorWebView(currentTheme)
        }

        themeOptions.items.addAll(nordDark, nordLight, primerDark, primerLight)
        viewMenu.items.add(themeOptions)
        menuBar.menus.add(viewMenu)

        // DATABASE menubar ///////////////////////////////////////////////////////
        val databaseMenu = Menu("Sync")
        val saveDatabase = MenuItem("Save")
        saveDatabase.setOnAction { _ ->
            model.saveToDatabase()
        }
        val updateDatabase = MenuItem("Update")
        updateDatabase.setOnAction { _ ->
            model.updateDatabase()
            val currSelection = noteview.selectionModel.selectedItem
            if (currSelection.value is Note) {
                updateNoteview(selectedNote=(currSelection.value as Note).id)
            }
        }
        databaseMenu.items.addAll(saveDatabase, updateDatabase)
        menuBar.menus.add(databaseMenu)

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
        searchbox.promptText = "Search"
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
        val searchsort = ChoiceBox(FXCollections.observableArrayList("Sort by", "Title", "Date created", "Last modified asc.", "Last modified desc.")).apply {
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
                searchNotes(searchbox.text)
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

        // Custom insert image button for textarea toolbar
        val textareaBar = textarea.lookup(".top-toolbar") as ToolBar
        val imageButton = Button("Insert Image")
        imageButton.setOnMouseClicked {
            onImageButtonClick()
        }
        textareaBar.items.addAll(imageButton, Separator(Orientation.VERTICAL))


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
            setUserAgentStylesheet(specifications.theme + ".css")
            applyThemeToHTMLEditorWebView(specifications.theme)
        } else {
            stage.width = defaultWidth
            stage.height = defaultHeight
            setUserAgentStylesheet("nord-light.css")
        }

        stage.setOnCloseRequest { _: WindowEvent? ->
            val json = Json.encodeToString(AppSizing(stage.x, stage.y, stage.height, stage.width, currentTheme))
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
                    KeyCode.D -> deleteSelectedNote()
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

    /**
     * Applies the [theme] to the text area within the htmlEditor
     *
     * @param theme the theme to be set
     */
    private fun applyThemeToHTMLEditorWebView(theme: String) {
        val webView = textarea.lookup("WebView") as WebView
        val webEngine = webView.engine

        var cssString = ""
        when (theme) {
            "nord-dark" -> cssString = NORD_DARK_CSS_HTML_EDITOR
            "nord-light" -> cssString = NORD_LIGHT_CSS_HTML_EDITOR
            "primer-dark" -> cssString = PRIMER_DARK_CSS_HTML_EDITOR
            "primer-light" -> cssString = PRIMER_LIGHT_CSS_HTML_EDITOR
        }

        webEngine.userStyleSheetLocation = cssString
    }

    private fun updateNoteview(listofnotes : List<Note>? = model.getAllUngroupedNotes(), selectedNote : UUID? = null,
                               listofgroups : List<Group>? = model.getAllGroups())
    {
        val rootitem = TreeItem<Any>()

        if (listofnotes != null) {
            // Display notes by current filters
            var notes = listofnotes

            if (listofnotes.isNotEmpty()) {
                when (currentSortType) {
                    NoteSortType.ALPHA -> {
                        notes = Sort.sortByTitle(listofnotes, Sort.Order.ASC) as MutableList<Note>
                    }
                    NoteSortType.CREATED -> {
                        notes = Sort.sortByDateCreated(listofnotes, Sort.Order.ASC) as MutableList<Note>
                    }
                    NoteSortType.MASCENDING -> {
                        notes = Sort.sortByDateModified(listofnotes, Sort.Order.ASC) as MutableList<Note>
                    }
                    NoteSortType.MDESCENDING -> {
                        notes = Sort.sortByDateModified(listofnotes, Sort.Order.DESC) as MutableList<Note>
                    }
                    else -> {}
                }
            }

            noteview.selectionModel.clearSelection()
            var treeitemofnote : TreeItem<Any>? = null
            notes.forEachIndexed { _, note ->
                val newitem = TreeItem<Any>(note)
                rootitem.children.add(newitem)
                if (selectedNote != null && selectedNote == note.id) {
                    treeitemofnote = newitem
                    displayNoteContents(note)
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
            updateNoteview(model.getAllUngroupedNotes(), newnote.id)
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
        val notes = mutableListOf<Note>()
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
            updateNoteview(notes, null, listOf<Group>())
        } else {
            updateNoteview()
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
                model.editNoteTitle(currNote.id, result.get())
                textarea.disableProperty().set(false)
                displayNoteContents(currNote)
                updateNoteview(model.getAllUngroupedNotes(), currNote.id)
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

    /**
     * Opens file explorer that allows user to choose an image file from user's local computer
     * Appends image to the end of current content in textarea
     */
    private fun onImageButtonClick() {
        // Open file explorer dialog
        val fileChooser = FileChooser()
        val imageFilter = FileChooser.ExtensionFilter("All Image Files", "*.png", "*.jpg", "*.jpeg")
        fileChooser.title = "Select image to import"
        fileChooser.extensionFilters.add(imageFilter)
        val selectedFile = fileChooser.showOpenDialog(textarea.scene.window)

        // If file is selected and successfully converted, add to textarea content
        if (selectedFile != null) {
            val html = convertFile(selectedFile)
            if (html != "") {
                val oldText = textarea.htmlText
                textarea.htmlText = "$oldText<img src=\"$html\" width=\"100%\">"
            }
        }
    }

    /**
     * Convert a file [file] to a string representing its data URI
     * Returns an empty string if [file] cannot be converted
     *
     * @param file is the File object to be converted
     */
    private fun convertFile(file: File): String {
        // check size of file
        if (file.length() > 524288) {
            val alert = Alert(AlertType.WARNING)
            alert.title = "Warning"
            alert.headerText = "Warning: File size limit"
            alert.contentText = "\"$file\" is too large and cannot be opened.\nFiles larger than 0.5 MB are not currently supported."
            alert.showAndWait()
            return ""
        }
        // get file type
        val type = java.nio.file.Files.probeContentType(file.toPath())
        // get html content
        val data = java.nio.file.Files.readAllBytes(file.toPath())
        val base64data = Base64.getEncoder().encodeToString(data)
        // return full data uri
        return "data:$type;base64,$base64data"
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