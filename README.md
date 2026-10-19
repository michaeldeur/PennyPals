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

## Project Status & Checkpoints
[x] Checkpoint 1: Repository setup, Java 21 / Spring Boot backend initialization, initial Thymeleaf layout structures, landing page implementation, automated start/deploy scripts,systemd auto-boot configuration, and local development setup documentation.

[ ] Checkpoint 2: Database schema configuration (Entities, foreign keys, repositories) and initial automated connectivity tests.

[ ] Checkpoint 3: User authentication and transaction ownership isolation.

[ ] Checkpoint 4: Transaction search, pagination, and dynamic filtering.

[ ] Checkpoint 5: Budget goals engine and 2D Aquarium Canvas rendering.

[ ] Checkpoint 6: Shop inventory toggles and daily quest rewards.

[ ] Checkpoint 7: Final deployment verification, test suite execution, and comprehensive UI walkthrough.
