# Satisfactory Project

Monorepo for the Satisfactory factory designer.

## Layout

- `satisfactory-factory-designer-backend` - Spring Boot + MySQL API
- `satisfactory-factory-designer-frontend-angular-canvas` - Angular frontend

## Configuration

Copy [.env.example](.env.example) to `.env` if you want to override the default local values for Docker Compose or cloud deployment.

## Local development

1. Start MySQL and the backend stack:

```bash
docker compose up -d mysql backend
```

2. Run the Angular frontend locally:

```bash
cd satisfactory-factory-designer-frontend-angular-canvas
npm install
npm run dev
```

The backend defaults to `http://localhost:8080/api`, and the frontend dev server uses that by default.

## Cloud deployment

The backend and frontend both have Dockerfiles, so you can deploy them as containers.

- Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` for the backend.
- Set `APP_CORS_ALLOWED_ORIGINS` to the public frontend origin.
- Build the frontend with the production config and serve it behind a reverse proxy or the included Nginx container.
- For container deployment, start from the values in [.env.example](.env.example) and override only what the cloud provider requires.

If you deploy both containers together through the root `docker-compose.yml`, the frontend proxy already forwards `/api` to the backend service.
