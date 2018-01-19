package com.payconiq

import scala.concurrent.duration._


import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder.toActionBuilder

class StocksSimulation extends Simulation {

  val httpProtocolBuilder = http.baseURL("http://localhost:8080")

  val scenarioBuilder = scenario("Scenario1")
    .exec(
      http("login")
        .post("/login")
        .formParam("username", "user")
        .formParam("password", "password"))
    .pause(3 seconds)
    .exec(
      http("stocks find all")
        .get("/api/stocks"))
    .pause(1 seconds, 2 seconds)
    .exec(
      http("stocks get one")
        .get("/api/stocks/1")
    )

  setUp(
    scenarioBuilder.inject(atOnceUsers(10),
      nothingFor(10),
      rampUsers(10) over (5 seconds),
      constantUsersPerSec(10) during (1 minute))
  ).protocols(httpProtocolBuilder)

}