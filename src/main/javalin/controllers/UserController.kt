package controllers

import com.github.unqUi.model.*
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import io.javalin.http.NotFoundResponse
import io.javalin.http.UnauthorizedResponse
import responses.*
import token.JwtController
import java.util.regex.Pattern

val emailPattern: Pattern = Pattern.compile(
    "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
            "\\@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+"
)

class UserController(private val system: RottenTomatoesSystem) {

    val tokenJWT = JwtController(system)

    fun login(ctx: Context) {
        val userLogin = ctx.bodyValidator<UserLoginDTO>()
            .check({ it.email.isNotEmpty() }, "Email cannot be empty")
            .check({emailPattern.toRegex().matches(it.email) }, "Invalid email address")
            .check({ it.password.isNotEmpty() }, "Password cannot by empty")
            .get()

        try {
            val user = system.users.find { it.email == userLogin.email }
            if (user != null) {
                if (userLogin.password == user.password) {
                    ctx.header("Authorization", tokenJWT.generate(user))
                    ctx.json(UserDTO(user.id, user.name, user.image, user.email))
                } else {
                    throw BadRequestResponse("Password incorrect")
                }
            } else {
                throw NotFoundResponse("Email doesn't exist")
            }
        } catch (e: NotFoundResponse) {
            ctx.status(404).json(mapOf("message" to e.message))
        }
        catch (e: BadRequestResponse){
            ctx.status(400).json(mapOf("message" to e.message))
        }
    }

    fun register(ctx: Context){
        val userRegister= ctx.bodyValidator<UserRegisterDTO>()
            .get()
        try {
            val user = system.addUser(DraftUser(userRegister.name, userRegister.image, userRegister.email, userRegister.password) )
            ctx.json(UserDTO(user.id, user.name, user.image, user.email));
        }
        catch (e: UserError) {
            ctx.status(400)
            ctx.json(
                mapOf("message" to e.message))
        }
    }

    fun getUser(ctx: Context) {

        try {
            val token = ctx.header("Authorization")
            val user = tokenJWT.validate(token!!)
            val userResponse = system.getUserById(user.id)
            val userResponse2 = UserDTO(userResponse.id, userResponse.name, userResponse.image, userResponse.email)
            val reviewsFromUser = userResponse.reviews.map { SimpleReviewDTO(it.id, userResponse2, movieFromReview(it.movie), it.text, it.stars) }
            ctx.json(UserWithReviewsDTO(userResponse.id, userResponse.name, userResponse.image, userResponse.email, reviewsFromUser as MutableList<SimpleReviewDTO>))
        }
        catch(e: UnauthorizedResponse){

            ctx.status(401).json(mapOf("message" to e.message))
        }
    }

    fun getUserId(ctx: Context) {
        val userId = ctx.pathParam("id")
        try {
            val user =  system.getUserById(userId);
            val user2 = UserDTO(user.id, user.name, user.image, user.email)
            val reviewsFromUser = user.reviews.map { SimpleReviewDTO(it.id, user2, movieFromReview(it.movie), it.text, it.stars) }
            ctx.json(UserWithReviewsDTO(user.id, user.name, user.image, user.email, reviewsFromUser as MutableList<SimpleReviewDTO>))
        }
        catch (e: UserError){
            ctx.status(404).json(mapOf("message" to e.message))
        }
    }

    fun movieFromReview(movie: Movie): SimpleMovieDTO{
        return SimpleMovieDTO(movie.id, movie.title, movie.description, movie.poster, movie.categories, calculateScore(movie.id, system))
    }

}