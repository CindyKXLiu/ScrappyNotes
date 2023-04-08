package cs346.console

import javafx.application.Application;
import javafx.application.Platform
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import javafx.stage.WindowEvent

class NoteEditor() : Application() {
    override fun start(stage: Stage?) {
        val htmlEditor = HTMLEditor()
        val vBox = VBox(htmlEditor)
        val scene = Scene(vBox)

        stage!!.scene = scene
        stage!!.title =  NoteEditorLauncher.Setting.noteTitle
        htmlEditor.htmlText = NoteEditorLauncher.Setting.noteContent
        stage!!.toFront()
        stage!!.show()

        stage.setOnCloseRequest { _: WindowEvent? ->
            NoteEditorLauncher.Setting.noteContent = htmlEditor.htmlText
            NoteEditorLauncher.Setting.active = false
        }
    }
}


/**
 * This class allows the console to seemingly launch a JavaFX application multiple times.
 *
 * Source: https://stackoverflow.com/questions/24320014/how-to-call-launch-more-than-once-in-java/61771424#61771424
 *
 * @property applicationClass is the application to be launched
 * @property launched is a flag that is by default false, but once application has been launched for the first time it is set to true
 */
class NoteEditorLauncher {
    private val applicationClass: Class<NoteEditor> = NoteEditor::class.java

    @Volatile
    private var launched: Boolean = false

    internal object Setting {
        @Volatile
        // false if no NoteEditor is open, true if a NoteEditor is open
        var active: Boolean = false

        @Volatile
        var noteTitle: String = ""

        @Volatile
        var noteContent: String =""
    }

    fun launch() {
        if (!launched) {
            Platform.setImplicitExit(false)
            Thread { Application.launch(applicationClass) }.start()
            launched = true
        } else {
            Platform.runLater {
                try {
                    val application = applicationClass!!.getDeclaredConstructor().newInstance()
                    val primaryStage = Stage()
                    application.start(primaryStage)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
