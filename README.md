# Sales Notebook - Project 1: Client Registration

Extension project (Systems Analysis and Development course). First stage of a management system
for small sellers: a fully functional client registration module, serving as the foundation for the
next projects (catalog/sales, credit tracking, reports).

## Technologies

- Java 17
- Spring Boot 3.3 (Web, Data JPA, Validation, Thymeleaf)
- File-based H2 database (nothing to install to run locally)
- Plain HTML + CSS (no front-end framework — everything rendered by Spring via Thymeleaf)

## How to run

Prerequisites: JDK 17 or higher and Maven (or use your IDE's Maven Wrapper). An internet connection is
required on the first run, so Maven can download the dependencies.

```bash
mvn spring-boot:run
```

Or import the project as a "Maven Project" in IntelliJ / Eclipse / VS Code and run the
`SalesSystemApplication` class.

Then just open: **http://localhost:8080**

The database is created automatically in the `data/` folder (`database.mv.db` file) on the first run —
data persists between runs. To inspect the database from the browser, go to
http://localhost:8080/h2-console and use the JDBC URL `jdbc:h2:file:./data/database`, user `sa`, blank password.

## What already works

- Client listing, with search by name or CPF
- Registering a new client
- Editing an existing client
- Deleting a client (with confirmation)
- Form validation: required fields, valid email, Brazilian phone number format
- **Real CPF validation**, with check-digit calculation (not just checking for 11 digits), and
  duplicate CPF blocking on create/edit
- Success/error messages after each action

## Project structure

```
src/main/java/com/salessystem/
  entity/        -> Client JPA entity (persistence model only, no validation)
  dto/           -> ClientRequestDTO (form input) and ClientResponseDTO (display output)
  mapper/        -> ClientMapper (converts between entities and DTOs)
  repository/    -> ClientRepository (Spring Data JPA)
  service/       -> ClientService (business rules: CPF duplication, etc.), works only with DTOs
  controller/    -> MVC controllers (receive requests, return Thymeleaf pages), never see entities
  exception/     -> custom domain exceptions
  validation/    -> custom @CPF annotation + validator

src/main/resources/
  templates/     -> HTML pages (Thymeleaf)
  static/css/    -> stylesheet
  application.properties -> database, port, etc. configuration
```

The architecture follows a layered pattern (Controller → Service → Mapper/Repository) with a
**DTO boundary**: controllers and templates only ever see `ClientRequestDTO` / `ClientResponseDTO`,
never the `Client` entity directly. This keeps persistence concerns (JPA annotations) separate from
input validation and from what gets rendered on screen, and leaves room to evolve each side
independently as the next modules are added.

## Why Thymeleaf instead of a separate front-end (React, etc.)?

Since the project doesn't require prior front-end knowledge, Thymeleaf allows building screens with
plain HTML processed by Spring Boot itself — no need to learn a separate JavaScript ecosystem. All the
learning effort stays focused on Java/Spring, which is the focus of the course.

## Switching from H2 to PostgreSQL or MySQL (for deployment)

1. In `pom.xml`, replace the H2 dependency with:
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```
2. In `application.properties`, replace the `spring.datasource` lines with:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/sales_notebook
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=YOUR_USER
spring.datasource.password=YOUR_PASSWORD
```
No other code changes are necessary — that's the advantage of using Spring Data JPA.

## Next steps (Project 2)

- `Product` and `Sale` entities (related to `Client`)
- Automatic stock deduction when registering a sale
- New sale screen with line items
