# FitPro BLE - Receptor de diagnóstico para Nexus 10

**Estado: proyecto fuente para Android; no incluye una APK ya compilada.**

Aplicación de diagnóstico BLE nativa, completamente independiente de FitPro y del marcador de pádel v2.4.
No usa Kotlin, NDK, AndroidX ni bibliotecas de terceros. Compatible por diseño con la
Nexus 10 con Android 10 (ARM32): está escrita en Java y no incluye bibliotecas nativas.

## Qué hace

- Lista dispositivos Bluetooth emparejados y busca relojes BLE cercanos.
- Permite seleccionar un dispositivo y conectarse por GATT.
- Descubre Nordic UART Service (`6e400001-...`) y habilita notificaciones.
- Envía `CD 00 06 12 01 0C 00 01 01` para activar cámara remota.
- Detecta `CD 00 05 1C 01 02 00 00` y responde con `DC 00 05 1C 01 00 08 01`.
- Cuenta **notificaciones**, **mensajes cámara**, **mensajes cámara aceptados por filtro** y **otros**.
- Permite modificar ventana anti-repetición: 500, 1000, 2000, 3500 o 5000 ms.
- Muestra y conserva registro HEX con horas exactas, y ofrece copiarlo.
- No accede a Internet, no incorpora analítica ni suma puntos.

**AVISO IMPORTANTE:** En un registro anterior hubo 62 mensajes de cámara con solo cinco pulsaciones
físicas previstas. Aplicar un filtro de 3500 ms deja 11 eventos, no cinco; por tanto no es una
solución de puntuación validada. Es necesario determinar si estos mensajes se generan sin tocar
el reloj y si la reconexión funciona sin FitPro.

## Compilación en Windows (con Android SDK de Android Studio)

1. Instala Android Studio si todavía no lo tienes.
2. Abre **Tools > SDK Manager** y descarga Android SDK **API 35**.
3. Extrae este ZIP en una carpeta, abre PowerShell dentro de ella y ejecuta:

```powershell
Set-ExecutionPolicy -Scope Process Bypass
.\build-windows.ps1
```

El script descarga Gradle 8.7 desde su sitio oficial si no está en la carpeta de trabajo.
Requiere Internet, Android SDK API 35 y Java 17 o superior. Genera
`fitpro-ble-diagnostico.apk` en la raíz. El código también puede abrirse y editarse
en Android Studio; el proyecto se compila mediante el script incluido, sin wrapper Gradle.

## Instalar en la Nexus 10

Conecta la tablet por USB. En tu carpeta `C:\platform-tools`, ejecuta:

```powershell
.\adb shell am force-stop cn.xiaofengkj.fitpro
.\adb install -r "C:\RUTA\fitpro-ble-diagnostico.apk"
```

Al abrir la aplicación:

1. En Android 10, concede permiso de Ubicación y enciende el interruptor **Ubicación** del sistema
   (Android lo exige para descubrir BLE; la app no lee ni almacena coordenadas).
2. Activa Bluetooth.
3. Pulsa **Buscar relojes**; espera 12 segundos, luego elige el dispositivo **WATCH**
   o el nombre que aparezca para tu reloj.
4. La app se conecta, habilita notificaciones y envía el comando de cámara automáticamente.
5. Deja el reloj sin tocar unos 15-20 segundos; anota los números.
6. Pulsa cinco veces, con separación clara entre pulsaciones. Compara los contadores.
7. Pulsa **Copiar registro** y pega el texto en un archivo, o extráelo mediante ADB.

Para leer el registro desde Windows:

```powershell
.\adb shell run-as cl.padel.fitprodiag cat files/ble_log.txt > "$env:USERPROFILE\Downloads\fitpro_ble_registro.txt"
```

`run-as` requiere instalación `debug` (la compilación indicada usa debug).

## Problemas conocidos / alcance

- FitPro debe detenerse antes de probar; dos aplicaciones no deben competir por la sesión GATT.
- El reloj podría exigir comandos adicionales de inicialización, dependiendo de la versión de firmware.
- El botón de la cámara podría enviar mensajes repetidos incluso sin tocarlo: el filtro es diagnóstico.
- Si el reloj está conectado a FitPro, forzar cierre y volver a intentar desde esta aplicación.
- **No se ha compilado ni probado físicamente** este proyecto en la Nexus 10 en este entorno.
- No necesita privilegios root.

## Protocolo observado en HCI

Servicio: `6e400001-b5a3-f393-e0a9-e50e24dcca9d` (Nordic UART)

- Escritura: `6e400002-b5a3-f393-e0a9-e50e24dcca9d` (handle 0x003E en la captura).
- Notificaciones: `6e400003-b5a3-f393-e0a9-e50e24dcca9d` (handle 0x0041 en la captura).
- Activar cámara: `CD 00 06 12 01 0C 00 01 01`.
- Desactivar cámara: `CD 00 06 12 01 0C 00 01 00`.
- Aviso de cámara: `CD 00 05 1C 01 02 00 00`.
- ACK: `DC 00 05 1C 01 00 08 01`.

Los handles pueden variar con el dispositivo o la sesión; por eso la app usa UUID en lugar de handles fijos.
