package net.codebot.application

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import net.codebot.shared.SysInfo

class Main : Application() {
    override fun start(stage: Stage) {
        stage.scene = Scene(
            StackPane(Label("Hello ${SysInfo.userName}")),
            250.0,
            150.0)
        stage.isResizable = false
        stage.title = "GUI Project"
        stage.show()
    }
}