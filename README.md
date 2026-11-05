# HBV501G Team 3 

## Team members:
- Rúnar Þór Árnason: rthor997
- Dagur Ingi Viðar: Dagurvidar
- Hildur Agla Ottadóttir: hilduAgl
- Óli Þorbjörn Guðbjartsson: oligood

# Project description 
This project is a backend REST API for a climbing application developed as part of HBV501G Software project 1.

The application allows users to create accounts, log climbs and workouts, view climbing history and route information, and manage user-specific climbing data. Administrative functionality is also provided for managing users, climbs, and routes. 

The backend is implemented using Spring Boot and uses PostgreSQL for persistent data storage. 


## Web Service
The deployed backend is available at 

https://hbv501g-3.onrender.com/

This is a REST API rather than a graphical website, so the root URL may not display a user interface. API functionality is accessed through the available endpoints.

## Environment variables
Some environment variables need to be set before the application can start  
- Database connection
- JWT configuration
variable names and expected values are in the `application.properties`

Connection settings are stored as environment variables in the terminal, if you want to use our configuration contact us through email.

## Authentication 
The API uses JSON Web Tokens (JWT) for authentication.

A user first logs in through the login endpoint and receives a JWT token. Protected endpoints require the token to be included in the request. Some endpoint additionally require the authticated user to have the ADMIN role. 

## Database
The deployed application uses a PostgreSQL database hosted on Neon. 

