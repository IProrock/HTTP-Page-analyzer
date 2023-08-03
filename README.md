
### Hexlet tests and linter status:

[![Actions Status](https://github.com/IProrock/java-project-72/workflows/hexlet-check/badge.svg)](https://github.com/IProrock/java-project-72/actions)
[![build action](https://github.com/IProrock/java-project-72/actions/workflows/my-check.yml/badge.svg)](https://github.com/IProrock/java-project-72/actions/workflows/my-check.yml)

### Codeclimate status:

[![Maintainability](https://api.codeclimate.com/v1/badges/c14416be89eb355ebd0a/maintainability)](https://codeclimate.com/github/IProrock/java-project-72/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/c14416be89eb355ebd0a/test_coverage)](https://codeclimate.com/github/IProrock/java-project-72/test_coverage)

Deployed at: https://project-four.onrender.com/

---

# HTTP Page analyzer
This app can track url's availability.\
Add URL to the list using form on the main page and easily check url's status with one click.\
APP will send HTTP request to specified URL and will show following response details:/
- Response status code
- Page title
- HTML H1 block (if available)
- HTML meta description block (if available)

All results will be saved to DB so checks history will be available.

## Technologies used:
**Programming language:** Java 20\
**Build automation tool:** Gradle 8.2.1\
**Web framework:** Javalin\
**HTML Template engine:** Thymeleaf\
**CSS framework:** Bootstrap 5\
**ORM:** eBean\
**Database:** PostgreSQL (for production) / H2 (for tests only)\
**Test technologies:** JUnit framework / Unirest library / MockWebServer / slf4j library\
**CI:** GithubActions / JaCoCo / Codeclimate\

## Setup

```sh
Local machine:
Run application: ./gradlew run
Run test: ./gradlew test
```

## Environment preparation

```sh
APP_ENV: development | production
in case of development - local H2 database will be used
in case of production set following variables:
JDBC_DATABASE_USERNAME: <PostgreSQL username>
JDBC_DATABASE_PASSWORD: <PostgreSQL password>
PGHOST: <PostgreSQL host name>
PGPORT: <PostgreSQL port>
PGDATABASE: <PostgreSQL database name>
```