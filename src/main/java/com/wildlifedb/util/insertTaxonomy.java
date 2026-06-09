package com.wildlifedb.util;
import java.io.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import com.wildlifedb.service.BackendService;
import com.wildlifedb.entity.*;

import java.util.List;
import java.util.Random;

import net.datafaker.Faker;
import com.wildlifedb.repository.LocationRepository;

public class insertTaxonomy {

    public void updateLoc(BackendService backendService){
        List<Report> allreps = backendService.getAllReports();
        List<Location> alllocs = backendService.getallLocations();
        Report currReport;
        Location currLocation;
        for(int i =0;i<allreps.size();i++){
            currReport = allreps.get(i);
            currLocation = alllocs.get((int)Math.random()*alllocs.size());
            backendService.updateReportLocation(currReport.getReportId(),currLocation);
        }
    }

    public void insertReports(BackendService backendService,LocationRepository locationRepository) {
        
        for(int i =0; i< 3000; i++){
            User userid = backendService.getrandomUser();
            Random random = new Random();
            int year = random.nextInt(2024 - 2015 + 1) + 2015;
            int month = random.nextInt(12 - 1 + 1) + 1;
            int day = random.nextInt(28 - 1 + 1) + 1;
            int hour = random.nextInt(23 - 0 + 1) + 0;
            int minute = random.nextInt(59 - 0 + 1) + 0;
            int second = random.nextInt(59 - 0 + 1) + 0;
            int age = random.nextInt(30 - -1 + 1) + -1;
            int loc = random.nextInt(1617 - 0 +1) + 0;
            Location realloc = locationRepository.findByLocationId(loc).get();

            ZoneOffset zone = ZoneOffset.ofHours(0);
            OffsetDateTime time = OffsetDateTime.of(year, month, day, hour, minute, second, 0,zone);
            Report rep;
            if(Math.random()<.2){
                rep =backendService.addReport(userid, backendService.getrandomSpecies(),true,age,time,realloc);

            }else{
                rep = backendService.addReport(userid, backendService.getrandomSpecies(),false,age,time,realloc);
            }
        }
    }




    public void insertUsers(BackendService backendService) throws IOException{
        //try (Scanner scanner = new Scanner(new File(filePath))) {
        try (BufferedReader br = new BufferedReader(new FileReader("htdocs\\cs411project\\backend\\src\\main\\java\\team15\\backend\\taxonomyClassfiles\\names.txt"));) {

            String UserId = null;
            String email = null;
            Boolean verifier = false;
            String password=null;
            String[] domains = {"@gmail.com","@yahoo.com","@outlook.com","@aol.com","@icloud.com","@mail.com"};
            int rand;
            Faker faker = new Faker();

            for (String line = null; (line = br.readLine()) != null;) {
                rand = (int) Math.floor(Math.random()*6) % 5;
                password=UUID.randomUUID().toString().substring(0,9);
                line = line.trim();
                line = line.replace(' ', '.');
                UserId = (faker.superhero().prefix()+faker.name().firstName()+faker.address().buildingNumber());

                email = line+domains[rand];
                if(Math.random()<0.01){
                    verifier=true;
                }else{verifier=false;}
                backendService.addUser(UserId, email, password, verifier);

            }
            


        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        }

    }
    
    public void insert(BackendService backendService) throws IOException{
        // String filePath = "C:\\xampp\\htdocs\\cs411project\\backend\\src\\main\\java\\team15\\backend\\taxonomyClassfiles\\codexOfLife.txt"; // Replace with your file path
        // int check = 0;

        // try (Scanner scanner = new Scanner(new File(filePath))) {
        // FileReader fr = new FileReader("C:\\xampp\\htdocs\\cs411project\\backend\\src\\main\\java\\team15\\backend\\taxonomyClassfiles\\codexOfLife.txt");
        // BufferedReader br = new BufferedReader(fr);

/* 
        try (BufferedReader br = new BufferedReader(new FileReader("htdocs\\cs411project\\backend\\src\\main\\java\\team15\\backend\\taxonomyClassfiles\\codexOfLife.txt"));) {

            String currKingdom = "";
            Phylum currPhylum = null;
            TaxonomyClass currClass = null;
            TaxonomyOrder currOrder = null;
            Family currFamily = null;
            Genus currGenus = null;
            String currSpecies = "";
            // while (scanner.hasNextLine()) {
            
            for (String line = null; (line = br.readLine()) != null;) {





                // String line = scanner.nextLine();
                line = line.trim();
                boolean equivalent = false;
                if(line.charAt(0)=='='){
                    equivalent = true;
                }
                String rank;
                if(line.indexOf("[")!=-1&line.indexOf("]")!=-1){
                    rank = line.substring(line.indexOf("[")+1,line.indexOf("]"));
                }else{
                    rank = null;
                }
                String name = "error";
                System.out.println(line);
                int index = line.indexOf(' ');
                int index2 = line.indexOf(' ', line.indexOf(' ') + 1);
                if(index!=-1 & Character.isUpperCase(line.charAt(index+1)) | line.charAt(index+1)== '[' | line.charAt(index+1)== '('){
                    name = line.substring(0,index);
                    name = name.replace(',',' ');
                    name = name.replace('=',' ');
                    name = name.replace('†',' ');
                    
                    name = name.trim();
                } else if(index2!=-1 & Character.isUpperCase(line.charAt(index2+1)) | line.charAt(index2+1)== '['| line.charAt(index2+1)== '('){
                    name = line.substring(0,index2);
                    name = name.replace(',',' ');
                    name = name.replace('=',' ');
                    name = name.replace('†',' ');
                    name = name.trim();
                } else if(index2!=-1){
                    int index3 = line.indexOf(" ", line.indexOf(" ",line.indexOf(" ") + 1) + 1);
                    name = line.substring(0,index3);
                    name = name.replace(',',' ');
                    name = name.replace('=',' ');
                    name = name.replace('†',' ');
                    name = name.trim();
                }


                if(rank!=null &&(!equivalent ||  rank.equals("species"))){
                    switch (rank){
                        case "kingdom":
                        
                        currKingdom= name;
                        break;
                        case "phylum":
                        currPhylum=new Phylum(name,currKingdom);
                        backendService.addPhylum(name,currKingdom);
                        break;
                        case "class":

                        currClass=new TaxonomyClass(name,currPhylum);
                        backendService.addTaxonomyClass(name, currPhylum);
                        break;
                        
                        case "order":
                        
                        currOrder=new TaxonomyOrder(name,currClass);
                        backendService.addOrder(name, currClass);
                        break;
                        case "family":
                        
                        currFamily=new Family(name,currOrder);
                        backendService.addFamily(name, currOrder);
                        break;
                        case "genus":
                        
                        currGenus=new Genus(name,currFamily);
                        backendService.addGenus(name, currFamily);
                        break;
                        case "species":
                        
                        currSpecies=name;
                        backendService.addSpecies(name, currGenus);
                        break;
                        default:

                    }
                }

                check++;
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        }
            */
            
        // filePath = "C:\\xampp\\htdocs\\cs411project\\backend\\src\\main\\java\\team15\\backend\\taxonomyClassfiles\\NameUsage.tsv";
        // System.out.println("next Section: \n\n");
        //try (Scanner scanner = new Scanner(new File(filePath))) {
        try (BufferedReader br = new BufferedReader(new FileReader("htdocs\\cs411project\\backend\\src\\main\\java\\team15\\backend\\taxonomyClassfiles\\NameUsage.tsv"));) {

        //    check = 0;
            String Speciesname = null;
            String genericname = null;
            String rank = null;
            String status = null;
            // while (scanner.hasNextLine()) {
            
            for (String line = null; (line = br.readLine()) != null;) {
            

            //while (scanner.hasNextLine() ) {
                // System.out.println("check: "+check);;
                //String row = scanner.nextLine();

                Speciesname = null;
                genericname = null;
                rank = null;
                status = null;

                String[] lineItems = line.split("\t");
                if(lineItems.length>7){
                    Speciesname = lineItems[7];
                }
                if(lineItems.length>9){
                    rank = lineItems[9];
                }
                if(lineItems.length>13){
                    genericname = lineItems[13];
                }
                if(lineItems.length>45){
                    status = lineItems[45];
                }
                if(rank!=null && rank.equals("species")){
                    if(status != null && status.equals("true")){
                        backendService.updateSpecies(Speciesname, genericname, true);
                    }else {
                        backendService.updateSpecies(Speciesname, genericname, false);
                    }
                }


                // System.out.println("item: "+7+" content: "+lineItems[7]);
                // System.out.println("item: "+9+" content: "+lineItems[9]);


                
                // if(lineItems.length>=45){
                //     System.out.println("item: "+45+" content: "+lineItems[45]);
                // } else{
                //     System.out.println("item: "+45+" content: "+null);
                // }
                
                // System.out.println("\n");
                
                // check++;
            }
            


        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + e.getMessage());
        }


    }

    

    
}
