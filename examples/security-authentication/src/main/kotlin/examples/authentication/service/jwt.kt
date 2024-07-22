package examples.authentication.service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier.BaseVerification
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.DecodedJWT
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Clock
import java.time.Instant

private val keyPair = KeyPairGenerator.getInstance("RSA").genKeyPair()
private val algorithm: Algorithm = Algorithm.RSA256(
    keyPair.public as RSAPublicKey,
    keyPair.private as RSAPrivateKey
)

fun jwtAccessToken(email: String, expires: Instant, claims: Map<String, String> = emptyMap()): String {
    val jwt = JWT.create()
        .withIssuer("app")
        .withSubject(email)
    claims.forEach { (key, value) -> jwt.withClaim(key, value) }
    return jwt
        .withExpiresAt(expires)
        .sign(algorithm)
}

fun accessToken(
    email: String,
    role: String,
    ttlMinutes: Long
): String = jwtAccessToken(email, Instant.now().plusSeconds(60 * ttlMinutes), mapOf("role" to role))

fun verifyJwt(jwtToken: String, clock: Clock = Clock.systemUTC()): TokenVerification = try {
    val verifier = (JWT.require(algorithm) as BaseVerification)
        .build(clock)
    val decodedJwt = verifier.verify(jwtToken)
    TokenVerification.Success(decodedJwt)
} catch (e: TokenExpiredException) {
    TokenVerification.Failed
}

sealed class TokenVerification {
    class Success(val jwt: DecodedJWT) : TokenVerification()
    data object Failed : TokenVerification()
}
