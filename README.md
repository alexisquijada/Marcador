# Marcador de Pádel v2.8 — SOLO 8BitDo SN30

Proyecto **Android Java** para la Nexus 10 con Android 10. Se compila con GitHub Actions, sin Android Studio. El ZIP es **código fuente**, no un APK compilado.

## Controles por defecto

| Botón | Acción |
| --- | --- |
| **SELECT** | Cambiar equipo seleccionado (rojo/azul). No cambia el saque. |
| **X** | **Sumar** un punto al equipo seleccionado. |
| **B** | **Restar** el último punto registrado del equipo seleccionado. |
| **Y** | Bloquear/desbloquear las pulsaciones del SN30. |
| **A** | Sin acción asignada. Disponible para futuras funciones. |

Al cambiar equipo, se ilumina un indicador junto al puntaje elegido y aparece el texto **SELECCIONADO: ROJO/AZUL**. Esto es independiente de la pelota que indica el saque.

### Qué significa restar

**B corrige el último punto que se registró para el equipo seleccionado** (no necesariamente la última jugada del partido). Se utiliza el historial de puntos del marcador, se retira ese punto y se vuelven a aplicar los puntos posteriores del otro equipo. Esto también puede afectar un juego o set si el punto corregido lo había decidido. Si no hay ningún punto del equipo en el historial, no resta ni altera resultados. No es una resta aritmética de 15 puntos ni permite puntuaciones negativas. El historial guarda hasta 200 jugadas; al retroceder más allá de ellas no hay garantía de poder restar puntos anteriores.

Se conserva el botón táctil **Deshacer punto** del menú, para deshacer la última jugada sin importar el equipo. También se mantiene la pulsación prolongada sobre cada marcador para restar al equipo correspondiente.

### Seguridad y preferencias

- La aplicación **solo** acepta eventos HID (`KeyEvent`) provenientes de un control cuyo nombre contenga `8BitDo` y `SN30`. Sin FitPro, sin BLE propietario, sin permisos Bluetooth en la APK.
- Para comenzar, empareja desde Ajustes de Android → Bluetooth y activa el control desde el menú del marcador (mantén pulsada la **hora**). Cada inicio de app deja el mando desactivado por seguridad.
- **Y** alterna bloqueado/desbloqueado mientras el control está activo. Bloqueado ignora SELECT, X y B.
- Las cuatro acciones pueden reasignarse desde el menú. La versión 2.8 usa una clave nueva para sus asignaciones, así no hereda una asignación de v2.7 que haría que X/B ejecuten otras funciones.
- El marcador sigue guardando el partido localmente en Android.
- `KeyEvent.ACTION_DOWN` sin repeticiones evita sumar/restar repetidamente si dejas el botón apretado.

## Compilar con GitHub Actions

1. Descomprime el ZIP y copia **todo su contenido** a la raíz de tu repositorio GitHub `Marcador` (rama `main`). Actualiza o sustituye los archivos de versiones anteriores.
2. GitHub → **Actions → Compilar Marcador Padel SN30 → Run workflow** (o haz push a `main`).
3. Descarga el artefacto **MarcadorPadel-SN30-SELECT-RESTA-v2-8**. Dentro viene `app-debug.apk`.
4. En PowerShell, desde la carpeta donde descargaste el APK:

```powershell
cd C:\platform-tools
.\adb devices -l
.\adb install -r .\app-debug.apk
```

El `applicationId` es `cl.marcadorpadel.sn30`, igual que en v2.7. Si cambió la firma de compilación, `INSTALL_FAILED_UPDATE_INCOMPATIBLE` puede requerir desinstalar; haz respaldo de los datos antes de hacerlo porque los partidos se guardan localmente.

## Verificación pendiente en hardware

En tu Nexus 10 ya se comprobó el emparejamiento y la llegada de señales distintas del 8BitDo SN30 mediante `adb shell getevent -lt`. Aún falta validar la traducción real de SELECT/X/B/Y a `KeyEvent` de Android y probar esta v2.8 instalada. Para comprobarlo: abre el menú → **Último botón** o usa `adb logcat -s PadelSN30:D`. Si alguna tecla aparece distinta, reasígnala en el menú.

## Estado de compilación

El ZIP incluye workflow de compilación con Java 17, SDK 35, Gradle 8.9 y Android Gradle Plugin 8.7.3. El APK final se obtiene desde GitHub Actions.
