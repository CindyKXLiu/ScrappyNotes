package cs346.webservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import org.jetbrains.exposed.sql.*
import java.util.*
import kotlin.collections.HashMap

@SpringBootApplication
class WebserviceApplication

fun main(args: Array<String>) {
    runApplication<WebserviceApplication>(*args)
}

@RestController
@RequestMapping("/model")
class ModelResource(val service: ModelService) {
    /**
     * Creates a GET mapping that retrieves the state of the Model.
     *
     * @return the state of the Model that was previously saved to the database
     */
    @GetMapping
    fun getState(): State = service.getState()

    /**
     * Creates a POST mapping that saves [state] to the database
     *
     * @param state contains the state of the Model
     */
    @PostMapping
    fun saveState(@RequestBody state: State) = service.saveState(state)

    /**
     * Creates a DELETE mapping that clears all entries in the database
     */
    /**@DeleteMapping
    fun clear() = service.clear()**/
}

data class State(val notes: HashMap<UUID, Note>, val groups: HashMap<String, Group>)

@Service
class ModelService {
    /**
     * Retrieves the Model's state that is saved to database
     *
     * @return the state of the Model that was previously saved to the database
     */
    fun getState(): State {
        return ModelDatabase.getState()
    }

    /**
     * Saves [state] to database
     *
     * @param state contains the state of the Model
     */
    fun saveState(state: State) {
        ModelDatabase.saveState(state)
    }

    /**
     * Clears all entries the database
     */
    /**fun clear() {
        ModelDatabase.clear()
    }**/
}
