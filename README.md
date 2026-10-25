# Penny Pals

**Student Name:** Michael Deur  
**Course:** CS 408 - Full Stack Web Development  
**Semester:** Fall 2026  

---

## Overview

**Penny Pals** is a full-stack web application designed to help young adults and students build healthy savings habits through interactive gamification. The application combines personal finance tracking, budget goal setting, and a gamified virtual pet simulation.

The core problem this application solves is the lack of engagement in mainstream budgeting apps. Saving money requires delayed gratification, which can make traditional expense tracking dry and unappealing. Penny Pals addresses this by converting financial milestones, including logging income, tracking daily expenses, staying under custom budgets, and completing financial quests, into visual rewards within an interactive 2D aquarium filled with fish and underwater decorations.

---

## Features & 7-Page Architecture

1. **Landing Page (`/`)**: Public landing interface with user registration, authentication, and conditional routing to the dashboard.
2. **2D Interactive Aquarium (`/aquarium`)**: Main application dashboard featuring an HTML5 Canvas 2D environment that renders equipped fish and decorations based on database state.
3. **Daily Quests (`/quests`)**: Checklist dashboard for daily and weekly financial habits where users can claim "pennies" upon completion.
4. **Transaction Search (`/transactions`)**: Paginated list of user transactions with inline update/delete forms and dynamic Spring Data JPA specification search filtering.
5. **Budget Goals (`/budget`)**: Spending limits and savings targets management with an automated reward engine for staying under budget.
6. **Fish & Decor Shop (`/shop`)**: E-commerce grid to purchase exotic fish and decorations using earned pennies and equip items to the aquarium canvas.
7. **Add Transaction (`/add-transaction`)**: Focused form for rapid financial data entry with validation.

---

## Tech Stack

- **Backend Language:** Java 21 (OpenJDK 21)
- **Backend Framework:** Spring Boot 3.3+
- **Database:** Embedded H2 / SQLite with Spring Data JPA & Hibernate
- **Frontend Templates:** Thymeleaf
- **Frontend Styling:** Bootstrap 5 & Custom CSS
- **Build Tool:** Maven
- **Testing:** JUnit 5 & Spring Test
- **Hosting Platform:** AWS EC2 (Ubuntu 24.04 LTS)

---

## Local Development Setup

To run Penny Pals locally on your machine for development and testing, use the automated startup script included in the repository.

### Prerequisites
- Git installed.
- *(Note: The startup script automatically checks for Java 21 and Maven, and will attempt to install them on supported Linux/macOS environments if missing).*

### Steps to Run Locally
1. **Clone the repository:**
   ```bash
   git clone https://github.com/michaeldeur/PennyPals.git
   cd pennypals
   ```
Run the startup script:

```bash
./start.sh
```
This script handles dependency checks, packages the application via Maven, and starts the server.

Press Ctrl+C in your terminal to cleanly stop the application.

## Results
### Running Locally Documentation
```
ubuntu@ip-172-31-41-72:~$ git clone https://github.com/michaeldeur/PennyPals.git
Cloning into 'PennyPals'...
remote: Enumerating objects: 150, done.
remote: Counting objects: 100% (150/150), done.
remote: Compressing objects: 100% (94/94), done.
remote: Total 150 (delta 44), reused 118 (delta 26), pack-reused 0 (from 0)
Receiving objects: 100% (150/150), 37.82 KiB | 2.52 MiB/s, done.
Resolving deltas: 100% (44/44), done.
ubuntu@ip-172-31-41-72:~$ ls
PennyPals
ubuntu@ip-172-31-41-72:~$ cd PennyPals/
ubuntu@ip-172-31-41-72:~/PennyPals$ ./start.sh
==> Building Penny Pals with Maven...
==> Starting on http://localhost:8080 -- press Ctrl+C to stop

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v3.3.4)

2026-10-23T22:10:28.948Z  INFO 143751 --- [           main] com.pennypals.PennyPalsApplication       : Starting PennyPalsApplication v0.0.1-SNAPSHOT using Java 21.0.12.1 with PID 143751 (/home/ubuntu/PennyPals/target/pennypals-0.0.1-SNAPSHOT.jar started by ubuntu in /home/ubuntu/PennyPals)
2026-10-23T22:10:28.957Z  INFO 143751 --- [           main] com.pennypals.PennyPalsApplication       : No active profile set, falling back to 1 default profile: "default"
2026-10-23T22:10:30.605Z  INFO 143751 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2026-10-23T22:10:30.762Z  INFO 143751 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 140 ms. Found 5 JPA repository interfaces.
2026-10-23T22:10:32.166Z  INFO 143751 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
2026-10-23T22:10:32.198Z  INFO 143751 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2026-10-23T22:10:32.199Z  INFO 143751 --- [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
2026-10-23T22:10:32.447Z  INFO 143751 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2026-10-23T22:10:32.448Z  INFO 143751 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 3374 ms
2026-10-23T22:10:32.533Z  INFO 143751 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2026-10-23T22:10:32.912Z  INFO 143751 --- [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection conn0: url=jdbc:h2:mem:pennypalsdb user=SA
2026-10-23T22:10:32.915Z  INFO 143751 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2026-10-23T22:10:32.943Z  INFO 143751 --- [           main] o.s.b.a.h2.H2ConsoleAutoConfiguration    : H2 console available at '/h2-console'. Database available at 'jdbc:h2:mem:pennypalsdb'
2026-10-23T22:10:33.698Z  INFO 143751 --- [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2026-10-23T22:10:33.879Z  INFO 143751 --- [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.5.3.Final
2026-10-23T22:10:33.962Z  INFO 143751 --- [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
2026-10-23T22:10:34.621Z  INFO 143751 --- [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
2026-10-23T22:10:34.716Z  WARN 143751 --- [           main] org.hibernate.orm.deprecation            : HHH90000025: H2Dialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
2026-10-23T22:10:36.545Z  INFO 143751 --- [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-10-23T22:10:36.593Z  INFO 143751 --- [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-10-23T22:10:36.742Z  INFO 143751 --- [           main] o.s.b.a.w.s.WelcomePageHandlerMapping    : Adding welcome page template: index
2026-10-23T22:10:38.443Z  INFO 143751 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path '/'
2026-10-23T22:10:38.489Z  INFO 143751 --- [           main] com.pennypals.PennyPalsApplication       : Started PennyPalsApplication in 10.526 seconds (process running for 11.749)
2026-10-23T22:19:31.243Z  INFO 143751 --- [nio-8080-exec-2] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
2026-10-23T22:19:31.245Z  INFO 143751 --- [nio-8080-exec-2] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
2026-10-23T22:19:31.246Z  INFO 143751 --- [nio-8080-exec-2] o.s.web.servlet.DispatcherServlet        : Completed initialization in 1 ms
```

### After Reboot Documentation
```
ubuntu@ip-172-31-15-42:~$ sudo journalctl -u pennypals -b 0 --no-pager
Oct 25 22:14:39 ip-172-31-15-42 systemd[1]: Started pennypals.service - Penny Pals Web Application.
Oct 25 22:14:49 ip-172-31-15-42 java[569]:   .   ____          _            __ _ _
Oct 25 22:14:49 ip-172-31-15-42 java[569]:  /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
Oct 25 22:14:49 ip-172-31-15-42 java[569]: ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
Oct 25 22:14:49 ip-172-31-15-42 java[569]:  \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
Oct 25 22:14:49 ip-172-31-15-42 java[569]:   '  |____| .__|_| |_|_| |_\__, | / / / /
Oct 25 22:14:49 ip-172-31-15-42 java[569]:  =========|_|==============|___/=/_/_/_/
Oct 25 22:14:49 ip-172-31-15-42 java[569]:  :: Spring Boot ::                (v3.3.4)
Oct 25 22:14:49 ip-172-31-15-42 java[569]: 2026-10-25T22:14:49.801Z  INFO 569 --- [           main] com.pennypals.PennyPalsApplication       : Starting PennyPalsApplication v0.0.1-SNAPSHOT using Java 21.0.12.1 with PID 569 (/home/ubuntu/PennyPals/target/pennypals-0.0.1-SNAPSHOT.jar started by ubuntu in /home/ubuntu/PennyPals)
Oct 25 22:14:49 ip-172-31-15-42 java[569]: 2026-10-25T22:14:49.826Z  INFO 569 --- [           main] com.pennypals.PennyPalsApplication       : No active profile set, falling back to 1 default profile: "default"
Oct 25 22:14:53 ip-172-31-15-42 java[569]: 2026-10-25T22:14:53.793Z  INFO 569 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
Oct 25 22:14:53 ip-172-31-15-42 java[569]: 2026-10-25T22:14:53.982Z  INFO 569 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 170 ms. Found 5 JPA repository interfaces.
Oct 25 22:14:55 ip-172-31-15-42 java[569]: 2026-10-25T22:14:55.590Z  INFO 569 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port 8080 (http)
Oct 25 22:14:55 ip-172-31-15-42 java[569]: 2026-10-25T22:14:55.627Z  INFO 569 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
Oct 25 22:14:55 ip-172-31-15-42 java[569]: 2026-10-25T22:14:55.627Z  INFO 569 --- [           main] o.apache.catalina.core.StandardEngine    : Starting Servlet engine: [Apache Tomcat/10.1.30]
Oct 25 22:14:55 ip-172-31-15-42 java[569]: 2026-10-25T22:14:55.918Z  INFO 569 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
Oct 25 22:14:55 ip-172-31-15-42 java[569]: 2026-10-25T22:14:55.922Z  INFO 569 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 5777 ms
Oct 25 22:14:56 ip-172-31-15-42 java[569]: 2026-10-25T22:14:56.001Z  INFO 569 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
Oct 25 22:14:56 ip-172-31-15-42 java[569]: 2026-10-25T22:14:56.530Z  INFO 569 --- [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection conn0: url=jdbc:h2:mem:pennypalsdb user=SA
Oct 25 22:14:56 ip-172-31-15-42 java[569]: 2026-10-25T22:14:56.533Z  INFO 569 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
Oct 25 22:14:56 ip-172-31-15-42 java[569]: 2026-10-25T22:14:56.553Z  INFO 569 --- [           main] o.s.b.a.h2.H2ConsoleAutoConfiguration    : H2 console available at '/h2-console'. Database available at 'jdbc:h2:mem:pennypalsdb'
Oct 25 22:14:57 ip-172-31-15-42 java[569]: 2026-10-25T22:14:57.413Z  INFO 569 --- [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
Oct 25 22:14:57 ip-172-31-15-42 java[569]: 2026-10-25T22:14:57.691Z  INFO 569 --- [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 6.5.3.Final
Oct 25 22:14:57 ip-172-31-15-42 java[569]: 2026-10-25T22:14:57.783Z  INFO 569 --- [           main] o.h.c.internal.RegionFactoryInitiator    : HHH000026: Second-level cache disabled
Oct 25 22:14:58 ip-172-31-15-42 java[569]: 2026-10-25T22:14:58.568Z  INFO 569 --- [           main] o.s.o.j.p.SpringPersistenceUnitInfo      : No LoadTimeWeaver setup: ignoring JPA class transformer
Oct 25 22:14:58 ip-172-31-15-42 java[569]: 2026-10-25T22:14:58.699Z  WARN 569 --- [           main] org.hibernate.orm.deprecation            : HHH90000025: H2Dialect does not need to be specified explicitly using 'hibernate.dialect' (remove the property setting and it will be selected by default)
Oct 25 22:15:00 ip-172-31-15-42 java[569]: 2026-10-25T22:15:00.817Z  INFO 569 --- [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
Oct 25 22:15:00 ip-172-31-15-42 java[569]: 2026-10-25T22:15:00.899Z  INFO 569 --- [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
Oct 25 22:15:01 ip-172-31-15-42 java[569]: 2026-10-25T22:15:01.091Z  INFO 569 --- [           main] o.s.b.a.w.s.WelcomePageHandlerMapping    : Adding welcome page template: index
Oct 25 22:15:03 ip-172-31-15-42 java[569]: 2026-10-25T22:15:03.309Z  INFO 569 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path '/'
Oct 25 22:15:03 ip-172-31-15-42 java[569]: 2026-10-25T22:15:03.372Z  INFO 569 --- [           main] com.pennypals.PennyPalsApplication       : Started PennyPalsApplication in 17.141 seconds (process running for 21.288)
```

## Project Status & Checkpoints
[x] Checkpoint 1: Repository setup, Java 21 / Spring Boot backend initialization, initial Thymeleaf layout structures, landing page implementation, automated start/deploy scripts,systemd auto-boot configuration, and local development setup documentation.

[x] Checkpoint 2: Database schema configuration (Entities, foreign keys, repositories) and initial automated connectivity tests.

[ ] Checkpoint 3: User authentication and transaction ownership isolation.

[ ] Checkpoint 4: Transaction search, pagination, and dynamic filtering.

[ ] Checkpoint 5: Budget goals engine and 2D Aquarium Canvas rendering.

[ ] Checkpoint 6: Shop inventory toggles and daily quest rewards.

[ ] Checkpoint 7: Final deployment verification, test suite execution, and comprehensive UI walkthrough.
