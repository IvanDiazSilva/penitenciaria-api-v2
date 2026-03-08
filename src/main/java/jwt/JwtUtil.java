package jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import model.Usuario;

public class JwtUtil {

    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(
            "EstaEsUnaClaveSecretaMuyLargaParaJWT_penitenciaria123".getBytes()
    );

    private static final long EXPIRATION_MS = 30 * 60 * 1000;

    public static String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .setSubject(usuario.getUsername())
                .claim("rol", usuario.getRol())
                .setIssuedAt(ahora)
                .setExpiration(expiracion)
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public static boolean validarToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // ¡FIX: 4 LÍNEAS FALTANTES!
    public static String getRol(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("rol", String.class);  // ← Extrae claim "rol" del JWT
        } catch (Exception e) {
            System.err.println("Error getRol: " + e.getMessage());
            return null;
        }
    }
}
