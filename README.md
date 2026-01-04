# Reactive R2DBC Experiment

This is a simple experiment to test Spring WebFlux [Functional Programming Model](https://docs.spring.io/spring-framework/reference/web/webflux-functional.html) interaction with PostgreSQL using [R2DBC](https://r2dbc.io/).

> Disclaimer: R2DBC does **not** implement the [JDBC](http://download.oracle.com/otn-pub/jcp/jdbc-4_1-mrel-spec/jdbc4.1-fr-spec.pdf?AuthParam=1529679008_7acd6035892acd847bba6ff8dd5242d1) specification - it's a fully reactive alternative.

## Technologies

* Spring Boot 4.0.1
* Spring WebFlux
* Spring Data R2DBC
* Java 21
* Gradle 9.1
* PostgreSQL R2DBC Driver 1.0.7
* Testcontainers 1.21.3
* Lombok 1.18.42

## Prerequisites

* An account with [Space Developer role](https://docs.cloudfoundry.org/concepts/roles.html#roles) access on a Cloud Foundry foundation, e.g., [Pivotal Web Services](https://run.pivotal.io)
* [CF CLI](https://github.com/cloudfoundry/cli#downloads) 6.37.0 or better if you want to push the application to a Cloud Foundry (CF) instance
* [httpie](https://httpie.org/#installation) 0.9.9 or better to simplify interaction with API endpoints
* Java [JDK](https://adoptium.net/) 21 or better to compile and run the code
* [Gradle](https://gradle.org/releases/) 9.1 or better to build and package source code (wrapper included)
* Docker for [Mac](https://store.docker.com/editions/community/docker-ce-desktop-mac) or [Windows](https://store.docker.com/editions/community/docker-ce-desktop-windows) for spinning up a local instance of Postgres and Adminer (a database administration interface)


## Clone

```bash
git clone https://github.com/pacphi/reactive-jdbc-demo.git
```

## How to build

```bash
cd reactive-jdbc-demo
gradle build
```

## How to run locally 

1. Prepare database

    Open a Terminal session, then type

    ```bash
    docker-compose up -d
    ```

2. Login to [Adminer](https://www.adminer.org) interface 

    Open a browser and visit `http://localhost:9090`

    Credentials are:

    * System => `PostgreSQL`
    * Server => `db`
    * Username => `admin`
    * Password => `passw0rd`
    * Database => `people`

    Click the `Login` button

3. Start the application

    Start a new Terminal session and type

    ```bash
    gradle bootRun
    ```

    > Spring Data R2DBC will automatically initialize the schema from schema.sql if present.

4. Let's create some data using the API

    ```bash
    http POST localhost:8080/person firstName=Dweezil lastName=Zappa age=48

    HTTP/1.1 202 Accepted
    Content-Type: application/json
    ```

5. Verify that we can find the person we added

    ```bash
    http localhost:8080/person

    HTTP/1.1 200 OK
    Content-Type: application/json
    transfer-encoding: chunked

    [
        {
            "age": 48,
            "firstName": "Dweezil",
            "id": "582279d1-9bd1-4e49-946c-ac720de0e04f",
            "lastName": "Zappa"
        }
    ]
    ```

6. Let's ask for a person by id

    ```bash
    http localhost:8080/person/582279d1-9bd1-4e49-946c-ac720de0e04f

    HTTP/1.1 200 OK
    Content-Length: 95
    Content-Type: application/json

    {
        "age": 48,
        "firstName": "Dweezil",
        "id": "582279d1-9bd1-4e49-946c-ac720de0e04f",
        "lastName": "Zappa"
    }
    ```

## How to shutdown locally

1. Stop the application

    Visit the Terminal session where you started application and press `Ctrl+c`

2. Shutdown Postgres and Adminer interface

    Visit the Terminal session where you invoked `docker-compose-up -d` and type

    ```bash
    docker-compose down
    ```

    Note: the data volume is persistent!  If you want to destroy all unused volumes and reclaim some additional space, type

    ```bash
    docker volume prune
    ```

## How to run on Cloud Foundry

1. Authenticate to a foundation using the API endpoint. 
    > E.g., login to [Pivotal Web Services](https://run.pivotal.io)

    ```bash
    cf login -a https:// api.run.pivotal.io
    ```

2. Push the app, but don't start it

    ```bash
    cf push reactive-jdbc-demo --random-route --no-start -p ./build/libs/reactive-jdbc-demo-0.0.1-SNAPSHOT.jar -m 1G -b https://github.com/cloudfoundry/java-buildpack.git
    ```

3. Let's fire fire up a Postgres instance

    > We're going to use [ElephantSQL](https://www.elephantsql.com)

    ```bash
    cf cs elephantsql panda {service name}
    ```

    > Note: this is going to cost you $19/month to keep alive
    > Replace {service name} above with your desired service name

4. Next we'll bind the service to the application

    ```bash
    cf bs reactive-jdbc-demo {service name}
    ```

    > Make sure {service name} above matches what you defined in Step 3

5. Let's verify that `VCAP_SERVICES` was properly injected

    ```bash
    cf env reactive-jdbc-demo
 
    Getting env variables for app reactive-jdbc-demo in org scooby-doo / space dev as dweezil@zappa.com...
    OK

    System-Provided:
    {
    "VCAP_SERVICES": {
    "elephantsql": [
    {
        "binding_name": null,
        "credentials": {
        "max_conns": "20",
        "uri": "postgres://blxrphig:XXX0bLLyWpiUqKozCRhzygyhnpOMlMC@elmer-01.db.elephantsql.com:5432/banzlhig"
        },
        ...
    ```

    > We're interested in `vcap_services.elephantsql.uri`
    > The URI consists of {vendor}://{username}:{password}@{server}:5432/{database}

6. Configure R2DBC connection using Cloud Foundry service binding

    Cloud Foundry will automatically configure the R2DBC connection from the bound PostgreSQL service.

7. Now let's startup the application

    ```bash
    cf start reactive-jdbc-demo
    ```

    > Spring Data R2DBC will automatically initialize the schema.

8. (Optional) Launch Adminer to verify database setup

    ```bash
    docker-compose up -d
    ```

    Open a browser and visit `http://localhost:9090`

    Credentials are:

    * System => `PostgreSQL`
    * Server => `{server}`
    * Username => `{username}`
    * Password => `{password}`
    * Database => `{database}`

    Replace all bracketed values above with what you learned from Step 5

    Click the `Login` button

9. Follow steps 4-6 above in `How to run locally` to interact with API

    But replace occurrences of `localhost:8080` with URL to application hosted on Cloud Foundry

*Congratulations! You've just pushed and interacted with a 100% reactive and cloud native app.*

## How to spin down workloads on Cloud Foundry

1. Stop the application

    ```bash
    cf stop reactive-jdbc-demo
    ```

2. Unbind the database instance

    ```bash
    cf us reactive-jdbc-demo {service name}
    ```
    > `{service name}` above should match value in `How to run on Cloud Foundry` steps 3 and 4

3. Delete the database instance

    ```bash
    cf ds {service name}
    ```
    > `{service name}` above should match value in `How to run on Cloud Foundry` steps 3 and 4

4. Delete the application

    ```bash
    cf delete reactive-jdbc-demo
    ```

## Key Features

### Spring Data R2DBC Benefits

* **Fully Reactive**: Non-blocking database operations from top to bottom
* **Repository Pattern**: Clean, type-safe data access with ReactiveCrudRepository
* **Spring Boot Integration**: Automatic configuration and connection pool management
* **Testcontainers Support**: Easy integration testing with actual PostgreSQL containers
* **Cloud-Native**: Built-in support for Cloud Foundry service bindings

### Migration Notes

This project has been modernized from the deprecated `reactive-pg-client` to **Spring Data R2DBC**, which provides:

* Standard Spring Data repository interfaces
* Automatic connection pool management
* Better error handling and transaction support
* Active community and long-term support
* Built-in Spring Boot autoconfiguration

## Learn More

* [Spring Data R2DBC Reference](https://docs.spring.io/spring-data/r2dbc/reference/)
* [R2DBC Specification](https://r2dbc.io/)
* [PostgreSQL R2DBC Driver](https://github.com/pgjdbc/r2dbc-postgresql)
* [Spring WebFlux Functional Endpoints](https://docs.spring.io/spring-framework/reference/web/webflux-functional.html)
