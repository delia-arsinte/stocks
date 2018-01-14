# Getting started

1. Run the application from command line: `mvn spring-boot:run`
2. All endpoints are accessible at http://localhost:8080/api/stocks
3. Swagger documentation is available at http://localhost:8080/swagger-ui.html

## Configuration

## Transaction Management
JPA Repositories have the `@Transactional` annotation on the save method by default, hence a transaction is created when a stock
is updated or deleted:

https://github.com/spring-projects/spring-data-jpa/blob/master/src/main/java/org/springframework/data/jpa/repository/support/SimpleJpaRepository.java
