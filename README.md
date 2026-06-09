## Project Summary

Wildlife Reporter is a web application that allows users to input their findings of wildlife ranging from any animals that are from mountains to sea. The application uses numerous databases from USGS and National Geographic to fetch necessary data about animals such as the name of the species to taxonomy. Through this application, we aim to create a more diverse and rich database for any organization and students who are interested in habitats and characteristics of animals

## Credits

This project is an extended and improved version of the original course project from CS411 at the University of Illinois at Urbana-Champaign. I am continuing development on my own to refactor the codebase, improve security, and add new features based on the original team project.

This project was originally developed by Team 015 in CS 411 (Summer 2025):

- Ryan Choi (rc49@illinois.edu)
- Nathan Colunga (colunag4@illinois.edu)
- Yunqi Han (yunqih2@illinois.edu)

## Tech Stack

- **Backend:** Java 17, Spring Boot 3.4.0
- **Database:** MySQL 8.0, Spring Data JPA
- **Frontend:** Thymeleaf, HTML/CSS/JavaScript
- **Geospatial:** GeoTools, JTS (Census shapefile processing)
- **Build:** Maven

## Features

- User registration and login
- BCrypt password hashing and stateless JWT authentication
- Create, edit, delete, and verify wildlife sighting reports
- Full taxonomy hierarchy (Phylum → Class → Order → Family → Genus → Species)
- Species query and search
- Geospatial location data with US city boundaries
- Data seeding with 3000+ randomized reports

## JWT Authentication

The REST authentication endpoints are:

- `POST /auth/register`
- `POST /auth/login`

Both endpoints return a JWT in `data.token`. Send it to protected Observation
write endpoints with the following header:

```text
Authorization: Bearer <token>
```

Observation queries remain public. Creating, updating, and deleting observations
requires authentication:

- `POST /observations`
- `PUT /observations/{id}`
- `DELETE /observations/{id}`

Configure a signing secret of at least 32 bytes before starting the application.
The value must be kept outside source control in production:

```powershell
$env:JWT_SECRET = "replace-with-a-long-random-production-secret"
$env:JWT_EXPIRATION_SECONDS = "3600"
.\mvnw.cmd spring-boot:run
```

The default secret in `application.properties` is intended only for local
development. Existing legacy plaintext passwords are upgraded to BCrypt after a
successful login.

## Swagger / OpenAPI

Start the backend from PowerShell:

```powershell
cd "C:\Users\xiudo\Documents\wildlife reporter\backend"
$env:JWT_SECRET = "replace-with-a-long-random-development-secret"
.\mvnw.cmd spring-boot:run
```

After the application starts, open:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Registration, login, and public Observation queries can be called directly from
Swagger UI. For protected Observation write endpoints, log in first, copy
`data.token`, select **Authorize**, and enter the token. Swagger UI adds the
`Bearer` prefix automatically.
