package cs346.console

import cs346.application.Main
import javafx.application.Application

private const val HELP_MSG = "[] Launches the console application.\n" +
                             "[--launchGUI] Launches the GUI application.\n"

fun main(args: Array<String>) {
   if (args.isEmpty()) {
      Console()
   } else if (args[0] == "--launchGUI") {
      Application.launch(Main::class.java)
   } else {
      print(HELP_MSG)
   }
}