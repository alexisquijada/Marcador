# Compilacion local del receptor FitPro BLE en Windows.
# Requiere Android SDK (plataforma 35 + build tools) y Java 17+.
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

if (-not $env:ANDROID_HOME) {
    $candidate = Join-Path $env:LOCALAPPDATA 'Android\Sdk'
    if (Test-Path $candidate) { $env:ANDROID_HOME = $candidate }
}
if (-not $env:ANDROID_HOME -or -not (Test-Path (Join-Path $env:ANDROID_HOME 'platforms\android-35\android.jar'))) {
    throw "Falta Android SDK plataforma 35. Instalalo en Android Studio > SDK Manager."
}
if (-not $env:JAVA_HOME) {
    $jbr = 'C:\Program Files\Android\Android Studio\jbr'
    if (Test-Path (Join-Path $jbr 'bin\java.exe')) { $env:JAVA_HOME = $jbr }
}
if ($env:JAVA_HOME) { $env:Path = (Join-Path $env:JAVA_HOME 'bin') + ';' + $env:Path }

$gradleHome = Join-Path $root '.gradle-bootstrap\gradle-8.7'
$gradleExe = Join-Path $gradleHome 'bin\gradle.bat'
if (-not (Test-Path $gradleExe)) {
    $cache = Join-Path $root '.gradle-bootstrap'
    New-Item -ItemType Directory -Force -Path $cache | Out-Null
    $zip = Join-Path $cache 'gradle-8.7-bin.zip'
    Write-Host 'Descargando Gradle 8.7 desde services.gradle.org ...'
    Invoke-WebRequest 'https://services.gradle.org/distributions/gradle-8.7-bin.zip' -OutFile $zip
    Expand-Archive -LiteralPath $zip -DestinationPath $cache -Force
    Remove-Item $zip
}

& $gradleExe --no-daemon ':app:assembleDebug'
if ($LASTEXITCODE -ne 0) { throw "Fallo Gradle (codigo $LASTEXITCODE)" }
$sourceApk = Join-Path $root 'app\build\outputs\apk\debug\app-debug.apk'
if (-not (Test-Path $sourceApk)) { throw 'No se genero app-debug.apk' }
$apk = Join-Path $root 'fitpro-ble-diagnostico.apk'
Copy-Item $sourceApk $apk -Force
Write-Host "APK generada: $apk"
Write-Host 'Para instalar: adb install -r fitpro-ble-diagnostico.apk'
