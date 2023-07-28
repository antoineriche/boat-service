package com.ariche.boatapi.web.controllers;

import com.ariche.boatapi.security.CAuthorityNames;
import com.ariche.boatapi.service.boatmanager.BoatService;
import com.ariche.boatapi.service.boatmanager.dto.BoatDTO;
import com.ariche.boatapi.service.storage.dto.FileResourceDTO;
import com.ariche.boatapi.web.errors.BoatAPIError;
import com.ariche.boatapi.web.utils.ResponseUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(path = "/api/v1/boats")
@RolesAllowed({ CAuthorityNames.ROLE_ADMIN, CAuthorityNames.ROLE_USER })
@RequiredArgsConstructor
public class BoatController {

    private final BoatService boatService;

    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Find all boats",
        description = "Find all boats, paginated, sorted and ordered"
    )
    @GetMapping(
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponse(
        responseCode = "200",
        description = "Ok"
    )
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = BoatAPIError.class))
    )
    @RolesAllowed(CAuthorityNames.ROLE_USER)
    public ResponseEntity<Page<BoatDTO>> findAllBoats(@ParameterObject Pageable pageable) {
        log.debug("[GET] Find all boats (size={};page={})", pageable.getPageSize(), pageable.getPageNumber());
        final Page<BoatDTO> boats = boatService.findALlBoats(pageable);
        return ResponseEntity.ok(boats);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Create a boat",
        description = "Create a new boat"
        //TODO: add security item for swagger description , security =
    )
    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponse(
        responseCode = "201",
        description = "Ok",
        content = @Content(schema = @Schema(implementation = BoatDTO.class)),
        headers = @Header(name = HttpHeaders.LOCATION, description = "Link to the created request", schema = @Schema(implementation = String.class))
    )
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = BoatAPIError.class))
    )
    @RolesAllowed(CAuthorityNames.ROLE_ADMIN)
    public ResponseEntity<BoatDTO> createBoat(@RequestBody @Valid BoatDTO boat) {
        log.debug("[POST] Create boat: {}", boat);
        final  BoatDTO res = boatService.createBoat(boat);
        return ResponseEntity
            .created(ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(res.id())
                .toUri())
            .body(res);
    }

    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Find a boat by id",
        description = "Find the boat identified by its id"
    )
    @GetMapping(
        path = "/{boatId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponse(
        responseCode = "200",
        description = "Ok",
        content = @Content(schema = @Schema(implementation = BoatDTO.class))
    )
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = BoatAPIError.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "Not found",
        content = @Content(schema = @Schema(implementation = BoatAPIError.class))
    )
    @RolesAllowed(CAuthorityNames.ROLE_USER)
    public ResponseEntity<BoatDTO> findBoatById(@PathVariable(name = "boatId") Long boatId) {
        log.debug("[GET] Find boat by id: {}", boatId);
        final Optional<BoatDTO> boatDTO = boatService.findBoatById(boatId);
        return ResponseUtils.wrapOrNotFound(boatDTO);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Upload an image for boat",
        description = "Upload an image for boat identified by its id"
    )
    @PostMapping(
        path = "/{boatId}/image",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.TEXT_PLAIN_VALUE
    )
    @ApiResponse(
        responseCode = "201",
        description = "Ok",
        headers = @Header(name = HttpHeaders.LOCATION, description = "Link to the uploaded image", schema = @Schema(implementation = String.class))
    )
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = BoatAPIError.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "Not found",
        content = @Content(schema = @Schema(implementation = BoatAPIError.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Bad request",
        content = @Content(schema = @Schema(implementation = BoatAPIError.class))
    )
    @ApiResponse(
        responseCode = "500",
        description = "Internal server error during upload",
        content = @Content(schema = @Schema(implementation = BoatAPIError.class))
    )
    @RolesAllowed(CAuthorityNames.ROLE_ADMIN)
    public ResponseEntity<Void> uploadBoatImage(@PathVariable(name = "boatId") Long boatId,
                                                @RequestParam MultipartFile file) {
        log.debug("[POST] Post boat image for: {} ({})", boatId, file.getSize());
        boatService.uploadImageForBoat(boatId, file);
        return ResponseEntity
            .created(ServletUriComponentsBuilder.fromCurrentRequest()
                .buildAndExpand()
                .toUri())
            .build();
    }

    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Download image for boat",
        description = "Download image of boat identified by its id"
    )
    @GetMapping(
        path = "/{boatId}/image",
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @ApiResponse(
        responseCode = "200",
        description = "Ok",
        content = @Content(schema = @Schema(implementation = byte[].class))
    )
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = BoatAPIError.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "Not found",
        content = @Content(schema = @Schema(implementation = BoatAPIError.class))
    )
    @RolesAllowed(CAuthorityNames.ROLE_USER)
    public ResponseEntity<byte[]> downloadBoatImage(@PathVariable(name = "boatId") Long boatId) {
        log.debug("[GET] Download boat image for: {}", boatId);
        final Optional<FileResourceDTO> optImage = boatService.downloadImageForBoat(boatId);

        if (optImage.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        final String mimeType = StringUtils.isNotBlank(optImage.get().mimeType()) ?
            optImage.get().mimeType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity
            .status(HttpStatus.OK)
            .header(HttpHeaders.CONTENT_TYPE, mimeType)
            .body(optImage.get().bytes());
    }

    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Update a boat",
        description = "Update the boat identified by its id"
    )
    @PutMapping(
        path = "/{boatId}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponse(
        responseCode = "200",
        description = "Ok",
        content = @Content(schema = @Schema(implementation = BoatDTO.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Bad request",
        content = @Content(schema = @Schema(implementation = BoatAPIError.class))
    )
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = BoatAPIError.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "Not found",
        content = @Content(schema = @Schema(implementation = BoatAPIError.class))
    )
    @RolesAllowed("ROLE_ADMIN")
    public ResponseEntity<BoatDTO> updateBoatById(@PathVariable(name = "boatId") Long boatId,
                                                  @RequestBody @Valid BoatDTO boat) {
        log.debug("[PUT] Update boat by id: {}", boatId);
        final BoatDTO res = boatService.updateBoat(boatId, boat);
        return ResponseEntity.ok(res);
    }

    @ResponseStatus(HttpStatus.OK)
    @Operation(
        summary = "Delete a boat",
        description = "Delete the boat identified by its id"
    )
    @DeleteMapping(
        path = "/{boatId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponse(
        responseCode = "200",
        description = "Ok"
    )
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized",
        content = @Content(schema = @Schema(implementation = BoatAPIError.class))
    )
    @RolesAllowed("ROLE_ADMIN")
    public ResponseEntity<Void> deleteBoatById(@PathVariable(name = "boatId") Long boatId) {
        log.debug("[DELETE] Delete boat by id: {}", boatId);
        boatService.deleteBoatById(boatId);
        return ResponseEntity.ok().build();
    }

}
