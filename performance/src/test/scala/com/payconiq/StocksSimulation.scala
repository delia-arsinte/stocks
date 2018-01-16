package com.payconiq

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder.toActionBuilder

class StocksSimulation extends Simulation {

  val httpProtocolBuilder = http.baseURL("http://localhost:8080")

  val scenarioBuilder = scenario("Scenario1")
    .exec(
      http("stocks find all")
        .get("/api/stocks"))
    .pause(2)
    .exec(
      http("stocks get one")
        .get("/api/stocks/1")
    )

  setUp(
    scenarioBuilder.inject(atOnceUsers(10),
      nothingFor(10),
      rampUsers(10) over (5),
      constantUsersPerSec(10) during (50))
  ).protocols(httpProtocolBuilder)

}