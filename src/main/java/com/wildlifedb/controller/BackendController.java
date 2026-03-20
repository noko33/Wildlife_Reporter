package com.wildlifedb.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


import org.springframework.ui.Model;


import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

import com.wildlifedb.entity.Species;
import com.wildlifedb.entity.User;
import com.wildlifedb.entity.Report;
import com.wildlifedb.repository.LocationRepository;
import com.wildlifedb.repository.ReportRepository;
import com.wildlifedb.repository.SpeciesRepository;
import com.wildlifedb.repository.UserRepository;
import com.wildlifedb.entity.Location;
import com.wildlifedb.service.BackendService;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@RequestMapping("/api/v1")
@Controller
public class BackendController {
    @Autowired BackendService backendService;
    @Autowired SpeciesRepository speciesRepository;
    @Autowired UserRepository userRepository;
    @Autowired ReportRepository reportRepository;
    @Autowired LocationRepository locationRepository;

    @GetMapping("/greeting")
    public String greeting(Model model) {
        String name = "nathan";
		model.addAttribute("name", name);
		return "greeting";
	}
    @GetMapping("/Mainpage/user={userId}")
    public String mainpage(Model model, @PathVariable String userId){
        
        Species randspec = backendService.getrandomSpecies();
        String stringtoadd = "Species Scientific Name: "+randspec.getSpeciesId()+
                                "\nSpecies Common Name: "+randspec.getCommonName()+
                                "\nAssosciated with Genus: "+randspec.getGenus().getGenusId()+
                                "\nSpecies Exctinct Status: "+randspec.getExtinctStatus();
        model.addAttribute("stringtoadd", stringtoadd);
        Optional<User> curr = userRepository.findByUserId(userId);
        
        if(userId.equals("unknown")){
            return "mainpage";
        }else if(curr.isPresent()){
            model.addAttribute("userId", userId);

            return "mainpagewithhandle";

        }else{
            return "mainpage";
        }
    }
    @GetMapping("/Query/user={userId}")
    public String query2(Model model,@PathVariable String userId){
        // List<Report> rep =reportRepository.findReportsbyUserId(2316);
        User u;
        if(userRepository.findByUserId(userId).isPresent()){
            u = userRepository.findByUserId(userId).get();
        }else{
            u = userRepository.findById(-1).get();
        }
        if(u.getVerifier()){
            model.addAttribute("verifier", "true");
        }else{
            model.addAttribute("verifier", "false");
        }
        model.addAttribute("fuserId", userId);
        if(userId.equals("unknown")){
            return "querybasic";

        }else{
            return "querybasicwithhandle";
        }
    }
  

    @PostMapping(value="/Query/user={userId}")
    public String queryRes(@PathVariable String userId, Model model,@RequestParam(name="input1") String ScienName, @RequestParam(name="input2") String ComName,
     @RequestParam(name="input3") String RepId, @RequestParam(name="input4") String quserId, 
    @RequestParam(name="input5") String age, @RequestParam(name="input6") String time1,@RequestParam(name="input7") String time2,@RequestParam(name="input8") String city
    ,@RequestParam(name="input9") String username){
        List<Report> otherresult =new ArrayList<>();
        List<Report> result = new ArrayList<>();
        List<Report> toRemove = new ArrayList<>();

        if(!ScienName.equals("") && ComName.equals("")){
            result = reportRepository.findReportsbySpecies(ScienName);
        }
        if(!ComName.equals("") && ScienName.equals("")){
            List<Species> temp = speciesRepository.findSpeciesbyCom(ComName);
            for (Species a : temp) {
                for (Report b : reportRepository.findReportsbySpecies(a.getSpeciesId())) {
                    otherresult.add(b);    
                }
            }
            if(result.isEmpty()){
                result=otherresult;
            }else{
                toRemove= new ArrayList<>();
                for (Report a : result) {
                    if(!otherresult.contains(a)){
                        toRemove.add(a);
                    }
                }
                result.removeAll(toRemove);
            }   
        }
        
        if(!city.equals("")){
            List<Location> temp = locationRepository.findLocationsByCity(city);
            otherresult =new ArrayList<>();
            for (Location a : temp) {
                for (Report b : reportRepository.findReportsbyLocation(a.getLocationId())) {
                    otherresult.add(b);    
                }
            }
            if(result.isEmpty()){
                result=otherresult;
            }else{
                toRemove= new ArrayList<>();
                for (Report a : result) {
                    if(!otherresult.contains(a)){
                        toRemove.add(a);
                    }
                }
                result.removeAll(toRemove);
            }   
        }
        if(!quserId.equals("")&&username.equals("")){
            otherresult = reportRepository.findReportsbyUserId( Integer.parseInt(quserId));
            if(result.isEmpty()){
                result=otherresult;
            }else{
                toRemove= new ArrayList<>();
                for (Report a : result) {
                    if(!otherresult.contains(a)){
                        toRemove.add(a);
                    }
                }
                result.removeAll(toRemove);
            }   
        }
        if(!username.equals("") && quserId.equals("")&&userRepository.findByUserId(username).isPresent()){
            Optional<User> temp2 = userRepository.findByUserId(username);
            otherresult =new ArrayList<>();
            List<Report> temp1 = reportRepository.findReportsbyUserId(temp2.get().getId());
            for (Report b : temp1) {
                otherresult.add(b);    
            }

            
            if(result.isEmpty()){
                result=otherresult;
            }else{
                toRemove= new ArrayList<>();
                for (Report a : result) {
                    if(!otherresult.contains(a)){
                        toRemove.add(a);
                    }
                }
                result.removeAll(toRemove);
            }   
        }
        if(!RepId.equals("")){
            otherresult = reportRepository.findReportsbyReportId( Integer.parseInt(RepId));
            if(result.isEmpty()){
                result=otherresult;
            }else{
                toRemove= new ArrayList<>();
                for (Report a : result) {
                    if(!otherresult.contains(a)){
                        toRemove.add(a);
                    }
                }
                result.removeAll(toRemove);
            }   
        }
        if(!age.equals("")){
            otherresult = reportRepository.findReportsbyAge( Integer.parseInt(age));
            if(result.isEmpty()){
                result=otherresult;
            }else{
                toRemove= new ArrayList<>();
                for (Report a : result) {
                    if(!otherresult.contains(a)){
                        toRemove.add(a);
                    }
                }
                result.removeAll(toRemove);
            }   
        }
        if(!time1.equals("")&&!time2.equals("")){
            OffsetDateTime dateTime1 = OffsetDateTime.parse(time1);
            OffsetDateTime dateTime2 = OffsetDateTime.parse(time2);

            otherresult = reportRepository.findReportsbyTime( dateTime1,dateTime2);
            if(result.isEmpty()){
                result=otherresult;
            }else{
                toRemove= new ArrayList<>();
                for (Report a : result) {
                    if(!otherresult.contains(a)){
                        toRemove.add(a);
                    }
                }
                result.removeAll(toRemove);
            }   
        }
        User u;
        if(userRepository.findByUserId(userId).isPresent()){
            u = userRepository.findByUserId(userId).get();
        }else{
            u = userRepository.findById(-1).get();
        }
        if(u.getVerifier()){
            model.addAttribute("verifier", "true");
        }else{
            model.addAttribute("verifier", "false");
        }
        int i = 1;
        for (Report b : result) {
            if(b.getVerified()==true||u.getVerifier()){
                model.addAttribute("spec"+i, b.getSpecies().getSpeciesId());
                model.addAttribute("user"+i, b.getUser().getUserId());
                model.addAttribute("rep"+i, b.getReportId());
                model.addAttribute("time"+i, b.getDateTime().toString());
                model.addAttribute("age"+i, b.getAgeApproximation());
                model.addAttribute("commonName"+i, b.getSpecies().getCommonName());
                model.addAttribute("city"+i, b.getLocation().getCity());
                model.addAttribute("com"+i, b.getComment());
                model.addAttribute("verfied"+i, b.getVerified());
                i++;
            }
        }     
        
        
        model.addAttribute("fuserId", userId);
        if(userId.equals("unknown")){
            return "querybasic";

        }else{
            return "querybasicwithhandle";
        }
    }


    @GetMapping("/login/user=unknown")
    public String login(Model model){
        return "login";
    }
    @GetMapping("/loginfailure/user=unknown")
    public String loginfailure(Model model){
        return "loginfailure";
    }

    @GetMapping("/login/email={femail}password={fpassword}")
    public String loggingint(@PathVariable("femail") String email, @PathVariable("fpassword") String password, Model model){
        Optional<User> current = userRepository.findByEmail(email);
        if(current.isPresent() && current.get().getPassword().equals(password)){
            String userId = current.get().getUserId();
            model.addAttribute("fuserId",userId);
            return "success";
        }else{
            return "failure";
        }
    }

    @GetMapping("/createaccount/email={femail}password={fpassword}userId={userId}")
    public String createAccount(@PathVariable("femail") String email,@PathVariable("fpassword") String password, @PathVariable("userId") String userId,Model model){
        backendService.addUser(userId,email,password,false);
        model.addAttribute("fuserId", userId);
        return "createAccount";
    }
    
    @GetMapping("/NewReport/user={userId}")
        public String newReport(@PathVariable("userId") String userId,Model model){
        model.addAttribute("fuserId", userId);
        if(userId.equals("unknown")){
            return "newReport";

        }else{
            return "newReportwithhandle";
        }
    }

     @PostMapping(value="/NewReport/user={userId}")
    public String newReportEntry(@PathVariable String userId, Model model,@RequestParam(name="input1") String ScienName, //@RequestParam(name="input2") String ComName,
    //  @RequestParam(name="input4") String quserId, 
    @RequestParam(name="input5") String age, @RequestParam(name="input6") String time1, @RequestParam(name="input7") String comment, @RequestParam(name="input8") String city){
        User user;
        if(userId.equals("unknown")||!userRepository.findByUserId(userId).isPresent()){
            user = userRepository.findByUserId("notreal").get();
        }else{
            user = userRepository.findByUserId(userId).get();
        }
        boolean validtime;
        try {
            OffsetDateTime.parse(time1);
            validtime = true;
        } catch (DateTimeParseException e) {
            validtime= false; // Parsing failed, string is not a valid OffsetDateTime
        }

        if((speciesRepository.findBySpeciesId(ScienName).isPresent()) && userRepository.findByUserId(userId).isPresent()){
            Report curr = backendService.addReport(user, speciesRepository.findBySpeciesId(ScienName).get(), user.getVerifier());
            if(validtime){
                backendService.updateReportTime(curr.getReportId(), OffsetDateTime.parse(time1));
            }else{
                backendService.updateReportTime(curr.getReportId(), OffsetDateTime.now());
            }
            if(!age.equals("")){
                try{backendService.updateReportAge(curr.getReportId(), Integer.parseInt(age));}catch(NumberFormatException e){}
            }
            if(!city.equals("")&&locationRepository.findBycity(city).isPresent()){
                backendService.updateReportLocation(curr.getReportId(),locationRepository.findBycity(city).get());
            }
            if(!comment.equals("")){
                backendService.updateReportComment(curr.getReportId(), comment);
            }
            model.addAttribute("resultEntry", "LAST REPORT SUCCESSFULLY SUBMITTED");
        }else{
            model.addAttribute("resultEntry", "LAST REPORT FAILED TO SUBMITTED");
        }
        model.addAttribute("fuserId", userId);
        if(userId.equals("unknown")){
            return "newReport";

        }else{
            return "newReportwithhandle";
        }
    }

    @GetMapping("/myreports/user={userId}")
    public String myReports(@PathVariable("userId") String userId,Model model){
        model.addAttribute("fuserId", userId);
        if(userId.equals("unknown")){
            return "myreports";

        }else{
            List<Report> result = reportRepository.findReportsbyUserId(userRepository.findByUserId(userId).get().getId());    
            int i = 1;
            for (Report b : result) {
                model.addAttribute("spec"+i, b.getSpecies().getSpeciesId());
                model.addAttribute("user"+i, b.getUser().getUserId());
                model.addAttribute("rep"+i, b.getReportId());
                model.addAttribute("time"+i, b.getDateTime().toString());
                model.addAttribute("age"+i, b.getAgeApproximation());
                model.addAttribute("commonName"+i, b.getSpecies().getCommonName());
                model.addAttribute("city"+i, b.getLocation().getCity());
                model.addAttribute("com"+i, b.getComment());
                i++;
            }
            return "myreportswithhandle";
        }
    }

    @GetMapping(value ={"/editrep/userId={userId}/rep=","/editrep/userId={userId}/rep={repId}"})
    public String editReprot(@PathVariable("userId") String userId,@PathVariable(required = false ,value="repId") String repId, Model model){
        if(repId==null){
            model.addAttribute("fuserId", userId);
            return "rerouteMyReports";
        }else{


            Report rep = reportRepository.findByReportId(Integer.parseInt(repId)).get();
            model.addAttribute("spec", rep.getSpecies().getSpeciesId());
            model.addAttribute("user", rep.getUser().getUserId());
            model.addAttribute("rep", rep.getReportId());
            model.addAttribute("time", rep.getDateTime().toString());
            model.addAttribute("age", rep.getAgeApproximation());
            model.addAttribute("commonName", rep.getSpecies().getCommonName());
            model.addAttribute("city", rep.getLocation().getCity());
            model.addAttribute("com", rep.getComment());
            return "editReps";
        }
    }

    @PostMapping(value={"/editrep/userId={userId}/rep=","/editrep/userId={userId}/rep={repId}"})
    public String updateReport(@PathVariable("userId") String userId,@PathVariable(required = false ,value="repId") String repId, Model model,@RequestParam(name="input1") String ScienName, //@RequestParam(name="input2") String ComName,
    //  @RequestParam(name="input4") String quserId, 
    @RequestParam(name="input5") String age, @RequestParam(name="input6") String time1, @RequestParam(name="input7") String comment, @RequestParam(name="input8") String city){
        User user = userRepository.findByUserId(userId).get();
        model.addAttribute("curruserId", user);
        boolean validtime;
        try {
            OffsetDateTime.parse(time1);
            validtime = true;
        } catch (DateTimeParseException e) {
            validtime= false; // Parsing failed, string is not a valid OffsetDateTime
        }
        boolean validage;
        try {
            Integer.parseInt(age);
            validage = true;
        } catch (NumberFormatException e) {
            validage= false; // Parsing failed, string is not a valid OffsetDateTime
        }
        Optional<Report> rep = reportRepository.findByReportId(Integer.parseInt(repId));
        if(!ScienName.equals("")&&speciesRepository.findBySpeciesId(ScienName).isPresent()&&rep.isPresent()){
            backendService.updateReportSpecies(rep.get().getReportId(), speciesRepository.findBySpeciesId(ScienName).get());;
        }
        if(!age.equals("")&&validage&&rep.isPresent()){
            backendService.updateReportAge(rep.get().getReportId(), Integer.parseInt(age));;
        }
        if(!time1.equals("")&&validtime&&rep.isPresent()){
            backendService.updateReportTime(rep.get().getReportId(), OffsetDateTime.parse(time1));;
        }
        if(!city.equals("")&&rep.isPresent()&&locationRepository.findBycity(city).isPresent()){
            backendService.updateReportLocation(rep.get().getReportId(),locationRepository.findBycity(city).get());;
        }
        if(!comment.equals("")&&rep.isPresent()){
            backendService.updateReportComment(rep.get().getReportId(), comment);;
        }    


        Report rep2 = reportRepository.findByReportId(Integer.parseInt(repId)).get();
        model.addAttribute("spec", rep2.getSpecies().getSpeciesId());
        model.addAttribute("user", rep2.getUser().getUserId());
        model.addAttribute("rep", rep2.getReportId());
        model.addAttribute("time", rep2.getDateTime().toString());
        model.addAttribute("age", rep2.getAgeApproximation());
        model.addAttribute("commonName", rep2.getSpecies().getCommonName());            
        model.addAttribute("city", rep2.getLocation().getCity());
        model.addAttribute("com", rep2.getComment());
        
        return "editReps";

    }

    @GetMapping(value ={"/delrep/userId={userId}/rep=","/delrep/userId={userId}/rep={repId}"})
    public String delRep(@PathVariable("userId") String userId,@PathVariable(required = false ,value="repId") String repId, Model model){
        model.addAttribute("fuserId", userId);
        if(repId==null){
            return "rerouteMyReports";
        }else{
            
            if(reportRepository.findByReportId(Integer.parseInt(repId)).isPresent()){
                reportRepository.delete(reportRepository.findByReportId(Integer.parseInt(repId)).get());
            }
            return "rerouteMyReports";
        }
    }

    @GetMapping(value ={"/verifyrep/userId={userId}/rep=","/verifyrep/userId={userId}/rep={repId}"})
    public String verifyrep(@PathVariable("userId") String userId,@PathVariable(required = false ,value="repId") String repId, Model model){
        model.addAttribute("fuserId", userId);
        if(repId==null){
            return "rerouteQuery";
        }else{
            if(reportRepository.findByReportId(Integer.parseInt(repId)).isPresent()){
                backendService.updateReportVerified(reportRepository.findByReportId(Integer.parseInt(repId)).get().getReportId(), true);
            }
            return "rerouteQuery";
        }
    }
    

}
