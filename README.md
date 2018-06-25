# Reactive ~~JDBC~~ Experiment  

This is a simple experiment to test Spring 5's Webflux Module's [Functional Programming Model](https://docs.spring.io/spring/docs/5.0.0.BUILD-SNAPSHOT/spring-framework-reference/html/web-reactive.html#_functional_programming_model) interaction with the Reactiverse [reactive-pg-client](https://reactiverse.io/reactive-pg-client/guide/java/index.html). 

> Disclaimer: the `reactive-pg-client` does **not** implement the [JDBC](http://download.oracle.com/otn-pub/jcp/jdbc-4_1-mrel-spec/jdbc4.1-fr-spec.pdf?AuthParam=1529679008_7acd6035892acd847bba6ff8dd5242d1) specification.

## Prerequisites

* An account with [Space Developer role](https://docs.cloudfoundry.org/concepts/roles.html#roles) access on a Cloud Foundry foundation, e.g., [Pivotal Web Services](https://run.pivotal.io)
* [CF CLI](https://github.com/cloudfoundry/cli#downloads) 6.37.0 or better if you want to push the application to a Cloud Foundry (CF) instance
* [httpie](https://httpie.org/#installation) 0.9.9 or better to simplify interaction with API endpoints
* Java [JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) 1.8u172 or better to compile and run the code
* [Gradle](https://gradle.org/releases/) 4.8 or better to build and package source code
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

3. Click on the `SQL command` link 

    Link is in the upper left hand-corner of the interface

4. Cut-and-paste the contents of [people.ddl](people.ddl) into the text area, then click the `Execute` button

5. Start the application

    Start a new Terminal session and type

    ```bash
    gradle bootRun
    ```

6. Let's create some data using the API

    ```bash
    http POST localhost:8080/person firstName=Dweezil lastName=Zappa age=48

    HTTP/1.1 202 Accepted
    content-length: 0
    ```

7. Verify that we can find the person we added

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

8. Let's ask for a person by id

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

6. We'll set an environment variable

    ```bash
    cf set-env reactive-jdbc-demo PG_LOOKUP_KEY {service name}
    ```
    > `{service name}` above should match value in steps 3 and 4

7. Now let's startup the application

    ```bash
    cf start reactive-jdbc-demo
    ```

8. Launch Adminer to administer the database

    The `people` table doesn't exist yet, so we need to create it

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

9. Click on the `SQL command` link 

10. Cut-and-paste the contents of [people.ddl](people.ddl) into the text area, then click the `Execute` button

11. Follow steps 6-8 above in `How to run locally` to interact with API

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
    
## What to look forward to?

* Asynchronous Database Access ([ADBA](https://blogs.oracle.com/java/jdbc-next:-a-new-asynchronous-api-for-connecting-to-a-database))
* ADBA over JDBC ([AoJ](https://github.com/oracle/oracle-db-examples/blob/master/java/AoJ/README.md))

Oracle continues to work on ADBA while having released AoJ under an Apache license to get community feedback. 

Maybe we will see something concrete in JDK 11? 

## What else is there to play with?

* [rxjava2-jdbc](https://github.com/davidmoten/rxjava2-jdbc)
* [Vert.x JDBC Client](https://vertx.io/docs/vertx-jdbc-client/java/)
* Reactive Relational Database Connectivity Client ([R2DBC](https://github.com/r2dbc/r2dbc-client))
