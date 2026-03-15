## Project Summary

Wildlife Reporter is a web application that allows users to input their findings of wildlife ranging from any animals that are from mountains to sea. The application uses numerous databases from USGS and National Geographic to fetch necessary data about animals such as the name of the species to taxonomy. Through this application, we aim to create a more diverse and rich database for any organization and students who are interested in habitats and characteristics of animals

## Credits

This project is an extended and improved version of the original course project from CS411 at the University of Illinois at Urbana-Champaign. I am continuing development on my own to refactor the codebase, improve security, and add new features based on the original team project.

This project was originally developed by Team 015 in CS 411 (Summer 2025):

- Ryan Choi (rc49@illinois.edu)
- Nathan Colunga (colunag4@illinois.edu)
- Yunqi Han (yunqih2@illinois.edu)

## Tech Stack

- **Backend:** Java 17, Spring Boot 3.5.3
- **Database:** MySQL 8.0, Spring Data JPA
- **Frontend:** Thymeleaf, HTML/CSS/JavaScript
- **Geospatial:** GeoTools, JTS (Census shapefile processing)
- **Build:** Maven

## Features

- User registration and login
- Create, edit, delete, and verify wildlife sighting reports
- Full taxonomy hierarchy (Phylum → Class → Order → Family → Genus → Species)
- Species query and search
- Geospatial location data with US city boundaries
- Data seeding with 3000+ randomized reports
