package com.example.demo.controllers;

import com.example.demo.dtos.UserDTO;
import com.example.demo.dtos.UserDetailsDTO;
import com.example.demo.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Validated
@CrossOrigin
@Tag(name = "2. User Management API", description = "Endpoint-uri pentru operatiuni CRUD asupra utilizatorilor")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Obtine lista tuturor utilizatorilor (doar pentru admin)",
        description = "Returneaza o lista cu toti utilizatorii din sistem. Necesita token de admin.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista returnata cu succes",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Neautorizat (token invalid sau lipsa)",
            content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<UserDTO>> getUsers() {
        return ResponseEntity.ok(userService.findUsers());
    }

    @Operation(summary = "Creeaza un utilizator nou (atat Admin cat si Client). Acesta apeleaza intern 'auth-service pentru a crea si acreditarea.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Utilizator creat cu succes", content = @Content),
            @ApiResponse(responseCode = "400", description = "date de intrare invalide (ex: username-ul exista deja sau validare esuata)", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody UserDetailsDTO user) {
        UUID id = userService.insert(user);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @Operation(summary = "Obtine detaliile unui utilizator după ID (doar Admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Utilizator gasit",
                    content = @Content(schema = @Schema(implementation = UserDetailsDTO.class))),
            @ApiResponse(responseCode = "401", description = "Neautorizat", content = @Content),
            @ApiResponse(responseCode = "404", description = "Utilizatorul nu a fost gasit", content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/{id}")
    public ResponseEntity<UserDetailsDTO> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findUserById(id));
    }

    @Operation(summary = "Actualizeaza un utilizator existent (doar Admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Actualizare reusita",
                    content = @Content(schema = @Schema(implementation = UserDetailsDTO.class))),
            @ApiResponse(responseCode = "400", description = "Date de intrare invalide", content = @Content),
            @ApiResponse(responseCode = "401", description = "Neautorizat", content = @Content),
            @ApiResponse(responseCode = "404", description = "Utilizatorul nu a fost gasit", content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/{id}")
    public ResponseEntity<UserDetailsDTO> update(@PathVariable UUID id, @Valid @RequestBody UserDetailsDTO userDetailsDTO) {
        return ResponseEntity.ok(userService.update(id, userDetailsDTO));
    }

    @Operation(summary = "Sterge un utilizator (doar Admin)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stergere reusita", content = @Content),
            @ApiResponse(responseCode = "401", description = "Neautorizat", content = @Content),
            @ApiResponse(responseCode = "404", description = "Utilizatorul nu a fost gasit", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.ok().build();
    }
}