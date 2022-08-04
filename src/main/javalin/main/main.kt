package main

import com.github.unqUi.model.getRottenTomatoesSystem
import controllers.CategoriesController
import controllers.ContentController
import controllers.UserController
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.core.security.RouteRole
import io.javalin.core.util.RouteOverviewPlugin
import token.JwtController
import token.TokenAccessManager

enum class Roles: RouteRole {
    ANYONE,USER
}

class Api  {
    fun start() {
        val system = getRottenTomatoesSystem()
        val jwtController = JwtController(system)
        val userController = UserController(system)
        val categoriesController = CategoriesController(system)
        val contentController = ContentController(system)

        val app = Javalin.create {
            it.defaultContentType = "aplication/json"
            it.registerPlugin(RouteOverviewPlugin("/routes"))
            it.accessManager(TokenAccessManager(jwtController))
            it.enableCorsForAllOrigins()
        }

        app.before {
            it.header("Access-Control-Expose-Headers", "*")
        }

        app.start(7070)

        app.routes {
            path("login") {
                post(userController::login, Roles.ANYONE)
            }
            path("register") {
                post(userController::register, Roles.ANYONE)
            }
            path("user") {
                get(userController::getUser, Roles.USER)
                path("{id}") {
                    get(userController::getUserId, Roles.ANYONE)
                }
            }
            path("content") {
                path("latest") {
                    get(contentController::getLatest, Roles.ANYONE)
                }
                path("top") {
                    get(contentController::getTop, Roles.ANYONE)
                }
                path("{id}") {
                    get(contentController::getContentId, Roles.ANYONE)
                    post(contentController::review, Roles.USER)
                }
            }
            path("categories") {
                get(categoriesController::getCategories, Roles.ANYONE)
                path("{id}") {
                    get(categoriesController::getCategoryId, Roles.ANYONE)
                }
            }
        }
    }
}

fun main() {
    Api().start()
}