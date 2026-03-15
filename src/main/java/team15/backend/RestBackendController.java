package team15.backend;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

import team15.backend.entity.Species;
import team15.backend.repository.LocationRepository;
import team15.backend.repository.SpeciesRepository;
import team15.backend.taxonomyClassfiles.insertTaxonomy;


@RequestMapping("/api/v1")
@RestController
public class RestBackendController {
    @Autowired BackendService backendService;
    @Autowired SpeciesRepository speciesRepository;
    @Autowired LocationRepository locationRepository;
    @GetMapping("/hello")
    public String hello() {return "Hello World!";}

    @GetMapping("/helloService")
    public String helloService() {return backendService.hello();}

    @GetMapping("/user")
    public String addUser() {return backendService.addUser();}

    insertTaxonomy ins = new insertTaxonomy();
    @GetMapping("/insertCodex")
    public String insertData() throws IOException {ins.insert(backendService); return "succsess";}

    @GetMapping("/insertUsers")
    public String insertusers() throws IOException {ins.insertUsers(backendService); return "users inserted";}

    @GetMapping("/insertReports")
    public String insertReports() {ins.insertReports(backendService, locationRepository); return "reports inserted";}

    @GetMapping("/updatelocs")
    public String updatelocs() {ins.updateLoc(backendService); return "locs updated";}

    @GetMapping("/getspecies")
    public String getSpec() {return backendService.getrandomSpecies().getSpeciesId();}

    @GetMapping("/species/{id}")
    public String getSpecSpec(@PathVariable String id) {
        Optional<Species> n = speciesRepository.findBySpeciesId(id);
        Species a = n.get();
        return "ID: " + a.getSpeciesId()+ "\nGenus: "+a.getGenus().getGenusId()+"\nExtict Status: "+a.getExtinctStatus()+"\nCommon Name: "+a.getCommonName();
    }


}
