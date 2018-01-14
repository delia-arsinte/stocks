# Getting started

1. mvn spring-boot:run

## Configuration

## Transaction Management
JPA Repositories have the `@Transactional` annotation on the save method by default, hence a transaction is created when a stock
is updated or deleted:

https://github.com/spring-projects/spring-data-jpa/blob/master/src/main/java/org/springframework/data/jpa/repository/support/SimpleJpaRepository.java
