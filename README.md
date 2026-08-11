#Expense Tracker Monolithic Backend

A robust RESTful API backend built with **Spring Boot** and **PostgreSQL** for managing personal finances, tracking recurring payments, and generating expense analytics.

---

##  Key Features

* **Complete Expense Management (CRUD):**
  * Create, view, edit/update, and delete expenses.
  * Search and filter expenses by custom date ranges or categories.

* **Automated Recurring Expense Scheduler:**
  * Uses Spring's `@Scheduled` background worker running daily at midnight.
  * Automatically calculates due dates (Daily, Weekly, Monthly, Yearly) and generates auto-expense entries without manual user intervention.

* **Python-Powered Chart Visualization:**
  * Uses a Python (`matplotlib`) subprocess integration via standard I/O streams to generate clean pie charts representing category-wise spending distribution.
  * Dynamically returns headless `IMAGE_PNG` data back to the Spring Boot REST endpoints.

* **High-Performance Redis Caching:**
  * Integrated **Spring Cache with Redis** using custom Jackson serializers (with Java 8 Time support) to accelerate repeated data fetching and reduce database load.

---

## Future Enhancements & Roadmap

* [ ] **Frontend Application:** A modern web user interface (React/TypeScript) is currently in development and will be integrated soon.
* [ ] **AI-Powered Insights:** AI integration is planned to provide smart expense categorization, budget recommendations, and anomaly detection in user spending.

---

##  Tech Stack

* **Backend Framework:** Java 21, Spring Boot 3
* **Database & ORM:** PostgreSQL, Spring Data JPA, Hibernate
* **Caching:** Redis, Spring Cache
* **Data Processing & Analytics:** Python 3 (`matplotlib`), Jackson, ModelMapper
* **Security & Auth:** Spring Security, JWT (JSON Web Tokens)
* **Build Tool:** Maven

---

## Getting Started

### Prerequisites
* **Java 17+**
* **PostgreSQL** running locally or in Docker
* **Redis** server running locally or via Docker
* **Python 3** with `matplotlib` installed (`pip install matplotlib`)

### Configuration (`application.properties`)
Ensure your database and Redis connection properties are configured:

```properties
# PostgreSQL Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/expense_tracker
spring.datasource.username=YOUR_POSTGRES_USER
spring.datasource.password=YOUR_POSTGRES_PASSWORD

# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379
