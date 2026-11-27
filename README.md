# Employee Management API

A Spring Boot REST API for managing employee records in PostgreSQL. Employees can be searched, filtered, sorted, and paginated. A `USER` can read records; an `ADMIN` can also create, replace, patch, and delete them. Authentication uses a database-backed login and short-lived JWTs.

**Stack:** Java 21 · Spring Boot 3.5.16 · Spring Security · Spring Data JPA · PostgreSQL · JJWT 0.12.6 · Springdoc OpenAPI 2.10.0 · Maven Wrapper

## Run locally

**Prerequisites:** JDK 21 and a running PostgreSQL server. Run Maven commands from the directory containing `pom.xml`. A global Maven installation is not required. The API has been run on macOS with PostgreSQL 18 on port `5433`; the project's default database port is `5432`.

### 1. Prepare the database

You need a PostgreSQL login named `employee_app` and a development database named `employee_management_db` owned by that login. **If you already have the restored development database, skip this step.** For a fresh PostgreSQL installation, run the following once as a PostgreSQL administrator, using the port of your own server:

```sh
createuser -h localhost -p 5433 --pwprompt employee_app
createdb -h localhost -p 5433 -O employee_app employee_management_db
```

These are PostgreSQL command-line tools, not Maven commands. With Postgres.app installed under your Mac account, you can add its tools to the current Terminal session with `export PATH="$HOME/Applications/Postgres.app/Contents/Versions/18/bin:$PATH"`. If your PostgreSQL administrator's name differs from your operating-system username, add `-U <admin-role>` to the two commands above.

### 2. Configure local credentials

In **macOS Terminal (zsh)**, enter the database password at the hidden prompt. The signing key below is generated locally, not copied from a public example:

```zsh
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
export DB_USER=employee_app
read -rs 'DB_PASSWORD?PostgreSQL password for employee_app: '; echo
export DB_PASSWORD
export JWT_SECRET="$(openssl rand -base64 48)"
```

Keep `JWT_SECRET` private. This example generates a new key for this shell; changing the signing key invalidates previously issued tokens. Use securely stored environment variables if you need the same key after restarting your shell.

**First login on a fresh database:** the app creates `rahul` (`USER`) and/or `admin` (`ADMIN`) only if the corresponding seed password is supplied and the account does not already exist. For the first launch, set either or both in the same shell:

```zsh
read -rs 'APP_SEED_USER_PASSWORD?Choose a password for rahul: '; echo
export APP_SEED_USER_PASSWORD
read -rs 'APP_SEED_ADMIN_PASSWORD?Choose a different password for admin: '; echo
export APP_SEED_ADMIN_PASSWORD
```

Skip the seed variables for an existing database. They neither reset existing passwords nor grant a role to someone logging in. New passwords are stored as BCrypt hashes. Do not commit real passwords, signing keys, JWTs, backups, or unreviewed Postman exports.

### 3. Start the API

For an **existing database with the application schema**, start in validation mode. The URL and schema mode below apply only to this command:

```sh
SPRING_PROFILES_ACTIVE=default \
SPRING_DATASOURCE_URL='jdbc:postgresql://127.0.0.1:5433/employee_management_db' \
SPRING_JPA_HIBERNATE_DDL_AUTO=validate \
./mvnw spring-boot:run
```

For an **empty development database**, use `SPRING_JPA_HIBERNATE_DDL_AUTO=update` for the first startup so Hibernate can create the tables. Use `validate` on subsequent runs to check the schema without changing it. Replace `5433` with `5432` if appropriate. On Windows, set the equivalent environment variables and use `mvnw.cmd spring-boot:run`.

Once Tomcat starts on port `8080`, open [Swagger UI](http://localhost:8080/swagger-ui/index.html) or the [OpenAPI JSON](http://localhost:8080/v3/api-docs). Leave the application running while testing. A successful docs response verifies HTTP availability, not authenticated access to employee data.

## Try a request in Postman

1. Send **`POST http://localhost:8080/auth/login`** with **No Auth** and **Body → raw → JSON**. Replace the placeholder locally:

   ```json
   { "username": "rahul", "password": "<your application password>" }
   ```

2. A successful response has `accessToken`, `tokenType`, and `expiresInSeconds`. Tokens expire after **15 minutes** by default. Create a **GET** request to `http://localhost:8080/employees/me`. Select **Authorization → Bearer Token** and paste only the `accessToken` value. With `rahul`'s token, the expected response is `200 OK` and the plain-text body `rahul`.
3. With the same token, try `GET /employees?page=0&size=10&department=QA&sort=salary,desc`. The JSON contains `content`, `pageNumber`, `pageSize`, `totalElements`, `totalPages`, `first`, and `last`. Optional filters are `id`, `name`, `department`, `email`, `minSalary`, `maxSalary`, and `keyword`. Repeat `sort` for multiple sort criteria.

## Endpoint reference

Paths below are relative to `http://localhost:8080`.

| Method | Path | Access | Success |
| --- | --- | --- | --- |
| POST | `/auth/login` | No token required | `200` with JWT |
| GET | `/employees/me` | USER or ADMIN | `200` with current username |
| GET | `/employees` | USER or ADMIN | `200` with employee page |
| GET | `/employees/{id}` | USER or ADMIN | `200` with employee |
| POST | `/employees` | ADMIN | `201` with employee and `Location` header |
| PUT | `/employees/{id}` | ADMIN | `200` with fully updated employee |
| PATCH | `/employees/{id}` | ADMIN | `200`; omitted fields preserved |
| DELETE | `/employees/{id}` | ADMIN | `204`, no response body |

For `POST` and `PUT`, supply `name`, `department`, `email`, and `salary`. For example:

```json
{ "name": "Example Employee", "department": "QA", "email": "employee@example.com", "salary": 45000 }
```

**Security verification:** A live Postman check returned `401` for a request without a token, `403` when a USER attempted a create, and `201` when an ADMIN created a temporary record. A USER could read that record (`200`); an ADMIN deleted it (`204`), after which the same read returned `404`. This distinguishes a missing identity from a valid identity without permission.

## Run the tests safely

**Never point tests at the development database.** The `repository-test` profile uses Hibernate `create-drop`: the test schema is created and dropped. Use a dedicated, disposable `employee_management_test_db`, owned by `employee_app`. Create it once, if it does not already exist:

```sh
createdb -h localhost -p 5433 -O employee_app employee_management_test_db
```

Before running the full suite, check [`application-repository-test.properties`](src/test/resources/application-repository-test.properties). It points to `localhost:5432/employee_management_test_db` in the repository. **If using PostgreSQL on `5433`, change just the test JDBC port in that file to `5433` locally.** The repository test explicitly loads this file, so don't rely on changing only the development URL. Use the `DB_USER`, `DB_PASSWORD`, and `JWT_SECRET` configured above. Run this in a separate Terminal if the API is running; environment variables must also be set in that Terminal.

```sh
(
  unset SPRING_DATASOURCE_URL SPRING_JPA_HIBERNATE_DDL_AUTO APP_SEED_USER_PASSWORD APP_SEED_ADMIN_PASSWORD
  SPRING_PROFILES_ACTIVE=repository-test ./mvnw clean test
)
```

For a faster **database-independent** check:

```sh
./mvnw -Dtest=SecurityDataInitializerTest,EmployeeControllerTest,EmployeeServiceTest,GlobalExceptionHandlerTest test
```

**Verification record:** The full suite passed **144 tests** before the seed-password change. After that change, **95 database-independent tests** passed, including four new initializer cases. These counts overlap; the latter is not a fresh full-suite result.

## Code layout

- [`controller`](src/main/java/com/chanchal/employeemanagement/controller) handles HTTP requests; [`dto`](src/main/java/com/chanchal/employeemanagement/dto) and [`mapper`](src/main/java/com/chanchal/employeemanagement/mapper) separate the API shape from persistence.
- [`service`](src/main/java/com/chanchal/employeemanagement/service) implements employee operations and method-level authorization. [`repository`](src/main/java/com/chanchal/employeemanagement/repository) and [`specification`](src/main/java/com/chanchal/employeemanagement/specification) handle persistence and search predicates.
- [`SecurityConfig.java`](src/main/java/com/chanchal/employeemanagement/SecurityConfig.java) and [`security`](src/main/java/com/chanchal/employeemanagement/security) configure stateless authentication and access rules. [`exception`](src/main/java/com/chanchal/employeemanagement/exception) maps application errors to HTTP responses.
- The [`flyway-lab` migrations](src/main/resources/db/migration-lab) are a separate learning exercise. Flyway is disabled for ordinary API startup; do not run the lab migrations against the employee database.
