import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import data.local.dao.SwintusDao
import data.repository.GameRepository
import view.Application
import viewmodel.SwintusViewModel

fun main() = application {
    val dao = SwintusDao()
    val repository = GameRepository(dao)
    val viewModel = SwintusViewModel(repository)

    Window(
        onCloseRequest = ::exitApplication,
        title = "Свинтус"
    ) {
        Application(viewModel = viewModel)
    }
}