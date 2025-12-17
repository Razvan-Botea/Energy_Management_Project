package com.example.demo.controllers;

import com.example.demo.dtos.LoginRequestDTO;
import com.example.demo.dtos.LoginResponseDTO;
import com.example.demo.dtos.RegisterRequestDTO;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.services.CredentialDetailsServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "1. Authentication API", description = "Endpoint-uri pentru autentificare, inregistrare interna si validare token")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private CredentialDetailsServiceImpl credentialService;

    @Operation(summary = "Autentificaun utilizator",
        description = "Autentifica un utilizator pe baza numelui si a parolei si returneaza un token JWT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autentificare reusita", content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Date de intrare invalide (ex: campuri goale)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Autentificare esuata (ex: username sau parola gresite", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.username(),
                        loginRequest.password()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        return ResponseEntity.ok(new LoginResponseDTO(loginRequest.username(), jwt, role));
    }

    @Operation(summary = "Inregistreaza un nou user (doar de catre un admin deja logat)", description = "Punct final intern apelat de 'user-service' pentru a crea un nou user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilizator inregistrat cu succes"),
            @ApiResponse(responseCode = "400", description = "Inregistrarea a esuat (ex: username-ul exista deja)")
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        try {
            credentialService.register(registerRequest);
            return ResponseEntity.ok("User registered successfully!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Valideaza un token JWT",
        description = "Endpoint intern apelat de traefik pentru a valida un token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token-ul este valid"),
            @ApiResponse(responseCode = "401", description = "Token-ul lipseste, este invald, sau a expirat.")
    })
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(HttpServletRequest request) {
        try {
            String jwt = getJwtFromRequest(request);
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Operation(summary = "Șterge o acreditare (Doar uz intern)",
            description = "Punct final intern apelat de 'user-service' pentru a șterge o acreditare.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Acreditare ștearsă cu succes"),
            @ApiResponse(responseCode = "404", description = "Utilizatorul nu a fost găsit")
    })
    @DeleteMapping("/delete/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        try {
            credentialService.deleteByUsername(username);
            return ResponseEntity.ok("Credential deleted successfully!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
