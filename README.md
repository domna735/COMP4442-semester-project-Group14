# COMP4442 Semester Project Group 14

Spring Boot cloud computing service for COMP4442.

## Step 1 Scope
- Create baseline Spring Boot service
- Provide a compute API endpoint
- Prepare project for cloud deployment on AWS EC2

## API (Initial)
- `GET /api/v1/compute/ping`
- `POST /api/v1/compute/calculate`

### Example Request
```json
{
  "operandA": 5,
  "operandB": 3,
  "operator": "ADD"
}
```

### Example Response
```json
{
  "expression": "5.0 + 3.0",
  "result": 8.0,
  "timestamp": "2026-03-20T00:00:00"
}
```

## Local Run (after Java and Maven are installed)
```bash
mvn spring-boot:run
```

Then open `http://localhost:8080/api/v1/compute/ping`.
