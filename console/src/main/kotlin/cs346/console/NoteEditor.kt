package cs346.console

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import javafx.stage.WindowEvent

internal object NoteEditorSetting {
    @Volatile
    // false if no NoteEditor is open, true if a NoteEditor is open
    var active: Boolean = false

    @Volatile
    var noteTitle: String = ""

    @Volatile
    var noteContent: String =""
}

class NoteEditor() : Application() {
    override fun start(stage: Stage?) {
        val htmlEditor = HTMLEditor()
        val vBox = VBox(htmlEditor)
        val scene = Scene(vBox)

        stage!!.scene = scene
        stage!!.title =  NoteEditorSetting.noteTitle
        htmlEditor.htmlText = NoteEditorSetting.noteContent
        stage!!.show()

        stage.setOnCloseRequest { _: WindowEvent? ->
            NoteEditorSetting.noteContent = htmlEditor.htmlText
            NoteEditorSetting.active = false
        }
    }
}