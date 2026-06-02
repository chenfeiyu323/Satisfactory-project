# Satisfactory Project

Monorepo for the Satisfactory factory designer.

## Layout

- `satisfactory-factory-designer-backend` - Spring Boot + MySQL API
- `satisfactory-factory-designer-frontend-angular-canvas` - Angular frontend

## Configuration

Copy [.env.example](.env.example) to `.env` if you want to override the default local values for Docker Compose or cloud deployment.

## One-click local startup (no local MySQL install)

Your friend only needs Docker Desktop and this repository.

1. Clone the repository.
2. Double-click [StartSatisfactory.exe](StartSatisfactory.exe).

What the EXE does:

- Creates `.env` from [.env.example](.env.example) when missing.
- Starts `mysql + backend + frontend` through Docker Compose.
- Opens `http://localhost:4200` automatically.

Stop all services with [stop-local.bat](stop-local.bat).

If you need to rebuild the EXE locally, run [launcher/build-launcher.bat](launcher/build-launcher.bat).

## Desktop package for friends with no environment

If you want the real zero-environment desktop version, build it with [build-desktop.ps1](build-desktop.ps1).

The desktop package uses:

- H2 local file database instead of MySQL
- Angular static files bundled into the Spring Boot app
- automatic browser launch on startup
- `shared/satisfactory-save.json` for sync/import

The generated app image is placed under `release/SatisfactoryFactoryDesigner/` and can be zipped and sent to your friend.

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

## Save-file sync between friends

Use the shared save file at `shared/satisfactory-save.json`:

1. On PC A: run [export-save.bat](export-save.bat) to export latest local data.
2. Send `shared/satisfactory-save.json` to PC B (or overwrite the same file in git-ignored `shared` folder).
3. On PC B: restart via [StartSatisfactory.exe](StartSatisfactory.exe).

The backend will auto-import this save file on startup and overwrite local factory data (catalog seed data remains untouched).

You can also import manually using [import-save.bat](import-save.bat).

## Cloud deployment

The backend and frontend both have Dockerfiles, so you can deploy them as containers.

- Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` for the backend.
- Set `APP_CORS_ALLOWED_ORIGINS` to the public frontend origin.
- Build the frontend with the production config and serve it behind a reverse proxy or the included Nginx container.
- For container deployment, start from the values in [.env.example](.env.example) and override only what the cloud provider requires.

If you deploy both containers together through the root `docker-compose.yml`, the frontend proxy already forwards `/api` to the backend service.
