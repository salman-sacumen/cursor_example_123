# Spring Boot CRUD Application

Simple REST CRUD API for managing books, built with Spring Boot, Spring Data JPA, and H2.

## Requirements

- Java 17+
- Maven 3.8+

## Run

```bash
mvn spring-boot:run
```

App starts at `http://localhost:8080`. H2 console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:cruddb`).

## API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/books` | List books. Optional filters: `title`, `author`, `genre`, `minPrice`, `maxPrice` |
| GET | `/api/books/{id}` | Get book by id |
| GET | `/api/books/isbn/{isbn}` | Get book by ISBN |
| POST | `/api/books` | Create a book (409 if ISBN already exists) |
| PUT | `/api/books/{id}` | Replace a book |
| PATCH | `/api/books/{id}` | Partially update a book |
| DELETE | `/api/books/{id}` | Delete a book |

Optional book fields: `isbn`, `genre`, `publishedYear`.

### Example

```bash
# Create
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"Clean Code","author":"Robert Martin","price":39.99}'

# List
curl http://localhost:8080/api/books

# Get by id
curl http://localhost:8080/api/books/1

# Update
curl -X PUT http://localhost:8080/api/books/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"Clean Code","author":"Robert C. Martin","price":42.00}'

# Delete
curl -X DELETE http://localhost:8080/api/books/1

# Search
curl "http://localhost:8080/api/books?author=Martin&genre=Programming"

# Look up by ISBN
curl http://localhost:8080/api/books/isbn/9780132350884

# Partial update
curl -X PATCH http://localhost:8080/api/books/1 \
  -H "Content-Type: application/json" \
  -d '{"price":29.99,"genre":"Software"}'
```
