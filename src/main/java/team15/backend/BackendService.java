package team15.backend;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import team15.backend.entity.*;
import team15.backend.repository.*;
import java.util.List;

@Service
public class BackendService {

    @Autowired UserRepository userRepository;
    @Autowired SpeciesRepository speciesRepository;
    @Autowired FamilyRepository familyRepository;
    @Autowired GenusRepository genusRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired PhylumRepository phylumRepository;
    @Autowired TaxonomyClassRepository classRepository;
    @Autowired ReportRepository reportRepository;
    @Autowired LocationRepository locationRepository;
    public String hello() {
        return "Hello World from Service";
    }

    // public Integer findReportIdbyUserId(User userid){
    //     return reportRepository.findReportIdbyUserId(userid.getUserId()).getReportId();
    // }

    public User getrandomUser(){
        
        List<User> optionalEntity = userRepository.findAll();
        int rand = (int) (Math.random()*optionalEntity.size());
        return optionalEntity.get(rand);
    }

    public Species getrandomSpecies(){
        List<Species> optionalEntity = speciesRepository.findAll();
        int rand = (int) (Math.random()*optionalEntity.size());
        return optionalEntity.get(rand);
    }

    public List<Location> getallLocations(){
        
        List<Location> optionalEntity = locationRepository.findAll();
        return optionalEntity;
    }
    public List<Report> getAllReports(){
        System.out.println("checkpoin1.5");
        List<Report> optionalEntity = reportRepository.findAll();
        System.out.println("checkpoin1.7");
        return optionalEntity;
    }

    public Report addReport(User UserId, Species species){
        Report newReport = new Report();
        newReport.setUser(UserId);
        newReport.setSpecies(species);
        newReport.setVerified(UserId.getVerifier());

        Report finalrep = reportRepository.save(newReport);
        return finalrep;
    }
    public Report addReport(User UserId, Species species, Boolean verfied, Integer age, OffsetDateTime time, Location loc){
        Random rand = new Random();
        Report newReport = new Report();
        newReport.setUser(UserId);
        newReport.setSpecies(species);
        if(UserId.getVerifier()){
            newReport.setVerified(true);
            newReport.setVerifierUser(UserId);
        }else if(verfied==true){
            newReport.setVerified(true);
            List<User> allver = userRepository.findAllverfied(true);
            User veruser = allver.get(rand.nextInt(allver.size() - 1 +1) + 0);
            newReport.setVerifierUser(veruser);
        }else{
            newReport.setVerified(false);
            newReport.setVerifierUser(userRepository.findById(-1).get());
        }
        newReport.setAgeApproximation(age);
        newReport.setDateTime(time);
        newReport.setLocation(loc);

        Report finalrep = reportRepository.save(newReport);
        return finalrep;
    }
    public Report addReport(User UserId, Species species, Boolean verfied){
        Random rand = new Random();
        Report newReport = new Report();
        newReport.setUser(UserId);
        newReport.setSpecies(species);
        if(UserId.getVerifier()){
            newReport.setVerified(true);
            newReport.setVerifierUser(UserId);
        }else if(verfied==true){
            newReport.setVerified(true);
            List<User> allver = userRepository.findAllverfied(true);
            User veruser = allver.get(rand.nextInt(allver.size() - 1 +1) + 0);
            newReport.setVerifierUser(veruser);
        }else{
            newReport.setVerified(false);
            newReport.setVerifierUser(userRepository.findById(-1).get());
        }
        // newReport.setLocation(locId);

        Report finalrep = reportRepository.save(newReport);
        return finalrep;
    }

    public void updateReportImage(Integer ReportId, Image image){
        Optional<Report> optionalEntity = reportRepository.findByReportId(ReportId);
        if (optionalEntity.isPresent()) {
            Report entityToUpdate = optionalEntity.get();
            entityToUpdate.setImage(image);
            reportRepository.save(entityToUpdate);
            // ... proceed to update
        } else {
            // Handle case where entity is not found
        }
    }
    public void updateReportSpecies(Integer ReportId, Species spec){
        Optional<Report> optionalEntity = reportRepository.findByReportId(ReportId);
        if (optionalEntity.isPresent()) {
            Report entityToUpdate = optionalEntity.get();
            entityToUpdate.setSpecies(spec);
            reportRepository.save(entityToUpdate);
            // ... proceed to update
        } else {
            // Handle case where entity is not found
        }
    }

    public void updateReportVerified(Integer ReportId, boolean verified){
        Optional<Report> optionalEntity = reportRepository.findByReportId(ReportId);
        if (optionalEntity.isPresent()) {
            Report entityToUpdate = optionalEntity.get();
            entityToUpdate.setVerified(verified);
            reportRepository.save(entityToUpdate);
            // ... proceed to update
        } else {
            // Handle case where entity is not found
        }
    }

    public void updateReportComment(Integer ReportId, String comment){
        Optional<Report> optionalEntity = reportRepository.findByReportId(ReportId);
        if (optionalEntity.isPresent()) {
            Report entityToUpdate = optionalEntity.get();
            entityToUpdate.setComment(comment);
            reportRepository.save(entityToUpdate);
            // ... proceed to update
        } else {
            // Handle case where entity is not found
        }
    }

    public void updateReportAge(Integer ReportId, Integer age){
        Optional<Report> optionalEntity = reportRepository.findByReportId(ReportId);
        if (optionalEntity.isPresent()) {
            Report entityToUpdate = optionalEntity.get();
            entityToUpdate.setAgeApproximation(age);
            reportRepository.save(entityToUpdate);
            // ... proceed to update
        } else {
            // Handle case where entity is not found
        }
    }

    public void updateReportTime(Integer ReportId, OffsetDateTime dateTime){
        Optional<Report> optionalEntity = reportRepository.findByReportId(ReportId);
        if (optionalEntity.isPresent()) {
            Report entityToUpdate = optionalEntity.get();
            entityToUpdate.setDateTime(dateTime);
            reportRepository.save(entityToUpdate);
            // ... proceed to update
        } else {
            // Handle case where entity is not found
        }
    }  
    
    public void updateReportLongLat(Integer ReportId, Float longitude, Float latitude){
        Optional<Report> optionalEntity = reportRepository.findByReportId(ReportId);
        if (optionalEntity.isPresent()) {
            Report entityToUpdate = optionalEntity.get();
            entityToUpdate.setLongitude(longitude);
            entityToUpdate.setLatitude(latitude);
            reportRepository.save(entityToUpdate);
            // ... proceed to update
        } else {
            // Handle case where entity is not found
        }
    }  

    public void updateReportLocation(Integer ReportId, Location loc){
        Optional<Report> optionalEntity = reportRepository.findByReportId(ReportId);
        if (optionalEntity.isPresent()) {
            Report entityToUpdate = optionalEntity.get();
            entityToUpdate.setLocation(loc);
            reportRepository.save(entityToUpdate);
            // ... proceed to update
        } else {
            // Handle case where entity is not found
        }
    }  



    public String addUser() {
        User newUser = new User();
        newUser.setUserId("userId1234");
        newUser.setEmail("email@email.com");
        newUser.setPassword("someHashedPassword");
        newUser.setVerifier(true);
        userRepository.save(newUser);
        return "New user successfully created!";
    }
    public void addUser(String userid, String email, String password, boolean verifier) {
        User newUser = new User();
        newUser.setUserId(userid);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setVerifier(verifier);
        userRepository.save(newUser);
    }

    public void addSpecies(String speciesId, Genus g) {
        Species newSpecies = new Species();
        newSpecies.setSpeciesId(speciesId);
        newSpecies.setGenus(g);
        speciesRepository.save(newSpecies);
    }

    public void updateSpecies(String SpeciesId, String commonname, boolean extinctStatus){
        Optional<Species> optionalEntity = speciesRepository.findBySpeciesId(SpeciesId);
        if (optionalEntity.isPresent()) {
            Species entityToUpdate = optionalEntity.get();
            entityToUpdate.setCommonName(commonname);
            entityToUpdate.setExtinctStatus(extinctStatus);
            speciesRepository.save(entityToUpdate);
            // ... proceed to update
        } else {
            // Handle case where entity is not found
        }
    }

    
    public void addFamily(String Familyid, TaxonomyOrder order) {
        Family newFamily = new Family();
        newFamily.setFamilyId(Familyid);
        newFamily.setTaxOrder(order);
        familyRepository.save(newFamily);
    }
    
    public void addGenus(String genusId, Family f) {
        Genus newGenus = new Genus();
        newGenus.setGenusId(genusId);
        newGenus.setFamily(f);
        genusRepository.save(newGenus);
    }
    
    public void addOrder(String Orderid, TaxonomyClass tc) {
        TaxonomyOrder newOrder = new TaxonomyOrder();
        newOrder.setOrderId(Orderid);
        newOrder.setTaxonomyClass(tc);
        orderRepository.save(newOrder);
    }
    
    public void addPhylum(String phylumid, String kingdom) {
        Phylum newPhylum = new Phylum();
        newPhylum.setPhylumId(phylumid);
        newPhylum.setKingdomId(kingdom);
        phylumRepository.save(newPhylum);
    }
    
    public void addTaxonomyClass(String classid, Phylum p) {
        TaxonomyClass newTaxonomyClass = new TaxonomyClass();
        newTaxonomyClass.setClassId(classid);
        newTaxonomyClass.setPhylum(p);
        classRepository.save(newTaxonomyClass);
    }
    
}
