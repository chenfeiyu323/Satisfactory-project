$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$frontend = Join-Path $root 'satisfactory-factory-designer-frontend-angular-canvas'
$backend = Join-Path $root 'satisfactory-factory-designer-backend'
$buildDir = Join-Path $root 'desktop-build'
$staticDir = Join-Path $backend 'src\main\resources\static'
$releaseDir = Join-Path $root 'release'

function Invoke-BackendBuild {
    if (Get-Command mvn -ErrorAction SilentlyContinue) {
        Push-Location $backend
        mvn -q -DskipTests package -Pdesktop
        Pop-Location
        return
    }

    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        throw 'Neither mvn nor docker is available for backend packaging.'
    }

    docker run --rm `
        --mount "type=bind,source=$root,target=/workspace" `
        -w /workspace/satisfactory-factory-designer-backend `
        maven:3.9.9-eclipse-temurin-17 `
        mvn -q -DskipTests package -Pdesktop
}

if (Test-Path $buildDir) { Remove-Item $buildDir -Recurse -Force }
if (Test-Path $releaseDir) { Remove-Item $releaseDir -Recurse -Force }
if (Test-Path $staticDir) { Remove-Item $staticDir -Recurse -Force }

New-Item -ItemType Directory $buildDir | Out-Null
New-Item -ItemType Directory $releaseDir | Out-Null
New-Item -ItemType Directory $staticDir | Out-Null

try {
    Push-Location $frontend
    npm install
    npm run build
    Pop-Location

    $frontendDist = Join-Path $frontend 'dist\satisfactory-factory-designer-frontend-angular\browser'
    Copy-Item (Join-Path $frontendDist '*') $staticDir -Recurse -Force

    Invoke-BackendBuild

    $jar = Join-Path $backend 'target\satisfactory-factory-designer-backend-0.0.1-SNAPSHOT.jar'
    $jpackage = (Get-Command jpackage).Source

    & $jpackage `
        --type app-image `
        --name SatisfactoryFactoryDesigner `
        --input (Join-Path $backend 'target') `
        --main-jar (Split-Path $jar -Leaf) `
        --main-class com.example.satisfactory.SatisfactoryFactoryDesignerApplication `
        --dest $releaseDir `
        --java-options '-Dspring.profiles.active=desktop' `
        --java-options '-Dfile.encoding=UTF-8'

    Write-Host "Desktop package created in $releaseDir\SatisfactoryFactoryDesigner"
}
finally {
    if (Test-Path $staticDir) { Remove-Item $staticDir -Recurse -Force }
}
