# Satisfactory Project

Monorepo for the Satisfactory factory designer.

## Layout

- `satisfactory-factory-designer-backend` - Spring Boot backend
- `satisfactory-factory-designer-frontend-angular-canvas` - Angular frontend

## Desktop distributable (recommended)

This repository provides a self-contained desktop package that requires no Docker or MySQL on the target machine.

- Build the desktop image with `build-desktop.ps1`.
- The generated app image is placed under `release/SatisfactoryFactoryDesigner/`.
- To run the desktop app, double-click:

- [release/SatisfactoryFactoryDesigner/SatisfactoryFactoryDesigner.exe](release/SatisfactoryFactoryDesigner/SatisfactoryFactoryDesigner.exe)

The desktop package runs an embedded JVM and the Spring Boot backend (uses H2 file DB), serves the Angular UI, and attempts to open your browser automatically.

To stop the desktop app, close the browser window and exit the application process (Task Manager if needed).

## Local development

If you want to develop locally, run the backend and frontend separately:

1. Backend: use the IDE or

```bash
cd satisfactory-factory-designer-backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

2. Frontend: run the Angular dev server:

```bash
cd satisfactory-factory-designer-frontend-angular-canvas
npm install
npm run dev
```

The backend defaults to `http://localhost:8080/api` and the frontend dev server talks to that by default.

## Notes

- The repository previously included Docker-based launchers and helper scripts; this checkout is desktop-first. If you upgraded from earlier versions, you can delete `StartSatisfactory.exe`, `stop-local.bat`, and `docker-compose.yml` files — they are not required for the desktop package.
- Use the `shared/` folder to exchange `satisfactory-save.json` for manual import/export when needed.
