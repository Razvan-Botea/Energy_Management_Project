package com.example.demo.controllers;

import com.example.demo.dtos.DeviceDTO;
import com.example.demo.dtos.DeviceDetailsDTO;
import com.example.demo.services.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/devices")
@Validated
@CrossOrigin(origins = "*")
@Tag(name = "3. Device management API", description = "Endpoint-uri pentru operatiuni CRUD asupra dispozitivelor")
@SecurityRequirement(name = "Bearer Authentication")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Operation(summary = "Obtine lista tuturor dispozitivelor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista returnata cu succes",
            content = @Content(array =@ArraySchema(schema = @Schema(implementation = DeviceDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Neautorizat (token invalid sau lipsa)",
            content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<DeviceDTO>> getDevices() {
        return ResponseEntity.ok(deviceService.findDevices());
    }

    @Operation(summary = "Creează un dispozitiv nou")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Dispozitiv creat cu succes", content = @Content),
            @ApiResponse(responseCode = "400", description = "Date de intrare invalide (eroare de validare)", content = @Content),
            @ApiResponse(responseCode = "401", description = "Neautorizat", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody DeviceDetailsDTO device) {
        UUID id = deviceService.insert(device);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @Operation(summary = "Obtine detaliile unui dispozitiv dupa ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dispozitiv gasit",
                    content = @Content(schema = @Schema(implementation = DeviceDetailsDTO.class))),
            @ApiResponse(responseCode = "401", description = "Neautorizat", content = @Content),
            @ApiResponse(responseCode = "404", description = "Dispozitivul nu a fost gasit", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<DeviceDetailsDTO> getDevice(@PathVariable UUID id) {
        return ResponseEntity.ok(deviceService.findDeviceById(id));
    }

    @Operation(summary = "Actualizeaza un dispozitiv existent")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Actualizare reusita",
                    content = @Content(schema = @Schema(implementation = DeviceDetailsDTO.class))),
            @ApiResponse(responseCode = "400", description = "Date de intrare invalide", content = @Content),
            @ApiResponse(responseCode = "401", description = "Neautorizat", content = @Content),
            @ApiResponse(responseCode = "404", description = "Dispozitivul nu a fost gasit", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<DeviceDetailsDTO> update(@PathVariable UUID id, @Valid @RequestBody DeviceDetailsDTO deviceDetailsDTO) {
        return ResponseEntity.ok(deviceService.update(id, deviceDetailsDTO));
    }

    @Operation(summary = "Șterge un dispozitiv")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stergere reusita", content = @Content),
            @ApiResponse(responseCode = "401", description = "Neautorizat", content = @Content),
            @ApiResponse(responseCode = "404", description = "Dispozitivul nu a fost gasit", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deviceService.delete(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Obtine toate dispozitivele asociate unui utilizator")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista returnata cu succes",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DeviceDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Neautorizat", content = @Content)
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DeviceDTO>> getDevicesForUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(deviceService.findDevicesByUserId(userId));
    }
}