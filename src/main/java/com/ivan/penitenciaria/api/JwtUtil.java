package com.ivan.penitenciaria.api;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtUtil {

    // Clave secreta simple para el proyecto (en un proyecto real iría en config)
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(
            "EstaEsUnaClaveSecretaMuyLargaParaJWT_penitenciaria123".getBytes()
    );

    // 30 minutos
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
}
