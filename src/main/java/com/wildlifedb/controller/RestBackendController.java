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

@RequestMapping("/api/v1")
@RestController
public class RestBackendController {
    @Autowired BackendService backendService;
    @Autowired SpeciesRepository speciesRepository;
    @Autowired LocationRepository locationRepository;

    @GetMapping("/hello")
    public ApiResponse<String> hello() {
        return ApiResponse.success("Hello World!");
    }

    @GetMapping("/helloService")
    public ApiResponse<String> helloService() {
        return ApiResponse.success(backendService.hello());
    }

    @GetMapping("/user")
    public ApiResponse<String> addUser() {
        return ApiResponse.success(backendService.addUser());
    }

    insertTaxonomy ins = new insertTaxonomy();

    @GetMapping("/insertCodex")
    public ApiResponse<String> insertData() throws IOException {
        ins.insert(backendService);
        return ApiResponse.success("succsess");
    }

    @GetMapping("/insertUsers")
    public ApiResponse<String> insertusers() throws IOException {
        ins.insertUsers(backendService);
        return ApiResponse.success("users inserted");
    }

    @GetMapping("/insertReports")
    public ApiResponse<String> insertReports() {
        ins.insertReports(backendService, locationRepository);
        return ApiResponse.success("reports inserted");
    }

    @GetMapping("/updatelocs")
    public ApiResponse<String> updatelocs() {
        ins.updateLoc(backendService);
        return ApiResponse.success("locs updated");
    }

    @GetMapping("/getspecies")
    public ApiResponse<String> getSpec() {
        return ApiResponse.success(backendService.getrandomSpecies().getSpeciesId());
    }

    @GetMapping("/species/{id}")
    public ApiResponse<String> getSpecSpec(@PathVariable String id) {
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
