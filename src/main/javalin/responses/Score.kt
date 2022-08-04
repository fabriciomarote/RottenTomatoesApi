package responses

import com.github.unqUi.model.Review
import com.github.unqUi.model.RottenTomatoesSystem

fun calculateScore(movieID: String, system: RottenTomatoesSystem): Int{

    val reviewsOfMovie = system.reviews.filter { it.movie.id == movieID }
    var acum : Int=0;
    reviewsOfMovie.forEach { acum += it.stars }
    if (reviewsOfMovie.isEmpty()){
        return 0
    }else {
        return acum / reviewsOfMovie.size;
    }
}




