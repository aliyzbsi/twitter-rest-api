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

```mermaid
Diagram.download-icon {
            cursor: pointer;
            transform-origin: center;
        }
        .download-icon .arrow-part {
            transition: transform 0.35s cubic-bezier(0.35, 0.2, 0.14, 0.95);
             transform-origin: center;
        }
        button:has(.download-icon):hover .download-icon .arrow-part, button:has(.download-icon):focus-visible .download-icon .arrow-part {
          transform: translateY(-1.5px);
        }
        #mermaid-diagram-ri95{font-family:var(--font-geist-sans);font-size:12px;fill:#000000;}#mermaid-diagram-ri95 .error-icon{fill:#552222;}#mermaid-diagram-ri95 .error-text{fill:#552222;stroke:#552222;}#mermaid-diagram-ri95 .edge-thickness-normal{stroke-width:1px;}#mermaid-diagram-ri95 .edge-thickness-thick{stroke-width:3.5px;}#mermaid-diagram-ri95 .edge-pattern-solid{stroke-dasharray:0;}#mermaid-diagram-ri95 .edge-thickness-invisible{stroke-width:0;fill:none;}#mermaid-diagram-ri95 .edge-pattern-dashed{stroke-dasharray:3;}#mermaid-diagram-ri95 .edge-pattern-dotted{stroke-dasharray:2;}#mermaid-diagram-ri95 .marker{fill:#666;stroke:#666;}#mermaid-diagram-ri95 .marker.cross{stroke:#666;}#mermaid-diagram-ri95 svg{font-family:var(--font-geist-sans);font-size:12px;}#mermaid-diagram-ri95 p{margin:0;}#mermaid-diagram-ri95 .label{font-family:var(--font-geist-sans);color:#000000;}#mermaid-diagram-ri95 .cluster-label text{fill:#333;}#mermaid-diagram-ri95 .cluster-label span{color:#333;}#mermaid-diagram-ri95 .cluster-label span p{background-color:transparent;}#mermaid-diagram-ri95 .label text,#mermaid-diagram-ri95 span{fill:#000000;color:#000000;}#mermaid-diagram-ri95 .node rect,#mermaid-diagram-ri95 .node circle,#mermaid-diagram-ri95 .node ellipse,#mermaid-diagram-ri95 .node polygon,#mermaid-diagram-ri95 .node path{fill:#eee;stroke:#999;stroke-width:1px;}#mermaid-diagram-ri95 .rough-node .label text,#mermaid-diagram-ri95 .node .label text{text-anchor:middle;}#mermaid-diagram-ri95 .node .katex path{fill:#000;stroke:#000;stroke-width:1px;}#mermaid-diagram-ri95 .node .label{text-align:center;}#mermaid-diagram-ri95 .node.clickable{cursor:pointer;}#mermaid-diagram-ri95 .arrowheadPath{fill:#333333;}#mermaid-diagram-ri95 .edgePath .path{stroke:#666;stroke-width:2.0px;}#mermaid-diagram-ri95 .flowchart-link{stroke:#666;fill:none;}#mermaid-diagram-ri95 .edgeLabel{background-color:white;text-align:center;}#mermaid-diagram-ri95 .edgeLabel p{background-color:white;}#mermaid-diagram-ri95 .edgeLabel rect{opacity:0.5;background-color:white;fill:white;}#mermaid-diagram-ri95 .labelBkg{background-color:rgba(255, 255, 255, 0.5);}#mermaid-diagram-ri95 .cluster rect{fill:hsl(0, 0%, 98.9215686275%);stroke:#707070;stroke-width:1px;}#mermaid-diagram-ri95 .cluster text{fill:#333;}#mermaid-diagram-ri95 .cluster span{color:#333;}#mermaid-diagram-ri95 div.mermaidTooltip{position:absolute;text-align:center;max-width:200px;padding:2px;font-family:var(--font-geist-sans);font-size:12px;background:hsl(-160, 0%, 93.3333333333%);border:1px solid #707070;border-radius:2px;pointer-events:none;z-index:100;}#mermaid-diagram-ri95 .flowchartTitleText{text-anchor:middle;font-size:18px;fill:#000000;}#mermaid-diagram-ri95 .flowchart-link{stroke:hsl(var(--gray-400));stroke-width:1px;}#mermaid-diagram-ri95 .marker,#mermaid-diagram-ri95 marker,#mermaid-diagram-ri95 marker *{fill:hsl(var(--gray-400))!important;stroke:hsl(var(--gray-400))!important;}#mermaid-diagram-ri95 .label,#mermaid-diagram-ri95 text,#mermaid-diagram-ri95 text>tspan{fill:hsl(var(--black))!important;color:hsl(var(--black))!important;}#mermaid-diagram-ri95 .background,#mermaid-diagram-ri95 rect.relationshipLabelBox{fill:hsl(var(--white))!important;}#mermaid-diagram-ri95 .entityBox,#mermaid-diagram-ri95 .attributeBoxEven{fill:hsl(var(--gray-150))!important;}#mermaid-diagram-ri95 .attributeBoxOdd{fill:hsl(var(--white))!important;}#mermaid-diagram-ri95 .label-container,#mermaid-diagram-ri95 rect.actor{fill:hsl(var(--white))!important;stroke:hsl(var(--gray-400))!important;}#mermaid-diagram-ri95 line{stroke:hsl(var(--gray-400))!important;}#mermaid-diagram-ri95 :root{--mermaid-font-family:var(--font-geist-sans);}ClientControllersServicesRepositoriesPostgreSQLAWS S3Security LayerException Handler
```

## 2. Setup and Configuration

### 2.1 Required Environment Variables

```plaintext
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
```

### 2.2 Database Schema

```mermaid
Diagram.download-icon {
            cursor: pointer;
            transform-origin: center;
        }
        .download-icon .arrow-part {
            transition: transform 0.35s cubic-bezier(0.35, 0.2, 0.14, 0.95);
             transform-origin: center;
        }
        button:has(.download-icon):hover .download-icon .arrow-part, button:has(.download-icon):focus-visible .download-icon .arrow-part {
          transform: translateY(-1.5px);
        }
        #mermaid-diagram-ri9j{font-family:var(--font-geist-sans);font-size:12px;fill:#000000;}#mermaid-diagram-ri9j .error-icon{fill:#552222;}#mermaid-diagram-ri9j .error-text{fill:#552222;stroke:#552222;}#mermaid-diagram-ri9j .edge-thickness-normal{stroke-width:1px;}#mermaid-diagram-ri9j .edge-thickness-thick{stroke-width:3.5px;}#mermaid-diagram-ri9j .edge-pattern-solid{stroke-dasharray:0;}#mermaid-diagram-ri9j .edge-thickness-invisible{stroke-width:0;fill:none;}#mermaid-diagram-ri9j .edge-pattern-dashed{stroke-dasharray:3;}#mermaid-diagram-ri9j .edge-pattern-dotted{stroke-dasharray:2;}#mermaid-diagram-ri9j .marker{fill:#666;stroke:#666;}#mermaid-diagram-ri9j .marker.cross{stroke:#666;}#mermaid-diagram-ri9j svg{font-family:var(--font-geist-sans);font-size:12px;}#mermaid-diagram-ri9j p{margin:0;}#mermaid-diagram-ri9j .entityBox{fill:#eee;stroke:#999;}#mermaid-diagram-ri9j .attributeBoxOdd{fill:#ffffff;stroke:#999;}#mermaid-diagram-ri9j .attributeBoxEven{fill:#f2f2f2;stroke:#999;}#mermaid-diagram-ri9j .relationshipLabelBox{fill:hsl(-160, 0%, 93.3333333333%);opacity:0.7;background-color:hsl(-160, 0%, 93.3333333333%);}#mermaid-diagram-ri9j .relationshipLabelBox rect{opacity:0.5;}#mermaid-diagram-ri9j .relationshipLine{stroke:#666;}#mermaid-diagram-ri9j .entityTitleText{text-anchor:middle;font-size:18px;fill:#000000;}#mermaid-diagram-ri9j #MD_PARENT_START{fill:#f5f5f5!important;stroke:#666!important;stroke-width:1;}#mermaid-diagram-ri9j #MD_PARENT_END{fill:#f5f5f5!important;stroke:#666!important;stroke-width:1;}#mermaid-diagram-ri9j .flowchart-link{stroke:hsl(var(--gray-400));stroke-width:1px;}#mermaid-diagram-ri9j .marker,#mermaid-diagram-ri9j marker,#mermaid-diagram-ri9j marker *{fill:hsl(var(--gray-400))!important;stroke:hsl(var(--gray-400))!important;}#mermaid-diagram-ri9j .label,#mermaid-diagram-ri9j text,#mermaid-diagram-ri9j text>tspan{fill:hsl(var(--black))!important;color:hsl(var(--black))!important;}#mermaid-diagram-ri9j .background,#mermaid-diagram-ri9j rect.relationshipLabelBox{fill:hsl(var(--white))!important;}#mermaid-diagram-ri9j .entityBox,#mermaid-diagram-ri9j .attributeBoxEven{fill:hsl(var(--gray-150))!important;}#mermaid-diagram-ri9j .attributeBoxOdd{fill:hsl(var(--white))!important;}#mermaid-diagram-ri9j .label-container,#mermaid-diagram-ri9j rect.actor{fill:hsl(var(--white))!important;stroke:hsl(var(--gray-400))!important;}#mermaid-diagram-ri9j line{stroke:hsl(var(--gray-400))!important;}#mermaid-diagram-ri9j :root{--mermaid-font-family:var(--font-geist-sans);}UserTweetFollowTweetLikecreatesfollowslikeshasreferences
```

## 3. API Endpoints

### 3.1 Authentication

#### Register

```plaintext
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
```

#### Login

```plaintext
POST /auth/login
Content-Type: application/json

{
    "email": "string",
    "password": "string"
}
```

### 3.2 Tweet Operations

#### Create Tweet

```plaintext
POST /tweet
Content-Type: multipart/form-data

content: string
media: file (optional)
```

#### Reply to Tweet

```plaintext
POST /tweet/{tweetId}/reply
Content-Type: multipart/form-data

content: string
media: file (optional)
```

#### Retweet

```plaintext
POST /tweet/{tweetId}/retweet
```

### 3.3 User Operations

#### Update Profile Image

```plaintext
PUT /user/profile-image
Content-Type: multipart/form-data

file: image
```

#### Follow User

```plaintext
POST /follow/{followingId}
```

## 4. Data Models

### 4.1 User Entity

```java
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
```

### 4.2 Tweet Entity

```java
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
```

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


### 9.2 Test Data

```java
@Test
public void testCreateTweet() {
    // Test scenarios
}
```

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