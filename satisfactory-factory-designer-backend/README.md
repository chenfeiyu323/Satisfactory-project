# Satisfactory Factory Designer Backend

Spring Boot + MySQL backend for a Satisfactory multi-factory bus designer.

## Current scope

Implemented according to the current requirement discussion:

- Factory forms: each form represents one factory.
- Factory / bucket / production node `enabled` switches.
- Only enabled factories, buckets, and nodes participate in calculation.
- Bus lines can be created manually or auto-created when enabled nodes use materials.
- Each bus line stores offset input, name, description, visibility, and external-output flags.
- Calculation is real-time and not stored in the database.
- Calculation formula per bus line:

```text
net = local output + external input + offset - local demand
```

- External connection rules:
  - source line must be from another factory
  - source and target material must match
  - source factory must be enabled
  - source line must be visible and marked as external output
  - source net must be positive
  - one source bus line can connect to only one target bus line
  - one target bus line can receive multiple external inputs
  - no manual connection code is required; frontend should use the available-source dropdown API
- Transport advice is calculated for belts and pipes.
- `FactorySnapshot` can save a JSON snapshot of a factory design.
- Factory copy is supported; copied factories start disabled and external flags are cleared.

## Important data note

The seed JSON files included here are **sample official-style data only**, not a complete full Satisfactory official dataset. The backend supports JSON-based seeding, so you can replace these files with full official data later:

```text
src/main/resources/data/seed/materials.json
src/main/resources/data/seed/machines.json
src/main/resources/data/seed/recipes.json
src/main/resources/data/seed/transport_levels.json
```

## Requirements

- Java 17+
- Maven
- MySQL 8+
- Optional: Docker / Docker Compose

## Run with Docker MySQL

```bash
docker compose up -d
```

Then run the backend:

```bash
mvn spring-boot:run
```

Default database config:

```yaml
DB_URL=jdbc:mysql://localhost:3306/satisfactory_factory_designer?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=password
```

## Seed sample data

After the backend starts:

```bash
curl -X POST http://localhost:8080/api/admin/seed/all
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

## Main APIs

### Catalog

```text
GET  /api/materials
GET  /api/machines
GET  /api/recipes
POST /api/admin/seed/all
```

### Factories

```text
GET    /api/factories
POST   /api/factories
GET    /api/factories/{id}
PATCH  /api/factories/{id}
DELETE /api/factories/{id}
POST   /api/factories/{id}/copy
GET    /api/factories/{id}/calculation
POST   /api/factories/{id}/snapshots
GET    /api/factories/{id}/snapshots
```

### Buckets and production nodes

```text
GET    /api/factories/{factoryId}/buckets
POST   /api/factories/{factoryId}/buckets
PATCH  /api/buckets/{bucketId}
DELETE /api/buckets/{bucketId}

GET    /api/buckets/{bucketId}/nodes
POST   /api/buckets/{bucketId}/nodes
PATCH  /api/nodes/{nodeId}
DELETE /api/nodes/{nodeId}
```

### Bus lines and external connections

```text
GET    /api/factories/{factoryId}/bus-lines
POST   /api/factories/{factoryId}/bus-lines
PATCH  /api/bus-lines/{busLineId}
DELETE /api/bus-lines/{busLineId}

GET    /api/bus-lines/{targetBusLineId}/available-external-sources
POST   /api/external-connections
GET    /api/factories/{factoryId}/external-connections
DELETE /api/external-connections/{connectionId}
```

## Example quick flow

1. Seed catalog data.
2. Create a steel-pipe sub factory.
3. Create a bucket and node that outputs steel pipe.
4. Enable the bucket and node.
5. Mark the steel pipe bus line as visible and external.
6. Create a main factory.
7. Create a steel pipe bus line in the main factory.
8. Query available external sources for the main factory steel pipe line.
9. Bind the source line to the target line.
10. Query `/api/factories/{id}/calculation` to see external input and net value.

## Example requests

Create factory:

```bash
curl -X POST http://localhost:8080/api/factories \
  -H "Content-Type: application/json" \
  -d '{"name":"Main Bus","factoryType":"MAIN","enabled":true,"maxBeltLevel":3,"maxPipeLevel":1}'
```

Create bus line manually:

```bash
curl -X POST http://localhost:8080/api/factories/1/bus-lines \
  -H "Content-Type: application/json" \
  -d '{"materialId":12,"name":"Steel Pipe","offsetAmount":0,"createdManually":true}'
```

Create bucket:

```bash
curl -X POST http://localhost:8080/api/factories/1/buckets \
  -H "Content-Type: application/json" \
  -d '{"name":"Motor Bucket","enabled":true}'
```

Add node:

```bash
curl -X POST http://localhost:8080/api/buckets/1/nodes \
  -H "Content-Type: application/json" \
  -d '{"recipeId":12,"enabled":true,"machineCount":3,"clockPercent":100,"outputMultiplier":1}'
```

Calculate:

```bash
curl http://localhost:8080/api/factories/1/calculation
```

## Seed data update note

The four seed files are in:

- `src/main/resources/data/seed/materials.json`
- `src/main/resources/data/seed/machines.json`
- `src/main/resources/data/seed/recipes.json`
- `src/main/resources/data/seed/transport_levels.json`

This package includes an expanded curated seed set for immediate testing. For an exact full official data refresh, use the game's own `Docs.json` file:

```powershell
python scripts/convert_docs_json_to_seed.py "C:\Program Files (x86)\Steam\steamapps\common\Satisfactory\CommunityResources\Docs\Docs.json" src\main\resources\data\seed --game-version 1.0
```

Then restart the backend and run:

```powershell
curl.exe -X POST http://localhost:8080/api/admin/seed/all
```

If old seed rows already exist in MySQL, the seeder updates rows by `gameKey`. For a completely clean import during development, drop and recreate the database, or truncate the recipe/material tables before reseeding.

## Import full official Satisfactory data from Docs.json

The bundled seed files are only a starter dataset. For a full material and recipe catalog, import the `Docs.json` that ships with your own game installation. `Docs.json` is the most reliable source because it is generated from the game data and includes items, fluids, buildings and recipes.

Typical Steam path:

```text
C:\Program Files (x86)\Steam\steamapps\common\Satisfactory\CommunityResources\Docs\Docs.json
```

Typical Epic path varies by install folder, but look under:

```text
...\Satisfactory\CommunityResources\Docs\Docs.json
```

After the backend is running, import it with PowerShell:

```powershell
curl.exe -X POST "http://localhost:8080/api/admin/seed/docs-json?gameVersion=local-docs" `
  -F "file=@C:\Program Files (x86)\Steam\steamapps\common\Satisfactory\CommunityResources\Docs\Docs.json"
```

Then refresh the Angular frontend. The material dropdowns and recipe dropdowns will be populated from the imported catalog.

Alternative: generate seed JSON files from Docs.json:

```powershell
python scripts/convert_docs_json_to_seed.py "C:\Program Files (x86)\Steam\steamapps\common\Satisfactory\CommunityResources\Docs\Docs.json" src/main/resources/data/seed --game-version local-docs
curl.exe -X POST http://localhost:8080/api/admin/seed/all
```
