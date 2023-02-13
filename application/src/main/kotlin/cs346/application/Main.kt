package cs346.application

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.stage.Stage

class Main : Application() {
    override fun start(stage: Stage) {
        stage.scene = Scene(
            StackPane(Label("Hello")),
            250.0,
            150.0)
        stage.isResizable = false
        stage.title = "GUI Project"
        stage.show()
    }
}