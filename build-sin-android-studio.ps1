# Compila el receptor BLE sin Android Studio (requiere Java JDK 17+).
# Descargar previamente Command-line Tools para Windows desde:
# https://developer.android.com/studio#command-line-tools-only
# Guardar el ZIP commandlinetools-win-15859902_latest.zip en Descargas.
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root
if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    throw 'No se detecta Java. Instala JDK 17 o superior y abre otra consola PowerShell.'
}
java -version
$sdk = Join-Path $env:LOCALAPPDATA 'Android\Sdk'
$manager = Join-Path $sdk 'cmdline-tools\latest\bin\sdkmanager.bat'
if (-not (Test-Path $manager)) {
    $zip = Join-Path $env:USERPROFILE 'Downloads\commandlinetools-win-15859902_latest.zip'
    if (-not (Test-Path $zip)) {
        throw "Descarga Command-line Tools para Windows desde developer.android.com/studio y deja el ZIP en $zip"
    }
    $temp = Join-Path $env:TEMP 'fitpro-commandlinetools-extract'
    if (Test-Path $temp) { Remove-Item -Recurse -Force $temp }
    Expand-Archive -LiteralPath $zip -DestinationPath $temp
    $src = Join-Path $temp 'cmdline-tools'
    if (-not (Test-Path (Join-Path $src 'bin\sdkmanager.bat'))) {
        throw 'El ZIP no contiene cmdline-tools/bin/sdkmanager.bat en la estructura esperada.'
    }
    $target = Join-Path $sdk 'cmdline-tools\latest'
    New-Item -ItemType Directory -Force -Path $target | Out-Null
    Copy-Item -Path (Join-Path $src '*') -Destination $target -Recurse -Force
    Remove-Item -Recurse -Force $temp
}
$env:ANDROID_HOME = $sdk
$env:ANDROID_SDK_ROOT = $sdk
Write-Host 'Acepta las licencias de Android SDK cuando se solicite (y/n).'
& $manager "--sdk_root=$sdk" --licenses
if ($LASTEXITCODE -ne 0) { throw 'No se pudieron aceptar las licencias del SDK' }
& $manager "--sdk_root=$sdk" 'platforms;android-35' 'build-tools;35.0.0' 'platform-tools'
if ($LASTEXITCODE -ne 0) { throw 'Fallo la instalacion de paquetes Android SDK' }
& (Join-Path $root 'build-windows.ps1')
if ($LASTEXITCODE -and $LASTEXITCODE -ne 0) { throw 'Fallo la compilacion' }
