# World Cup USA 2026 Prediction Pool

Spring Boot web app for predicting FIFA World Cup 2026 group stage scores. Built to mirror the deployment pattern of [my-finance-watcher](https://github.com/alperozdamar/my-finance-watcher): Maven, Thymeleaf, MySQL, Docker, and GitHub Actions → Docker Hub → EC2.

## Stack

- Java 21, Spring Boot 3.3
- Spring MVC + Thymeleaf + Spring Security (JDBC auth)
- Spring Data JPA / Hibernate
- MySQL 8

## Features (phase 1)

- User login with JDBC-backed credentials
- 72 group stage fixtures imported from `Fixtures.txt` (Raleigh / America/New_York local times, stored as UTC)
- Users enter and update score predictions until kickoff
- Per-user timezone display (Settings page)
- Admin enters actual match scores
- Points calculation stub (rules TBD)
- Knockout fixtures imported but predictions disabled until group stage completes

## Local development

**Requires Java 21.** If you use jenv, the repo includes `.java-version` (run `jenv local 21` once if needed). Verify with `java -version` before starting Maven — if it shows 17, `spring-boot:run` will fail with `UnsupportedClassVersionError`.

1. Start MySQL:

```bash
docker compose -f docker-compose.local.yml up -d
```

2. Optional local overrides:

```bash
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
```

3. Run the app:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

4. Open http://localhost:8090

### Players (default password `123` for all — change via Profile → Change password)

| Username | Name | Role |
|----------|------|------|
| alper | Alper Ozdamar | Admin |
| gonenc | Gonenc Gorgulu | User |
| tcan | Tayyip Can | User |
| kubilay | Kubilay Kahraman | User |
| ali | Ali Sahin | User |
| sadik | Sadik Demirdogen | User |
| adem | Adem Sari | User |

## Deployment

Same as my-finance-watcher:

- `mvn verify` builds the JAR
- `Dockerfile` packages the JAR on port **8090**
- `.github/workflows/deploy.yml` builds, pushes to Docker Hub, and deploys to EC2 via SSH

Configure GitHub secrets: `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`, `EC2_HOST`, `EC2_USER`, `EC2_SSH_KEY`, `MYSQL_ROOT_PASSWORD`.

## Project layout

```
src/main/java/com/alper/worldcup/
  config/       Security, data loader
  controller/   MVC controllers
  service/      Business logic, fixture import
  dao/          JPA repositories
  entity/       JPA entities
src/main/resources/
  data/fixtures.txt   Imported FIFA schedule
  templates/          Thymeleaf views
db/setup.sql          Spring Security users
```

## Rules

Implemented — see `/rules` in the app or:

- [English](document/PrinciplesAndRules.en.md)
- [Turkish](document/PrinciplesAndRules.tr.md)

See [Changelog.md](Changelog.md) for project history.

## Next steps

- Enable knockout-stage predictions once group results are known
- Resolve knockout placeholder teams (e.g. `1A`, `2B`) after group stage
- Optional: group winner / champion picks (phase 2)
