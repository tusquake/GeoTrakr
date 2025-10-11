# GeoTrackr - Real-Time Geofencing System

## Overview

GeoTrackr is a production-ready geofencing and location tracking system built with Spring Boot and React. The application enables real-time monitoring of assets within defined geographical boundaries and provides automated alerts when boundaries are crossed.

## Key Features

### Backend Capabilities
- RESTful API with comprehensive endpoint coverage
- JWT-based authentication and authorization
- Real-time geofence boundary detection using Haversine formula and JTS Topology Suite
- PostgreSQL database with optimized indexing
- OpenAPI 3.0 documentation with Swagger UI
- Docker containerization support
- Comprehensive event logging and analytics

### Frontend Capabilities
- Modern responsive user interface with dark theme
- Interactive map visualization using Leaflet.js
- Real-time data synchronization with React Query
- Advanced analytics and reporting with Recharts
- Type-safe development with TypeScript
- Form validation with React Hook Form and Zod
- Global state management with Zustand

## Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.0
- **Security**: Spring Security 6.1 with JWT
- **Database**: PostgreSQL 15 / H2 (development)
- **Geospatial**: JTS Topology Suite 1.19.0
- **Documentation**: OpenAPI 3.0 (Swagger)
- **Build Tool**: Maven 3.6+
- **Java Version**: 17

### Frontend
- **Framework**: React 18
- **Language**: TypeScript 5.2
- **Build Tool**: Vite 5.0
- **State Management**: React Query, Zustand
- **UI Framework**: Tailwind CSS 3.3
- **Mapping**: Leaflet.js with React-Leaflet
- **Charts**: Recharts 2.10
- **HTTP Client**: Axios 1.6

## Prerequisites

- Java Development Kit (JDK) 17 or higher
- Maven 3.6 or higher
- Node.js 18 or higher
- PostgreSQL 15 or higher (optional - H2 available for development)
- Docker and Docker Compose (optional)

## Installation

### Backend Setup

1. Clone the repository
```bash
git clone https://github.com/yourusername/geotrackr.git
cd geotrackr
```

2. Configure database connection in `src/main/resources/application.yml`
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/geotrackr
    username: your_username
    password: your_password
```

3. Build and run the application
```bash
mvn clean install
mvn spring-boot:run
```

Alternatively, use H2 in-memory database for development:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

The backend will be available at `http://localhost:8080`

### Frontend Setup

1. Navigate to frontend directory
```bash
cd geotrackr-frontend
```

2. Install dependencies
```bash
npm install
```

3. Create `.env` file
```bash
VITE_API_URL=http://localhost:8080/api
```

4. Start development server
```bash
npm run dev
```

The frontend will be available at `http://localhost:3000`

### Docker Deployment

Start all services using Docker Compose:
```bash
docker-compose up -d
```

This will start:
- Spring Boot application on port 8080
- PostgreSQL database on port 5432
- pgAdmin on port 5050 (optional)

## Project Structure

```
geotrackr/
├── src/main/java/com/tushar/geotrackr/
│   ├── config/              # Configuration classes
│   ├── controller/          # REST controllers
│   ├── dto/                 # Data transfer objects
│   ├── exception/           # Exception handlers
│   ├── model/               # JPA entities
│   ├── repository/          # Data access layer
│   ├── security/            # Security configuration
│   ├── service/             # Business logic
│   └── GeoTrackrApplication.java
├── src/main/resources/
│   ├── application.yml      # Application configuration
│   └── data.sql            # Sample data
├── src/test/                # Unit and integration tests
├── pom.xml                  # Maven configuration
├── Dockerfile              # Container definition
└── docker-compose.yml      # Multi-container setup

geotrackr-frontend/
├── src/
│   ├── components/         # Reusable UI components
│   ├── pages/              # Application pages
│   ├── services/           # API integration
│   ├── store/              # State management
│   ├── App.tsx             # Root component
│   └── main.tsx            # Entry point
├── package.json            # Dependencies
├── vite.config.ts          # Build configuration
└── tailwind.config.js      # Styling configuration
```

## API Documentation

Once the backend is running, access the interactive API documentation at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

### Main API Endpoints

**Authentication**
- POST `/api/auth/register` - Register new user
- POST `/api/auth/login` - Authenticate and receive JWT token

**Assets**
- GET `/api/assets` - List all assets
- POST `/api/assets` - Create new asset
- PUT `/api/assets/{id}` - Update asset
- DELETE `/api/assets/{id}` - Delete asset

**Geofences**
- GET `/api/geofences` - List all geofences
- POST `/api/geofences` - Create new geofence
- PUT `/api/geofences/{id}` - Update geofence
- DELETE `/api/geofences/{id}` - Delete geofence

**Location Tracking**
- POST `/api/location/update` - Update asset location
- GET `/api/location/asset/{id}` - Get asset location
- GET `/api/location/all` - Get all asset locations

**Events**
- GET `/api/events` - List all events
- GET `/api/events/asset/{id}` - Get events for asset
- GET `/api/events/geofence/{id}` - Get events for geofence
- GET `/api/events/statistics` - Get event analytics

## Usage Examples

### Creating a Circular Geofence

```bash
curl -X POST http://localhost:8080/api/geofences \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Office Zone",
    "description": "Main office perimeter",
    "type": "CIRCULAR",
    "centerLatitude": 22.5726,
    "centerLongitude": 88.3639,
    "radius": 500,
    "alertType": "BOTH"
  }'
```

### Updating Asset Location

```bash
curl -X POST http://localhost:8080/api/location/update \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "assetId": 1,
    "latitude": 22.5726,
    "longitude": 88.3639,
    "timestamp": "2025-10-11T10:30:00"
  }'
```

## Testing

### Backend Tests

Run unit and integration tests:
```bash
mvn test
```

### API Testing

Import the Postman collection from `GeoTrackr.postman_collection.json` for comprehensive API testing.

### Frontend Development

```bash
npm run dev    # Start development server
npm run build  # Create production build
npm run preview # Preview production build
```

## Configuration

### Backend Configuration

Key configuration options in `application.yml`:

```yaml
jwt:
  secret: your-secret-key
  expiration: 86400000  # 24 hours

geofence:
  check-interval: 5000  # milliseconds
  distance-unit: KILOMETERS

spring:
  jpa:
    hibernate:
      ddl-auto: update  # Use 'validate' in production
```

### Frontend Configuration

Environment variables in `.env`:

```bash
VITE_API_URL=http://localhost:8080/api
```

## Database Schema

The application uses four main entities:

- **Users**: Authentication and user management
- **Assets**: Tracked entities (vehicles, persons, devices, packages)
- **Geofences**: Virtual boundaries (circular or polygonal)
- **GeofenceEvents**: Historical record of boundary crossings

Refer to the entity classes in `src/main/java/com/tushar/geotrackr/model/` for detailed schema information.

## Security Considerations

### Implemented Security Measures
- JWT-based stateless authentication
- BCrypt password hashing
- Role-based access control (RBAC)
- CORS configuration for cross-origin requests
- SQL injection prevention through JPA
- Input validation using Bean Validation
- XSS protection through output encoding

### Production Recommendations
- Enable HTTPS/TLS encryption
- Implement rate limiting
- Configure firewall rules
- Use environment variables for sensitive configuration
- Regular security audits and dependency updates
- Implement audit logging
- Set up monitoring and alerting

## Performance Optimization

### Backend
- Database connection pooling (HikariCP)
- Strategic database indexing
- Query optimization
- Efficient geospatial algorithms
- Response caching where appropriate

### Frontend
- Code splitting and lazy loading
- React Query caching
- Optimized bundle size
- Efficient re-rendering through React memoization
- Asset optimization

## Deployment

### Production Build

**Backend:**
```bash
mvn clean package
java -jar target/geotrackr-1.0.0.jar
```

**Frontend:**
```bash
npm run build
# Deploy dist/ folder to static hosting
```

### Docker Production Deployment

```bash
docker-compose -f docker-compose.prod.yml up -d
```

## Troubleshooting

### Common Issues

**Backend won't start**
- Verify Java 17+ is installed
- Check database connectivity
- Review application logs in `logs/` directory

**Frontend can't connect to backend**
- Verify backend is running on port 8080
- Check CORS configuration in SecurityConfig
- Verify VITE_API_URL in .env file

**Authentication failures**
- Verify JWT secret is configured
- Check token expiration time
- Ensure Authorization header format: `Bearer <token>`

**Map not displaying**
- Verify Leaflet CSS is imported
- Check browser console for errors
- Ensure container has defined height


## Roadmap

### Planned Features
- WebSocket support for real-time push notifications
- Mobile applications (iOS and Android)
- Email and SMS alert integration
- Advanced analytics with machine learning
- Multi-tenancy support
- Integration with IoT platforms
- Heatmap visualization
- Custom alert rules engine

### Technical Improvements
- Redis caching layer
- Message queue integration
- Microservices architecture
- Enhanced monitoring and logging
- Performance optimization
- Additional test coverage