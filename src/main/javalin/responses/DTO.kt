package responses

import com.github.unqUi.model.Category

data class UserLoginDTO(val email: String, val password: String)
data class UserDTO(val id: String, val name: String, val image: String, val email: String)
data class UserRegisterDTO(val email: String, val password: String, val image: String, val name: String)
data class UserWithReviewsDTO(val id: String, val name: String, val image: String, val email: String, val reviews: MutableList<SimpleReviewDTO> = mutableListOf())
data class SimpleMovieDTO(val id: String, val title: String, val description: String, val poster: String, val categories : MutableList<Category> = mutableListOf(), val score: Int)
data class SimpleReviewDTO(val id: String, val user: UserDTO, val movie: SimpleMovieDTO, val text: String, val stars: Int)
data class ReviewDTO (val text: String, val stars: Int)
data class MovieDTO(val id: String, val title: String, val description: String, val poster: String, val categories : MutableList<Category> = mutableListOf(), val relatedContent: MutableList<SimpleMovieDTO>, val score: Int, val reviews: MutableList<SimpleReviewDTO>)

data class CategoryByIdDTO(val nameCategory: String, val movies: List<SimpleMovieDTO>)

