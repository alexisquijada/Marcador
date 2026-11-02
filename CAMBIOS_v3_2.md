# Marcador de Pádel v3.2 — controles HID y menú

Base: proyecto v3.1 facilitado por el usuario.

- Nuevo Partido primero, con confirmación existente.
- Título y P en menú vuelven al marcador.
- Compatibilidad nativa con dispositivos Android SOURCE_GAMEPAD y SOURCE_JOYSTICK; no restringido al nombre SN30.
- Reasignación por pulsación existente y persistente, con alerta por botón duplicado.
- Asignaciones originales sin cambios: X=rojo+, B=deshacer rojo, A=azul+, Y=deshacer azul, SELECT=bloqueo.
- Visualización predeterminada para instalaciones nuevas: sólido y lado a lado. Diseños existentes intactos.
- Los ajustes guardados de usuarios actuales tienen prioridad sobre los valores iniciales.
- Activación automática cuando se detecta conexión; desconexión desactiva por seguridad.
- Los controles detectados comparten el mismo perfil; **NO** hay perfiles por dispositivo ni asignación exclusiva por equipo en v3.2.

## Pruebas pendientes
- Compilar con GitHub Actions (no hay SDK Android local).
- Probar entrada HID, conexiones, desconexiones y bloqueo en Nexus 10.
- Probar simultaneidad y compatibilidad real por modelo de mando.
