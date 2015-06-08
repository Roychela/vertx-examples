import io.vertx.groovy.ext.web.Router
import io.vertx.groovy.ext.auth.jwt.JWTAuth
import io.vertx.groovy.ext.web.handler.JWTAuthHandler
import io.vertx.groovy.ext.web.handler.StaticHandler

def router = Router.router(vertx)

// Create a JWT Auth Provider
def jwt = JWTAuth.create([
  keyStoreType:"jceks",
  keyStoreURI:"classpath:///keystore.jceks",
  keyStorePassword:"secret"
])

// protect the API
router.route("/api/*").handler(JWTAuthHandler.create(jwt, "/api/newToken"))

// this route is excluded from the auth handler
router.get("/api/newToken").handler({ ctx ->
  ctx.response().putHeader(io.vertx.core.http.HttpHeaders.CONTENT_TYPE, "text/plain")
  ctx.response().end(jwt.generateToken([:], [
    expiresInSeconds:60
  ]))
})

// this is the secret API
router.get("/api/protected").handler({ ctx ->
  ctx.response().putHeader(io.vertx.core.http.HttpHeaders.CONTENT_TYPE, "text/plain")
  ctx.response().end("a secret you should keep for yourself...")
})

// Serve the non private static pages
router.route().handler(StaticHandler.create())

vertx.createHttpServer().requestHandler(router.&accept).listen(8080)