package com.microservice.authservice.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.*;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

@Service
public class TokenService {

    private final String secret = "super-secret-key-should-be-256-bits-minimum!!";

    public String generateToken(OAuth2User oAuth2User) {
        try {
            JWSSigner signer = new MACSigner(secret.getBytes());

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(oAuth2User.getName())
                    .claim("email", oAuth2User.getAttribute("email"))
                    .claim("name", oAuth2User.getAttribute("name"))
                    .expirationTime(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claimsSet
            );

            signedJWT.sign(signer);
            return signedJWT.serialize();

        } catch (Exception e) {
            throw new RuntimeException("Token generation failed", e);
        }
    }

    public Map<String, Object> validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(secret.getBytes());

            if (signedJWT.verify(verifier)) {
                Date exp = signedJWT.getJWTClaimsSet().getExpirationTime();
                if (exp.after(new Date())) {
                    return signedJWT.getJWTClaimsSet().getClaims();
                }
            }
        } catch (ParseException | JOSEException e) {
            e.printStackTrace();
        }
        return null;
    }
}
