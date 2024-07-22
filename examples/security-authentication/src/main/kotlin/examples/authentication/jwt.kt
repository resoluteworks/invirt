package examples.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier.BaseVerification
import com.auth0.jwt.algorithms.Algorithm
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
private val verifier = (
    JWT.require(algorithm)
        .acceptLeeway(0)
        as BaseVerification
    )
    .build(Clock.systemUTC())

fun jwtAccessToken(email: String, expires: Instant): String {
    return JWT.create()
        .withIssuer("app")
        .withSubject(email)
        .withExpiresAt(expires)
        .sign(algorithm)
}

fun verifyJwt(jwtToken: String): DecodedJWT {
    return verifier.verify(jwtToken)
}
