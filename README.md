# file-management-system

Spring Boot + SQLite file management system with administrator-approved registration.
Frontend: **Thymeleaf** (server-rendered pages). A JSON REST API is also available for Postman.

## Run
Requirements: JDK 17+, Maven 3.9+

    cd backend
    mvn spring-boot:run

Open http://localhost:8080

Default administrator (created on first start, see `application.properties`):

    admin@example.com / Admin@12345     <-- change this

## Workflow
1. A user registers -> status `PENDING` (cannot log in).
2. Admin logs in -> "Pending approvals" -> Approve / Reject.
3. Approved users log in, create folders, upload, preview, download, delete files.

## Structure
    backend/src/main/java/com/example/filesystem/
        config/ controller/ entity/ repository/ service/ dto/ exception/
    backend/src/main/resources/templates/  Thymeleaf pages (login, register, files, preview, admin, fragments)
    backend/src/main/resources/static/css/ stylesheet
    backend/data/filesystem.db           SQLite database (users, folders, file metadata)
    backend/uploads/                     uploaded file contents (never served statically)

## Web pages (Thymeleaf)
| URL | Page |
|---|---|
| /login, /register | Log in / create account |
| /files?folderId= | Folder and file list, create folder, upload, delete |
| /files/{id}/view | Preview (PDF, image, text) |
| /admin/users | Admin: approve / reject pending users |

## REST API
| Method | Endpoint | Purpose |
|---|---|---|
| POST | /api/auth/register | Register (PENDING) |
| POST | /api/auth/login | Log in (APPROVED only) |
| POST | /api/auth/logout, GET /api/auth/me | Session |
| GET | /api/admin/users/pending | List pending users (ADMIN) |
| PATCH | /api/admin/users/{id}/approve, /reject | Decide (ADMIN) |
| GET | /api/folders?parentId= | List folders + files |
| POST | /api/folders | Create folder `{name, parentId}` |
| DELETE | /api/folders/{id} | Delete folder recursively |
| POST | /api/files/upload | Multipart `file`, optional `folderId` |
| GET | /api/files/{id}/preview | Inline PDF / image / text |
| GET | /api/files/{id}/download | Download |
| DELETE | /api/files/{id} | Delete |

## Security notes
- Passwords stored with BCrypt; backend rejects PENDING/REJECTED logins.
- Files stored under random names in `uploads/`, served only through authenticated endpoints; owner or ADMIN only.
- Text previews are served as `text/plain`; HTML is never rendered.
- CSRF protection is enabled for the Thymeleaf forms (the `/api/**` JSON API is exempt for Postman use).
- Before production: change the admin password, use HTTPS, use DB migrations (Flyway) instead of `ddl-auto=update`, and back up `data/` and `uploads/`.
