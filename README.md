# AgriMitra

> **AI-Driven Precision Agriculture & Soil Health Intelligence Platform**

AgriMitra is a backend RESTful service built with Spring Boot that empowers farmers, agricultural planners, and agronomists with data-driven crop recommendations and soil health management. By digitizing physical Soil Health Cards (SHCs) through multimodal vision AI, integrating real-time regional weather, tracking fertilizer history, and calculating soil test staleness, AgriMitra generates contextual crop recommendations and customized fertilizer advice for Indian farming conditions.

---

## Table of Contents

- [Project Overview](#project-overview)
  - [Core Problem Solved](#core-problem-solved)
  - [Target Users](#target-users)
  - [Key Features](#key-features)
- [Technology Stack](#technology-stack)
- [Architecture & Request Flow](#architecture--request-flow)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Configuration and Environment Variables](#configuration-and-environment-variables)
  - [Environment Variables Reference](#environment-variables-reference)
  - [Setting Up `.env`](#setting-up-env)
- [Local Setup](#local-setup)
- [Running and Testing](#running-and-testing)
  - [Run the Application](#run-the-application)
  - [Run Unit & Integration Tests](#run-unit--integration-tests)
- [API Documentation](#api-documentation)
- [API Overview](#api-overview)
- [Security and Secrets](#security-and-secrets)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

---

## Project Overview

### Core Problem Solved

Indian farmers receive physical Soil Health Cards containing chemical laboratory measurements for 12 standard soil parameters. In practice, these cards often remain underutilized due to:
1. **Manual Transcription Friction**: Complex tabular cards are tedious to parse and interpret manually.
2. **Data Decay**: Soil chemistry evolves over time, yet recommendations frequently use outdated laboratory readings without caveats.
3. **Disconnected Context**: Soil data is rarely evaluated alongside actual field acreage, real-time local weather conditions, or historical fertilizer application records.

AgriMitra automates the entire lifecycle: from digitizing cards via multimodal OCR to orchestrating AI-driven crop and fertilizer advisory grounded in real-time weather and soil test freshness tiers.

### Target Users

- **Farmers**: Digitize Soil Health Cards, monitor field parcels, track fertilizer applications, and receive actionable crop advisories.
- **Agricultural Administrators & Agronomists**: Oversee farmer registries, analyze field soil history, and manage platform-wide records.

### Key Features

- **Role-Based Access Control (RBAC)**: Secure registration and authentication using JWT (JSON Web Tokens) with separate `ADMIN` and `FARMER` privileges and method-level ownership checks (`@PreAuthorize`).
- **Farmer & Field Management**: Hierarchical modeling of farmers, geographic addresses (village, district, state), and individual farm parcel sizes (`areaInAcres`).
- **Multimodal Soil Card OCR**: Direct upload of Soil Health Card images (JPEG/PNG) to AWS S3, processed by **Google Gemini 2.0 Flash Vision** with strict JSON schemas to extract the 12 standard chemical parameters:
  - Nitrogen ($N$), Phosphorus ($P$), Potassium ($K$)
  - pH Level, Electrical Conductivity ($EC$), Organic Carbon ($OC$)
  - Sulphur ($S$), Zinc ($Zn$), Iron ($Fe$), Copper ($Cu$), Manganese ($Mn$), Boron ($B$)
- **Automated Regional Web Fallback**: When an uploaded card image is unreadable, blurry, or invalid, the system automatically triggers a web-grounded estimation via Gemini with **Google Search Grounding**, synthesizing typical regional baseline readings for the farmer's village, district, and state.
- **Live Weather Enrichment**: Real-time atmospheric context (temperature, humidity, weather condition, wind speed) fetched from the **OpenWeatherMap API** based on the farmer's district and state.
- **Fertilizer Usage Tracking**: Field-level historical application tracking (fertilizer type, quantity, unit, application date) to avoid over-fertilization.
- **Four-Tier Soil Staleness Engine**: Evaluates the age of soil records relative to the test date:
  - `FRESH` ($< 6\text{ months}$): Optimal baseline; no staleness warnings.
  - `MODERATE_STALE` ($6\text{ to }24\text{ months}$): Moderate freshness warning suggesting an updated test.
  - `HIGH_STALE` ($24\text{ to }36\text{ months}$): Prompts Gemini to reduce recommendation confidence and append explicit caveats; returns a deprecation warning in the response.
  - `CRITICAL_STALE` ($> 36\text{ months}$): Blocks recommendation generation with an `HTTP 422 Unprocessable Entity` response, requiring a fresh soil card.
- **GenAI Crop & Fertilizer Advisory**: Orchestrates field size, 12 soil parameters, weather data, staleness tier, and recent fertilizer history into Gemini 2.0 Flash to return recommended crops, confidence scores, agronomic reasoning, and fertilizer advice.

---

## Technology Stack

| Component | Technology | Version | Notes |
| :--- | :--- | :--- | :--- |
| **Language** | Java | 21 (LTS) | Modern Java features and syntax |
| **Framework** | Spring Boot | 4.1.1 | Web MVC, Data JPA, Security, Validation |
| **Build Tool** | Apache Maven | 3.9+ | Managed via Maven Wrapper (`mvnw` / `mvnw.cmd`) |
| **Database** | PostgreSQL | 14+ | Relational persistence with Hibernate 6 / PostgreSQLDialect |
| **Security & Auth** | Spring Security & JJWT | 0.12.6 | Stateless JWT Bearer token authentication & BCrypt |
| **Cloud Storage** | AWS SDK for Java v2 (S3) | 2.29.51 | Secure object storage for Soil Health Card images |
| **GenAI / Vision** | Google Gemini API | 2.0 Flash | Vision OCR, Search Grounding, and Crop Recommendation |
| **External Weather** | OpenWeatherMap API | 2.5 Current | Ambient weather conditions by district and state |
| **API Documentation** | Springdoc OpenAPI | 3.1.0 | OpenAPI 3.0 specification & Swagger UI |
| **Boilerplate Reducer**| Project Lombok | Included | Automated getters, setters, builders, and loggers |

---

## Architecture & Request Flow

The crop recommendation workflow coordinates multiple internal services and external APIs in a single request:

```
+-----------------------------------------------------------------------------------+
|                           Client Request (POST)                                   |
|                /api/fields/{fieldId}/recommendations (Bearer JWT)                 |
+-----------------------------------------+-----------------------------------------+
                                          |
                                          v
                 +-------------------------------------------------+
                 |       CropRecommendationController              |
                 |  - Validates JWT & field ownership checks       |
                 +------------------------+------------------------+
                                          |
                                          v
                 +-------------------------------------------------+
                 |       CropRecommendationServiceImpl             |
                 +------------------------+------------------------+
                                          |
        +---------------------------------+---------------------------------+
        |                                 |                                 |
        v                                 v                                 v
+------------------+             +------------------+             +------------------+
| SoilDataService  |             |  WeatherService  |             | FertilizerUsage  |
| - Latest SoilData|             | - OpenWeatherMap |             |   Repository     |
| - Staleness Tier |             |   (Non-blocking) |             | - Last 5 logs    |
+--------+---------+             +--------+---------+             +--------+---------+
         |                                |                                |
         | (If CRITICAL_STALE (>36 mo),   |                                |
         |  returns HTTP 422 immediately) |                                |
         +--------------------------------+--------------------------------+
                                          |
                                          v
                 +-------------------------------------------------+
                 |               GeminiService                     |
                 | - Prompt with 12 nutrients, weather, fertilizer |
                 | - Enforces structured JSON output schema        |
                 +------------------------+------------------------+
                                          |
                                          v
                 +-------------------------------------------------+
                 |       CropRecommendation Entity Saved           |
                 | - Persisted to PostgreSQL                       |
                 | - dataFreshnessWarning computed & returned      |
                 +-------------------------------------------------+
```

---

## Project Structure

```text
agriMitra/
├── .mvn/wrapper/                  # Maven Wrapper runtime binaries
├── src/
│   ├── main/
│   │   ├── java/com/agm/agrimitra/
│   │   │   ├── config/            # Spring configurations (Security, S3, OpenAPI, Jackson)
│   │   │   │   ├── JacksonConfig.java
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   ├── S3Config.java
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── controller/        # REST Controllers (Endpoints & HTTP contracts)
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── CropRecommendationController.java
│   │   │   │   ├── FarmerController.java
│   │   │   │   ├── FertilizerUsageController.java
│   │   │   │   ├── FieldController.java
│   │   │   │   ├── SoilCardController.java
│   │   │   │   └── SoilDataController.java
│   │   │   ├── dto/               # Request & Response Data Transfer Objects
│   │   │   │   ├── AuthResponseDto.java
│   │   │   │   ├── CropRecommendationResponseDto.java
│   │   │   │   ├── CropRecommendationResult.java
│   │   │   │   ├── FarmerRequestDto.java
│   │   │   │   ├── FertilizerUsageRequestDto.java
│   │   │   │   ├── SoilExtractionResult.java
│   │   │   │   ├── WeatherDataDto.java
│   │   │   │   └── ...
│   │   │   ├── entity/            # JPA Entities & Domain Enums
│   │   │   │   ├── Address.java
│   │   │   │   ├── CropRecommendation.java
│   │   │   │   ├── Farmer.java
│   │   │   │   ├── FertilizerUsage.java
│   │   │   │   ├── Field.java
│   │   │   │   ├── Role.java
│   │   │   │   ├── SoilData.java
│   │   │   │   ├── SoilDataSource.java
│   │   │   │   ├── SoilStalenessTier.java
│   │   │   │   └── User.java
│   │   │   ├── exception/         # Custom exceptions & centralized error advice
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   └── UnprocessableEntityException.java
│   │   │   ├── mapper/            # Entity <-> DTO Mapping Layer
│   │   │   ├── repository/        # Spring Data JPA Repositories
│   │   │   ├── security/          # Security filters, JWT utilities & access evaluators
│   │   │   │   ├── CustomUserDetailsService.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── JwtService.java
│   │   │   │   └── SecurityService.java
│   │   │   ├── service/           # Business service interfaces
│   │   │   │   └── impl/          # Service implementations (Gemini, S3, Weather, etc.)
│   │   │   └── AgriMitraApplication.java # Application main class
│   │   └── resources/
│   │       └── application.properties # Main configuration file with env-var fallbacks
│   └── test/                      # Unit and integration test suites
│       └── java/com/agm/agrimitra/service/
│           ├── CropRecommendationServiceTest.java
│           ├── FertilizerUsageServiceTest.java
│           ├── SoilDataStalenessTest.java
│           └── WeatherServiceTest.java
├── .env.example                   # Environment variable template (safe to commit)
├── .gitignore                     # Git exclusion rules
├── mvnw                           # Linux/macOS Maven wrapper script
├── mvnw.cmd                       # Windows Maven wrapper script
└── pom.xml                        # Maven project descriptor & dependency tree
```

---

## Prerequisites

Before running the project locally, verify you have the following installed:

1. **Java Development Kit (JDK) 21**:
   Verify installation:
   ```bash
   java -version
   ```
2. **PostgreSQL (14+)**:
   Ensure a local or containerized PostgreSQL instance is running on port `5432`.
3. **Maven 3.9+** *(optional)*:
   Maven Wrapper (`./mvnw` or `.\mvnw.cmd`) is included in the repository.
4. **External Cloud & API Accounts**:
   - **Google AI Studio**: API Key with access to `gemini-2.0-flash`.
   - **AWS Account**: IAM credentials with `s3:PutObject` permissions and an existing S3 bucket.
   - **OpenWeatherMap**: Free Tier API Key for the Current Weather Data API.

---

## Configuration and Environment Variables

The application is configured through `src/main/resources/application.properties` and dynamically binds settings to environment variables using Spring's `${VAR_NAME:default}` placeholder syntax.

### Environment Variables Reference

| Variable Name | Description | Status | Default / Safe Example |
| :--- | :--- | :--- | :--- |
| `SPRING_DATASOURCE_URL` | JDBC connection string to PostgreSQL | Optional | `jdbc:postgresql://localhost:5432/agrimitra` |
| `SPRING_DATASOURCE_USERNAME` | PostgreSQL database username | Optional | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL database password | **Required** (if password set) | *(empty by default)* |
| `JWT_SECRET` | 256-bit secret key (min 32 characters or Base64) for signing JWTs | **Required** | `your-secure-random-256-bit-secret-key-here` |
| `JWT_EXPIRATION_MS` | JWT token lifespan in milliseconds | Optional | `86400000` *(24 hours)* |
| `AWS_S3_BUCKET_NAME` | S3 bucket name for Soil Health Card uploads | **Required** *(for S3)* | `agrimitra-soil-cards-bucket` |
| `AWS_REGION` | AWS region where the S3 bucket is hosted | Optional | `ap-south-1` |
| `AWS_ACCESS_KEY_ID` | AWS IAM Access Key ID | **Required** *(for S3)* | `AKIAIOSFODNN7EXAMPLE` |
| `AWS_SECRET_ACCESS_KEY` | AWS IAM Secret Access Key | **Required** *(for S3)* | `wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY` |
| `GEMINI_API_KEY` | Google AI Studio API key | **Required** *(for AI/OCR)*| `AIzaSyDummyKeyForGeminiExampleOnly` |
| `GEMINI_MODEL` | Gemini model name | Optional | `gemini-2.0-flash` |
| `OPENWEATHER_API_KEY` | OpenWeatherMap API Key | Optional | `dummyOpenWeatherApiKeyExample` |
| `WEATHER_BASE_URL` | OpenWeatherMap weather endpoint URL | Optional | `https://api.openweathermap.org/data/2.5/weather` |

> [!IMPORTANT]
> The database schema is generated automatically on startup (`spring.jpa.hibernate.ddl-auto=create-drop`). For production or persistent staging environments, adjust this property accordingly.

### Setting Up `.env`

A template file [`.env.example`](file:///.env.example) is provided in the repository root. Create your local `.env` file:

```bash
# In the project directory (where pom.xml is located):
cp .env.example .env
```

Populate `.env` with your actual development keys and database credentials.

> [!CAUTION]
> Never commit `.env` or any file containing real API keys or credentials to version control. The `.gitignore` file is configured to exclude all `.env` files.

---

## Local Setup

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/agriMitra.git
cd agriMitra/agriMitra
```

### 2. Configure Local Database

Ensure PostgreSQL is running, then create the target database:

```sql
CREATE DATABASE agrimitra;
```

### 3. Configure Environment Variables

#### Option A: Export variables in your terminal

- **On Windows (PowerShell)**:
  ```powershell
  $env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/agrimitra"
  $env:SPRING_DATASOURCE_USERNAME="postgres"
  $env:SPRING_DATASOURCE_PASSWORD="your_local_password"
  $env:JWT_SECRET="your-at-least-32-characters-long-jwt-secret-key"
  $env:AWS_S3_BUCKET_NAME="your-s3-bucket-name"
  $env:AWS_ACCESS_KEY_ID="your-aws-access-key"
  $env:AWS_SECRET_ACCESS_KEY="your-aws-secret-key"
  $env:GEMINI_API_KEY="your-gemini-api-key"
  $env:OPENWEATHER_API_KEY="your-openweather-api-key"
  ```

- **On macOS / Linux (bash/zsh)**:
  ```bash
  export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/agrimitra"
  export SPRING_DATASOURCE_USERNAME="postgres"
  export SPRING_DATASOURCE_PASSWORD="your_local_password"
  export JWT_SECRET="your-at-least-32-characters-long-jwt-secret-key"
  export AWS_S3_BUCKET_NAME="your-s3-bucket-name"
  export AWS_ACCESS_KEY_ID="your-aws-access-key"
  export AWS_SECRET_ACCESS_KEY="your-aws-secret-key"
  export GEMINI_API_KEY="your-gemini-api-key"
  export OPENWEATHER_API_KEY="your-openweather-api-key"
  ```

#### Option B: Configure your IDE
In IntelliJ IDEA or VS Code, import the project as a Maven project and add the environment variables to the `AgriMitraApplication` Run/Debug Configuration.

### 4. Build the Project

Compile the project and verify all dependencies:

- **Windows**:
  ```powershell
  .\mvnw.cmd clean compile
  ```
- **macOS / Linux**:
  ```bash
  ./mvnw clean compile
  ```

---

## Running and Testing

### Run the Application

Start the Spring Boot development server:

- **Windows**:
  ```powershell
  .\mvnw.cmd spring-boot:run
  ```
- **macOS / Linux**:
  ```bash
  ./mvnw spring-boot:run
  ```

Once started, the application will listen on the default port:
```text
http://localhost:8080
```

### Run Unit & Integration Tests

Execute the automated test suite with the Maven Wrapper:

- **Windows**:
  ```powershell
  .\mvnw.cmd test
  ```
- **macOS / Linux**:
  ```bash
  ./mvnw test
  ```

To run a specific test suite:
```powershell
.\mvnw.cmd test '-Dtest=CropRecommendationServiceTest'
```

---

## API Documentation

AgriMitra includes interactive API documentation generated via Springdoc OpenAPI 3.0.

Once the application is running, open your browser:

- **Interactive Swagger UI**:
  ```
  http://localhost:8080/swagger-ui/index.html
  ```
- **OpenAPI 3.0 JSON Specification**:
  ```
  http://localhost:8080/v3/api-docs
  ```

### Authorizing in Swagger UI
1. Call `POST /api/auth/login` with your user credentials to receive a JWT token.
2. In Swagger UI, click the green **Authorize** button in the top right.
3. In the `bearerAuth` modal, enter your token (without the `Bearer ` prefix) and click **Authorize**.
4. All subsequent requests executed through the UI will include the `Authorization: Bearer <token>` header.

---

## API Overview

Below is a summary of the controller domains and verified routes:

### 1. Authentication (`/api/auth`)
*Public endpoints for onboarding and authentication.*

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register-farmer` | Register a new farmer account and linked profile | Public |
| `POST` | `/api/auth/register-admin` | Bootstrap an administrative user account | Public |
| `POST` | `/api/auth/login` | Authenticate credentials and receive a JWT token | Public |

### 2. Farmer Profiles (`/api/farmers`)
*Manage agricultural profiles and geographic locations.*

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/farmers` | Create farmer profile manually | Admin only |
| `GET` | `/api/farmers` | Retrieve all registered farmers (paginated) | Admin only |
| `GET` | `/api/farmers/{id}` | Retrieve farmer details by ID | Admin or Owning Farmer |
| `PUT` | `/api/farmers/{id}` | Update farmer information | Admin or Owning Farmer |
| `DELETE` | `/api/farmers/{id}` | Delete a farmer profile | Admin only |

### 3. Fields & Land Parcels (`/api/fields` & `/api/farmers/{farmerId}/fields`)
*Manage distinct agricultural plots belonging to farmers.*

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/farmers/{farmerId}/fields` | Create a new field parcel for a farmer | Admin or Owning Farmer |
| `GET` | `/api/farmers/{farmerId}/fields` | List all fields for a farmer (paginated) | Admin or Owning Farmer |
| `GET` | `/api/fields/{id}` | Retrieve field parcel details | Admin or Field Owner |
| `PUT` | `/api/fields/{id}` | Update field parcel name or acreage | Admin or Field Owner |
| `DELETE` | `/api/fields/{id}` | Delete a field parcel | Admin or Field Owner |

### 4. Soil Health Cards & OCR Extraction (`/api/fields/{fieldId}/soil-card`)
*Direct image processing pipeline with automated web-fallback.*

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/fields/{fieldId}/soil-card` | Upload Soil Health Card image (`multipart/form-data`). Performs S3 upload, Gemini Vision OCR extraction of 12 nutrients, and automated regional search fallback if unreadable | Admin or Field Owner |

### 5. Manual Soil Test Records (`/api/fields/{fieldId}/soil-data` & `/api/soil-data/{id}`)
*Direct CRUD operations on soil test parameters.*

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/fields/{fieldId}/soil-data` | Manually record 12 soil parameters | Admin or Field Owner |
| `GET` | `/api/fields/{fieldId}/soil-data` | List all soil records for a field (paginated) | Admin or Field Owner |
| `GET` | `/api/soil-data/{id}` | Get specific soil record by ID | Admin or Field Owner |
| `DELETE` | `/api/soil-data/{id}` | Delete a soil record by ID | Admin or Field Owner |

### 6. Fertilizer Usage History (`/api/fields/{fieldId}/fertilizer-usage`)
*Track applied fertilizers, quantities, and dates.*

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/fields/{fieldId}/fertilizer-usage` | Log a fertilizer application event | Admin or Field Owner |
| `GET` | `/api/fields/{fieldId}/fertilizer-usage` | List fertilizer usage history (paginated) | Admin or Field Owner |
| `DELETE` | `/api/fertilizer-usage/{id}` | Delete a fertilizer usage record | Admin or Field Owner |

### 7. Crop Recommendations (`/api/fields/{fieldId}/recommendations`)
*AI-driven agronomic recommendation orchestration.*

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/fields/{fieldId}/recommendations` | Generate and save a new crop recommendation. Gathers latest soil test, computes staleness tier, fetches live weather, retrieves fertilizer history, calls Gemini, and attaches freshness warnings | Admin or Field Owner |
| `GET` | `/api/fields/{fieldId}/recommendations` | Get historical recommendations for a field | Admin or Field Owner |
| `GET` | `/api/recommendations/{id}` | Get specific recommendation by ID | Admin or Field Owner |
| `DELETE` | `/api/recommendations/{id}` | Delete recommendation record | Admin or Field Owner |

---

## Security and Secrets

- **Untracked Credentials**: The `.env` file containing local credentials must remain untracked by Git. Always verify with `git status` that `.env` is never staged.
- **Safe Template**: Only [`.env.example`](file:///.env.example) is committed to version control. It contains placeholder descriptions and empty values.
- **Accidental Exposure Response**: If any real credential (database password, JWT secret, AWS access key, or Gemini key) is committed or pushed accidentally:
  1. Revoke the key immediately in the respective provider console (AWS IAM, Google AI Studio, OpenWeatherMap).
  2. Generate a new secret.
  3. Purge the secret from local Git history before pushing.
- **Stateless Authentication**: Passwords are encrypted using BCrypt. Authentication tokens are issued as HMAC-SHA signed JWTs and validated on every request via `JwtAuthenticationFilter`.
- **Granular Authorization**: Endpoint access is enforced using `@PreAuthorize` annotations combined with `SecurityService` to ensure users can only access their own farmer profiles, fields, soil cards, and recommendations.

---

## Troubleshooting

### 1. Database Connection Failure
**Symptom**: `org.postgresql.util.PSQLException: Connection to localhost:5432 refused` or `database "agrimitra" does not exist`.
- Verify PostgreSQL is running:
  - Windows: Check Services (`services.msc`) -> PostgreSQL.
  - macOS/Linux: `sudo systemctl status postgresql` or `brew services list`.
- Verify that database `agrimitra` exists:
  ```bash
  psql -U postgres -c "CREATE DATABASE agrimitra;"
  ```
- Ensure `SPRING_DATASOURCE_PASSWORD` matches your local database password.

### 2. Missing Environment Variables / Authentication Failures
**Symptom**: `java.lang.IllegalArgumentException: JWT secret cannot be null or empty` or `IllegalStateException: Gemini API key is not configured`.
- Confirm that you exported the environment variables in the active terminal session or set them in your IDE Run Configuration.
- Check that variable names match the exact casing in the table above (e.g. `JWT_SECRET`, not `jwt_secret`).

### 3. Port Already in Use
**Symptom**: `Web server failed to start. Port 8080 was already in use.`
- Identify and terminate the conflicting process:
  - **Windows**:
    ```powershell
    netstat -ano | findstr :8080
    taskkill /PID <PID> /F
    ```
  - **macOS / Linux**:
    ```bash
    lsof -i :8080
    kill -9 <PID>
    ```

### 4. Stale Maven Compilation / Lombok Annotation Errors
**Symptom**: Build failures stating getters/setters or builder methods cannot be found.
- Clean and rebuild the Maven target:
  ```powershell
  .\mvnw.cmd clean compile
  ```
- In IntelliJ IDEA: Trigger **File -> Invalidate Caches / Restart**, and ensure **Settings -> Build, Execution, Deployment -> Compiler -> Annotation Processors -> Enable annotation processing** is checked.

---

## Contributing

1. **Create a Feature Branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```
2. **Follow Code Conventions**:
   - Adhere to the existing package structure (`controller`, `service`, `repository`, `entity`, `dto`, `mapper`).
   - Use constructor injection (`@RequiredArgsConstructor` or explicit constructor).
   - Write comprehensive unit tests for all service-layer logic.
3. **Verify Before Committing**:
   Run the test suite and ensure clean compilation:
   ```bash
   .\mvnw.cmd clean test
   ```
4. **Open a Pull Request**: Submit a clear description of the feature or bug fix with steps to test.

---

## License

No license has been specified for this project. All rights are reserved by the repository owner.
