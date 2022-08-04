package controllers

import com.github.unqUi.model.CategoryError
import com.github.unqUi.model.RottenTomatoesSystem
import io.javalin.http.Context
import responses.CategoryByIdDTO
import responses.SimpleMovieDTO
import responses.calculateScore

class CategoriesController(private val system: RottenTomatoesSystem) {
    fun getCategories(ctx: Context) {

        val allCategories = system.categories

        ctx.json(mapOf("result" to allCategories))
    }

    fun getCategoryId(ctx: Context) {

        val categoryId = ctx.pathParam("id")

        try {
            val category = system.getCategoryById(categoryId)
            val allMovies = system.movies
            val filterMovies = allMovies.filter { movie -> movie.categories.contains(category)}
            val moviesResponse = filterMovies.map { SimpleMovieDTO(it.id, it.title, it.description, it.poster, it.categories, calculateScore(it.id, system)) }
            ctx.json(mapOf("result" to CategoryByIdDTO(category.name, moviesResponse)))
        } catch (e: CategoryError) {
            ctx.status(404).json(mapOf("message" to e.message))
        }

    }
}