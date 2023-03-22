package cs346.console

import javafx.application.Application
import javafx.application.Platform
import javafx.stage.Stage

internal object LauncherSetting {
    var applicationClass: Class<out Application>? = null
}

/**
 * This class allows the console to seemingly launch a JavaFX application multiple times.
 *
 * Source: https://stackoverflow.com/questions/24320014/how-to-call-launch-more-than-once-in-java/61771424#61771424
 *
 * @property applicationClass is the application to be launched
 * @property launched is a flag that is by default false, but once application has been launched for the first time it is set to true
 */
class Launcher{
    @Volatile
    private var launched: Boolean = false

    fun launch() {
        if (!launched) {
            Platform.setImplicitExit(false)
            Thread { Application.launch(LauncherSetting.applicationClass) }.start()
            launched = true
        } else {
            Platform.runLater {
                try {
                    val application = LauncherSetting.applicationClass!!.getDeclaredConstructor().newInstance()
                    val primaryStage = Stage()
                    application.start(primaryStage)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
