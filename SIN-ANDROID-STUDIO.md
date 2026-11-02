# Compilar FitPro BLE sin Android Studio

## Opcion A: solo navegador (GitHub Actions)

1. Crea un repositorio nuevo y **privado** en https://github.com/new (rama `main`).
2. Extrae este ZIP. En GitHub, usa `Add file > Upload files` para subir **el contenido** de la carpeta extraida, manteniendo la estructura `app/...` y `.github/workflows/compilar-apk.yml`.
3. En el repositorio ve a `Actions > Compilar FitPro BLE APK > Run workflow`. Tambien se lanza con un push a `main`.
4. Al terminar el flujo con estado verde, abre su ejecucion y en `Artifacts` descarga `fitpro-ble-diagnostico`.
5. Extrae el archivo descargado: contendra `app-debug.apk`.
6. Conecta la Nexus 10 por USB y desde `C:\platform-tools` ejecuta: `./adb install -r "RUTA\app-debug.apk"` (en PowerShell usa `.\adb`).

El APK es de **depuracion**, firmado automaticamente para pruebas; no es una compilacion de produccion. El flujo prepara Java 17, Android SDK 35 y Gradle 8.7 en GitHub.

## Opcion B: Windows sin Android Studio

1. Instala Java JDK 17 o superior. Por ejemplo Temurin JDK 17 desde https://adoptium.net/.
2. Descarga *Command line tools only* para Windows desde https://developer.android.com/studio#command-line-tools-only. El ZIP esperado es `commandlinetools-win-15859902_latest.zip`.
3. Guarda el ZIP sin descomprimir en `Descargas`.
4. Abre PowerShell en la carpeta de este proyecto, y ejecuta:

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\build-sin-android-studio.ps1
```

El script instala el SDK de manera local, solicita la aceptacion de licencias, descarga SDK Platform 35 y Build Tools 35.0.0, y usa el compilador Gradle del proyecto. Si termina bien, genera `fitpro-ble-diagnostico.apk`.

## Uso

Forzar cierre de FitPro antes de conectar el reloj desde nuestra app:

```powershell
.\adb shell am force-stop cn.xiaofengkj.fitpro
.\adb install -r "RUTA\fitpro-ble-diagnostico.apk"
```

No se ha validado aun el comportamiento del reloj sin FitPro; esta es una APK de diagnostico que registra eventos, no un marcador definitivo.
