# Reactive JDBC Experiment  

This is a simple experiment to test Spring 5's Webflux Module's [Functional Programming Model](https://docs.spring.io/spring/docs/5.0.0.BUILD-SNAPSHOT/spring-framework-reference/html/web-reactive.html#_functional_programming_model) interaction with the Reactiverse [reactive-pg-client](https://reactiverse.io/reactive-pg-client/guide/java/index.html).  

## Prerequisites

* An account with [Space Developer role](https://docs.cloudfoundry.org/concepts/roles.html#roles) access on a Cloud Foundry foundation, e.g., [Pivotal Web Services](https://run.pivotal.io)
* [CF CLI](https://github.com/cloudfoundry/cli#downloads) 6.37.0 or better if you want to push the application to a Cloud Foundry (CF) instance
* [httpie](https://httpie.org/#installation) 0.9.9 or better to simplify interaction with API endpoints
* Java [JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) 1.8u172 or better
* [Gradle](https://gradle.org/releases/) 4.8 or better
* Docker for [Mac](https://store.docker.com/editions/community/docker-ce-desktop-mac) or [Windows](https://store.docker.com/editions/community/docker-ce-desktop-windows)


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

* System => PostgreSQL
* Server => db
* Username => admin
* Password => passw0rd
* Database => people

Click the `Login` button

3. Click on the `SQL command` link 

> Link is in the upper left hand-corner of the interface

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

// TODO

## Resources

// TODO