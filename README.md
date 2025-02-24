# Twitter Clone REST API Documentation

## 1. Project Overview

### 1.1 Technologies

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- AWS S3
- JWT Authentication
- Swagger/OpenAPI

### 1.2 Project Architecture
![diagram.png](../../Desktop/diagram.png)

## 2. Setup and Configuration

### 2.1 Required Environment Variables

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/twitter_clone
spring.datasource.username=your_username
spring.datasource.password=your_password

# AWS S3 Configuration
aws.access.key.id=your_access_key
aws.secret.access.key=your_secret_key
aws.s3.bucket=your_bucket_name
aws.s3.region=your_region

# JWT Configuration
app.jwt.secret=your_jwt_secret
app.jwt.access-token.expiration=1800000
app.jwt.refresh-token.expiration=604800000

### 2.2 Database Schema
![diagram2.png](../../Desktop/diagram2.png)

## 3. API Endpoints

### 3.1 Authentication

#### Register

POST /auth/register
Content-Type: application/json

{
"firstName": "string",
"lastName": "string",
"username": "string",
"email": "string",
"password": "string",
"bio": "string",
"profileImage": "string",
"headerImage": "string"
}

### Login

POST /auth/login
Content-Type: application/json

{
"email": "string",
"password": "string"
}

### 3.2 Tweet Operations

#### Create Tweet

POST /tweet
Content-Type: multipart/form-data

content: string
media: file (optional)

### Reply to Tweet

POST /tweet/{tweetId}/reply
Content-Type: multipart/form-data

content: string
media: file (optional)

### Retweet

POST /tweet/{tweetId}/retweet

### 3.3 User Operations

#### Update Profile Image

PUT /user/profile-image
Content-Type: multipart/form-data

file: image

### Follow User

POST /follow/{followingId}

## 4. Data Models

### 4.1 User Entity

public class User {
Long id;
String firstName;
String lastName;
String username;
String email;
String password;
String bio;
String profileImage;
String headerImage;
Integer followersCount;
Integer followingCount;
Integer tweetsCount;
Boolean verified;
Boolean privateAccount;
}

### 4.2 Tweet Entity

public class Tweet {
Long id;
String content;
TweetType tweetType;
String mediaUrl;
MediaType mediaType;
Integer likeCount;
Integer retweetCount;
Integer replyCount;
User user;
Tweet parentTweet;
}

## 5. Security

### 5.1 JWT Token Structure

```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
        "id": "number",
        "username": "string",
        "email": "string"
    }
}
```

### 5.2 Authorization

- JWT-based authentication
- Role-based access control (RBAC)
- Token refresh mechanism


## 6. Error Handling

### 6.1 Error Response Format

```json
{
    "message": "Error message",
    "status": 400
}
```

### 6.2 Common Error Codes

- 400: Bad Request
- 401: Unauthorized
- 403: Forbidden
- 404: Not Found
- 500: Internal Server Error


## 7. AWS S3 Integration

### 7.1 Supported File Types

- Images: JPEG, PNG, GIF, WEBP
- Video: MP4
- Maximum file size: 100MB


### 7.2 Media URL Structure

```plaintext
https://{bucket-name}.s3.{region}.amazonaws.com/{file-name}
```

## 8. Performance Optimizations

### 8.1 Database Indexes

- Unique indexes for username and email
- Full-text search index for tweet content
- Composite indexes for follow relationships


### 8.2 Caching Strategy

- Cache for tweet details
- Cache for user profiles
- Counter cache for likes and retweets


## 9. Testing

### 9.1 Test Coverage

- Unit tests
- Integration tests
- API tests


## 10. Deployment

### 10.1 System Requirements

- Java 17 or higher
- PostgreSQL 12 or higher
- AWS S3 bucket
- Minimum 1GB RAM


### 10.2 Deployment Steps

1. Database setup
2. Environment variables configuration
3. JAR file creation
4. Application startup


## 11. API Response Examples

### 11.1 Successful Tweet Creation

```json
{
    "id": 1,
    "content": "Hello World!",
    "mediaUrl": "https://s3.amazonaws.com/bucket/image.jpg",
    "mediaType": "IMAGE",
    "likeCount": 0,
    "retweetCount": 0,
    "replyCount": 0,
    "createdAt": "2024-02-25T13:00:00Z"
}
```

### 11.2 User Profile Response

```json
{
    "id": 1,
    "username": "johndoe",
    "fullName": "John Doe",
    "bio": "Software Developer",
    "profileImage": "https://s3.amazonaws.com/bucket/profile.jpg",
    "followersCount": 100,
    "followingCount": 50,
    "tweetsCount": 25
}
```

## 12. Rate Limiting

### 12.1 API Limits

- Tweet creation: 300 per day
- Follow actions: 400 per day
- Likes: 1000 per day


### 12.2 Rate Limit Headers

```plaintext
X-RateLimit-Limit: 300
X-RateLimit-Remaining: 299
X-RateLimit-Reset: 1614556800
```

## 13. Monitoring and Logging

### 13.1 Logging Format

```json
{
    "timestamp": "2024-02-25T13:00:00Z",
    "level": "INFO",
    "thread": "main",
    "logger": "com.twitter.service.TweetService",
    "message": "Tweet created successfully",
    "userId": 1
}
```

### 13.2 Metrics

- API response times
- Database query performance
- S3 upload/download times
- Error rates

This documentation covers the fundamental components and usage of the project. 
It can be updated and expanded during the development process.