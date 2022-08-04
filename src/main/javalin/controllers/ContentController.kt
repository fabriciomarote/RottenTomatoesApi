package controllers

import com.github.unqUi.model.*
import io.javalin.http.Context
import responses.*
import token.JwtController


class ContentController(private val system: RottenTomatoesSystem) {
    val tokenJWT = JwtController(system)

    fun getContentId(ctx: Context) {
        val contentId = ctx.pathParam("id")
        try {
            val content = system.getMovieById(contentId)
            val related = content.relatedContent.map {
                SimpleMovieDTO(content.id, content.title, content.description, content.poster, content.categories, calculateScore(content.id, system)) }
            val contentResponse = MovieDTO(content.id, content.title,
                content.description, content.poster, content.categories,
                related as MutableList<SimpleMovieDTO>, calculateScore(content.id, system),
                reviewsFromMovie(content.id, system) as MutableList<SimpleReviewDTO>
            )
            ctx.json(contentResponse)
        } catch (e: MovieError) {
            ctx.status(400).json(mapOf("message" to e.message))
        }

    }

    fun getLatest(ctx: Context) {
        val movies = system.movies.map { SimpleMovieDTO(it.id, it.title, it.description, it.poster, it.categories, calculateScore(it.id, system)) }
        ctx.json(mapOf("result" to movies.takeLast(10)))
    }

    fun getTop(ctx: Context){
        var movies = system.movies.map { SimpleMovieDTO(it.id, it.title, it.description, it.poster, it.categories, calculateScore(it.id, system)) }
        movies = movies.sortedByDescending { it.score  }
        ctx.json(mapOf("result" to movies.take(10)))
    }
    fun review(ctx: Context){
        val token = ctx.header("Authorization")
        val userToken = tokenJWT.validate(token!!)
        val reviewDTO = ctx.bodyValidator<ReviewDTO>()
            .get()
        val movieId = ctx.pathParam("id")
        try {
            var user = system.getUserById(userToken.id)
            var movie = system.getMovieById(movieId)
            var review = system.addReview(DraftReview(user.id,movieId, reviewDTO.text, reviewDTO.stars))
            var userDTO = UserDTO(user.id, user.name, user.image, user.email)
            var movieDTO = SimpleMovieDTO(movie.id, movie.title, movie.description, movie.poster, movie.categories, calculateScore(movieId, system))
            ctx.json(SimpleReviewDTO(review.id, userDTO, movieDTO,reviewDTO.text, review.stars ))
        } catch (e: ReviewError) {
            ctx.status(400).json(mapOf("message" to e.message))

        }
        catch (e: MovieError) {
            ctx.status(400).json(mapOf("message" to e.message))
        }
    }

    fun reviewsFromMovie(movieID: String, system: RottenTomatoesSystem): List<SimpleReviewDTO> {

        val reviews= system.reviews;

        val reviewsFiltered=  reviews.filter { it.movie.id == movieID }

        val simpleReviews= reviewsFiltered.map {
            SimpleReviewDTO(it.id, UserDTO(it.user.id, it.user.name, it.user.image, it.user.email),
                SimpleMovieDTO(it.movie.id, it.movie.title, it.movie.description, it.movie.poster,
                    it.movie.categories, calculateScore(it.movie.id, system)), it.text, it.stars) }
        return simpleReviews

    }
}