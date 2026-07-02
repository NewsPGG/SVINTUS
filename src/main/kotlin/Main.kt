import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import view.Application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Свинтус"
    ) {
        Application()
    }
}