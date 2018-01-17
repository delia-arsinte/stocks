# Getting started

1. Run the application from command line: `mvn spring-boot:run`
2. All endpoints are accessible at http://localhost:8080/api/stocks
3. Swagger documentation is available at http://localhost:8080/swagger-ui.html
4. Run performance test from command line from performance project: 'mvn integration-test'

## Configuration

Currently all the configuration is loaded from application.yml. Since there are no deployment scripts
also the environment specific configuration is in application.yml. We can have environment specific configuration
in environment specific yml file and specify it via system properties with -Dspring.config.location=<path to config location>
or use spring-profiles. Then the values from environment specific yml will overwrite the values from default application.yml

Logback configuration is also environment specific and can be specified with -Dlogging.config=<path to logback configuration> 

## Data model

###
I added constraints for the name to be not empty and max 255 characters. I did not add any constraints
for the name to be unique because one company could have muliple types of stocks. 

I did not expose the stock ID in the response of the APIs because due to security reasons. The IDs
are generated sequentially and an authenticated user could guess the number of records in the database.

I would change from primary keys generated sequentially to GUIDs. A good article on primary keys
versus GUIDS can be found here:

https://blog.codinghorror.com/primary-keys-ids-versus-guids/


### Transaction management
JPA Repositories have the `@Transactional` annotation on the save method by default, hence a transaction is created when a stock
is updated or deleted:

https://github.com/spring-projects/spring-data-jpa/blob/master/src/main/java/org/springframework/data/jpa/repository/support/SimpleJpaRepository.java

## Health and Metrics
The application uses spring boot actuator plugin. By default actuator exposes the health indicator via /health and metrics via /metrics. 
I would use Prometheus and spring boot does have support for it as a monitoring solution.  Below are some nice features of Prometheus:

* ability to set custom alerts
* no aggregation: all data is available unlike with Graphite
* ability to write powerful queries and create customized dashboards

## Security
I implemented a basic role based security model with in memory basic authentication. There is a user with VIEW permissions
than can call only the GET endpoints and an user with admin permissions that can call the POST and PUT endpoints.
I disabled CSRF support in order to simply the integration and performance tests. 

This is definitely not production-ready and we should have a proper authentication mechanism in place. I would also add CSRF protection back,
adjust the integration and performance tests to read the CSRF token after login and create security integration tests for all possible combinations
of user permissions (ALL, VIEW, UPDATE) and URLs and assert for the expected response. 

If we want to expose these endpoints to both a WEB UI and a machine, then we can use the current role based security model for WEB-UI
and create another endpoint that exposes the same URLs with JWT certificate based security. 
  
# Possible Extensions

## Pagination for GET /api/stocks
The current API does not implement any pagination: the findAll endpoint returns all records from the database. If the database
has millions of stocks for one company it can cause performance issues/out of memory on the backend side and the frontend will freeze
because it cannot handle so many records. A solution to mitigate this issue is implementing pagination, spring provides this functionality
by default by returning a Pageable instead of a list or a stream:

https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/domain/Pageable.html

## Scalability

The application is stateless, depending on the requirements, we could deploy multiple instances on different nodes, assuming that we have 
a proper authentication mechanism in place that offers single sign on.   