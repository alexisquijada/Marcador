# Receptor BLE FitPro: prueba de controles de música (v2)

## Verificado en la APK original FitPro 2.9.75

Al inspeccionar la rutina `BaseReceiveData.DeviceControlApp(B)` en el archivo
`classes2.dex`, la tabla de salto relaciona las claves:

- 10 (`0x0A`) con `MusicControl.lastMusic()`.
- 11 (`0x0B`) con `MusicControl.playOrPause()`.
- 12 (`0x0C`) con `MusicControl.nexMusic()`.

En capturas BLE reales previas, la orden de cámara usó
`CD 00 05 1C 01 02 00 00`. Por eso la versión 2 interpreta las notificaciones
`CD ?? ?? 1C 01 AA ...` tomando `AA` como posible código de control.
**No se ha confirmado todavía que el modelo WATCH emita música en ese formato.**

## Qué probar en la Nexus 10

1. Actualiza la APK (el paquete es el mismo: `cl.padel.fitprodiag`).
2. Abre la app, busca WATCH y conecta.
3. Pulsa **Probar Música**: manda CAMERA_OFF y no envía ningún comando de música no verificado.
4. En el reloj abre **Música** y pulsa **Anterior** 5 veces, **Pausa/Play**
   5 veces y **Siguiente** 5 veces. Deja al menos 2 segundos entre pulsaciones.
5. Revisa los contadores **Música: anterior / pausa / siguiente**. Puedes
   ver los mensajes `[MUSIC_PREVIOUS]`, `[MUSIC_PLAY_PAUSE]` o `[MUSIC_NEXT]`.
6. Si no aparecen, conserva todos los `RX` para investigar otros formatos o
   la necesidad de inicializar el modo música desde la aplicación.

Los eventos de música quedan **sin ACK automático** porque aún no se ha
verificado cuál corresponde a cada acción. La recepción **no modifica el marcador**.

## Extraer registro con ADB (PowerShell)

```powershell
cd C:\platform-tools
.\adb root
.\adb pull /data/data/cl.padel.fitprodiag/files/ble_log.txt "$env:USERPROFILE\Downloads\fitpro_ble_musica.txt"
```

## Actualizar repo GitHub

En el proyecto local que ya compiló GitHub, sustituye
`app/src/main/java/cl/padel/fitprodiag/MainActivity.java`, `app/build.gradle`
y `app/src/main/AndroidManifest.xml` por los archivos de este ZIP.
Si también copias `.github/workflows/compilar-apk.yml` tendrás el
artefacto `fitpro-ble-musica-v2`; puedes conservar tu workflow actual si funciona.

```powershell
git add .
git commit -m "Diagnostico BLE controles musica v2"
git push origin main
```

Después entra a Actions y descarga el artefacto generado. No requiere Android Studio.
