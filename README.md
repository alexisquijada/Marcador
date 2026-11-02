# Marcador de Pádel v3.1 — SN30, alerta grande de conectividad + edición manual

Código fuente para Nexus 10 Android 10, compilable con GitHub Actions. Sin FitPro ni escaneo BLE. **Este ZIP no es un APK compilado**.

## 8BitDo SN30

| Botón | Acción |
| --- | --- |
| X | Sumar rojo |
| B | Deshacer **solo si el último punto del partido fue rojo** |
| A | Sumar azul |
| Y | Deshacer **solo si el último punto del partido fue azul** |
| SELECT | Bloquear / desbloquear las teclas del mando |

El SN30 comienza desactivado al abrir la app. Mantén pulsada la hora superior izquierda para abrir el menú y pulsa «Activar SN30». Puede reasignarse cada botón desde ese menú. Se conserva la entrada táctil.

## Alerta de conectividad visible desde lejos

- **Roja, pantalla completa**: **CONTROL DESCONECTADO** cuando Android no detecta el 8BitDo SN30, incluido el inicio sin mando. Bordes rojos y aviso central grande, no solo letras pequeñas.
- **Amarilla**: **CONTROL DESACTIVADO** cuando el SN30 está conectado pero no habilitado en el menú. La app desactiva el control al perder conexión; al reconectar, el usuario debe activarlo nuevamente por seguridad.
- **Amarilla**: **CONTROL BLOQUEADO** cuando SELECT ha bloqueado los botones. SELECT permite desbloquear.
- **Normal**: cuando está conectado, activado y desbloqueado no hay aviso grande; permanecen los puntos visibles sin interferencia.
- Se utilizan `InputManager.InputDeviceListener` y una comprobación cada 2 segundos de los dispositivos de entrada que Android reporta. **No se usa un tiempo sin pulsaciones** para concluir que está desconectado (puede transcurrir mucho tiempo entre puntos).
- El mensaje es visual: no bloquea las pulsaciones táctiles; el marcador y su puntuación se conservan al desconectar.
- **Límite técnico**: solo puede detectar una desconexión cuando Android retira el dispositivo de entrada. La latencia real de detección/reconexión debe validarse en la Nexus 10.

## Corrección manual completa

Mantén pulsada la hora → menú → **Editar resultado manualmente**.

Desde ahí se pueden establecer, para cada equipo:
- Puntos normales (0, 15, 30, 40) o puntos numéricos en tie-break.
- Juegos del set en curso (0 a 7).
- Sets ganados (0 a 2).
- Quién saca, si hay tie-break y tiempo transcurrido.

Se validan enteros, tie-break solo con 6–6 en juegos y que no haya dos ganadores del partido. Con dos sets ganados, el partido se declara terminado. La edición **no adjudica automáticamente juegos ni sets al guardar**: se respetan los valores introducidos. Para un set ya terminado, ingresa los juegos del **nuevo set**, no el resultado histórico del set anterior.

Al pulsar **Revisar cambios**, aparece una confirmación con el resultado antes de aplicarlo. Puedes cancelar sin modificar nada. Al confirmar:
- La nueva puntuación se guarda en localStorage (clave `padel_scoreboard_v08`, compatible con versiones anteriores).
- Se borra el historial de jugadas anterior: las próximas correcciones solo se refieren a puntos registrados tras la edición.
- Se conserva temporalmente un respaldo del estado anterior en localStorage (`padel_sn30_manual_backup_v1`). Desde el mismo editor se ofrece **Restaurar resultado anterior**, con confirmación que advierte que se perderán los puntos jugados después.
- El respaldo se elimina al iniciar partido nuevo o cargar un ejemplo.

**Importante:** un botón B/Y no reduce 15 puntos aritméticamente. Solo deshace el último punto del partido si lo ganó el equipo correspondiente. Esto evita borrar puntos de otros juegos o sets al equivocarse de tecla.

## Subida y compilación

1. Extrae el ZIP y reemplaza con su contenido los archivos del proyecto en la **raíz** de `alexisquijada/Marcador`, rama `main` (mantén el historial `.git` intacto si usas GitHub Desktop).
2. Confirma los cambios y haz `Push origin`; GitHub Actions ejecuta `Compilar Marcador Padel SN30`.
3. Descarga el artefacto `MarcadorPadel-SN30-ALERTA-CONEXION-v3-1` que incluye `app-debug.apk`.
4. En PowerShell desde `C:\platform-tools`:

   ```powershell
   .\adb devices -l
   .\adb install -r .\app-debug.apk
   ```

Se mantiene `applicationId cl.marcadorpadel.sn30`; una instalación sobre la versión anterior requiere la misma firma debug.

## Validación pendiente

Las pruebas de la lógica y del editor se ejecutaron en Chromium local. Quedan pendientes la compilación en GitHub Actions y la validación táctil real en Nexus 10 con SN30.
