# Marcador de Pádel v2.5 + control BLE FitPro LP715(D)

**Proyecto Android para compilar por GitHub Actions. No incluye un APK ya compilado.**

## Funciones
- Conserva el marcador v2.4: tres formatos, LED y sólido, punto de oro, tie-break, saque, historial de deshacer y pantalla completa.
- Receptor BLE directo del reloj `WATCH` por Nordic UART, sin ejecutar FitPro ni modificar su firmware.
- Music Anterior `0x0A`: deshacer último punto.
- Music Play/Pausa `0x0B`: cambiar equipo seleccionado (rojo/azul).
- Music Siguiente `0x0C`: sumar un punto al equipo seleccionado.
- La app **NO activa la cámara automáticamente**. Cuando se conecta, envía solamente el comando conocido de desactivación de cámara.
- Los eventos de cámara no suman puntos. Los controles desconocidos 0x01/0x03/0x04 tampoco.
- El control remoto comienza **desactivado**, aun cuando el reloj esté conectado. Se activa manualmente en el menú de la app.
- El menú muestra hora y tipo del último comando musical recibido incluso desactivado (para probar sin alterar el puntaje).
- Indicador de conexión, equipo seleccionado y estado ON/OFF siempre visible.
- Filtro antirrebote de 350 ms por acción. Este valor es preliminar; probar en cancha antes de darlo por fiable.
- Guarda el log BLE en `files/ble_log.txt` para extracción mediante ADB root.

## Cómo compilar
El workflow está en `.github/workflows/compilar-apk.yml`. Se ejecuta al subir cambios a `main` o desde `Actions > Compilar Marcador Padel BLE > Run workflow`. El artefacto contiene `app-debug.apk`.

## Instalación

```powershell
cd C:\platform-tools
.\adb install -r .\app-debug.apk
```

El paquete es `cl.marcadorpadel.ble`, **diferente** del diagnóstico `cl.padel.fitprodiag` y del marcador antiguo. No reemplaza ninguno de ellos.

Abre **Marcador Pádel BLE**. Mantén pulsada la hora del marcador para abrir el menú. En la sección *Control FitPro LP715(D)* pulsa **Buscar WATCH**, acepta ubicación y verifica que Bluetooth y Ubicación estén activados en Android 10. Si no conecta al primer intento, vuelve a pulsar Buscar. Una vez conectado, pulsa **Activar control** y cierra el menú. Entra en Music del reloj.

Para ver si la entrada BLE responde sin modificar el partido, deja *Activar control* desactivado. Se actualizará `Último botón recibido` en el menú cada vez que llegue un comando musical.

## Logs ADB

```powershell
.\adb root
.\adb pull /data/data/cl.marcadorpadel.ble/files/ble_log.txt "$env:USERPROFILE\Downloads\marcador_ble_v25.txt"
```

## Límites y advertencias
- No existe prueba física de esta nueva versión: la compilación y la integración deberán verificarse en GitHub/Nexus 10.
- No podemos modificar desde Android la velocidad con que el **firmware del reloj** navega su menú de Música.
- El reloj debe estar en Música y activar el control elegido con su sensor táctil. Si tarda demasiado o la pantalla se apaga, los controles táctiles de la tablet siguen operativos.
- No se ha demostrado que el clic simple/doble del sensor transmita comandos BLE propios.
- No se ha probado recuperación de puntos si la tablet pierde energía justo mientras registra un punto; el marcador conserva su guardado local normal.
- El permiso de ubicación se usa para escanear BLE en Android 10.

## Actualizar repositorio GitHub existente
Descomprime este ZIP en una carpeta aparte; copia todos los archivos y carpetas (incluyendo `.github`) a la raíz de tu clon local de Git. Borra el código anterior del diagnóstico `app/src/main/java/cl/padel/fitprodiag` si todavía existe en ese repositorio. No borres la carpeta `.git` ni el historial. Luego `git add -A`, `git commit -m "Marcador de padel v2.5 con BLE FitPro"` y `git push origin main`.
