package com.wildlifedb.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import com.wildlifedb.api.ApiResponse;
import com.wildlifedb.entity.Species;
import com.wildlifedb.exception.ResourceNotFoundException;
import com.wildlifedb.service.BackendService;
import com.wildlifedb.repository.LocationRepository;
import com.wildlifedb.repository.SpeciesRepository;
import com.wildlifedb.util.insertTaxonomy;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/api/v1")
@RestController
@Tag(name = "Species", description = "Species and basic service lookup endpoints")
public class RestBackendController {
    @Autowired BackendService backendService;
    @Autowired SpeciesRepository speciesRepository;
    @Autowired LocationRepository locationRepository;

    @GetMapping("/hello")
    @Operation(summary = "Check API availability")
    public ApiResponse<String> hello() {
        return ApiResponse.success("Hello World!");
    }

    @GetMapping("/helloService")
    @Operation(summary = "Check service-layer availability")
    public ApiResponse<String> helloService() {
        return ApiResponse.success(backendService.hello());
    }

    @GetMapping("/user")
    @Hidden
    public ApiResponse<String> addUser() {
        return ApiResponse.success(backendService.addUser());
    }

    insertTaxonomy ins = new insertTaxonomy();

    @GetMapping("/insertCodex")
    @Hidden
    public ApiResponse<String> insertData() throws IOException {
        ins.insert(backendService);
        return ApiResponse.success("succsess");
    }

    @GetMapping("/insertUsers")
    @Hidden
    public ApiResponse<String> insertusers() throws IOException {
        ins.insertUsers(backendService);
        return ApiResponse.success("users inserted");
    }

    @GetMapping("/insertReports")
    @Hidden
    public ApiResponse<String> insertReports() {
        ins.insertReports(backendService, locationRepository);
        return ApiResponse.success("reports inserted");
    }

    @GetMapping("/updatelocs")
    @Hidden
    public ApiResponse<String> updatelocs() {
        ins.updateLoc(backendService);
        return ApiResponse.success("locs updated");
    }

    @GetMapping("/getspecies")
    @Operation(
            summary = "Get a random species",
            description = "Returns the scientific ID of one randomly selected species.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Random species ID returned"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "No species available or unexpected server error")
    })
    public ApiResponse<String> getSpec() {
        return ApiResponse.success(backendService.getrandomSpecies().getSpeciesId());
    }

    @GetMapping("/species/{id}")
    @Operation(
            summary = "Get species details",
            description = "Returns basic taxonomy and extinction details for a scientific species ID.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "200",
                description = "Species details returned"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "404",
                description = "Species not found"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "Unexpected server error")
    })
    public ApiResponse<String> getSpecSpec(
            @Parameter(description = "Scientific species ID", example = "Panthera leo")
            @PathVariable String id) {
        Species species = speciesRepository.findBySpeciesId(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Species not found with id: " + id));
        String details = "ID: " + species.getSpeciesId()
                + "\nGenus: " + species.getGenus().getGenusId()
                + "\nExtict Status: " + species.getExtinctStatus()
                + "\nCommon Name: " + species.getCommonName();
        return ApiResponse.success(details);
    }
}
