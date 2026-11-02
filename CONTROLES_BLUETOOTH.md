# Mandos HID

Android vincula el mando en Ajustes. La aplicación escucha dispositivos gamepad/joystick y KeyEvent. Valores iniciales: rojo X suma/B deshace, azul A suma/Y deshace, SELECT bloquea. Menú: tocar acción y presionar botón; asignaciones se guardan localmente.

Limitaciones: la detección depende de Android; mandos HID no equivalen a todos los dispositivos Bluetooth. Múltiples dispositivos comparten la misma configuración, no existen perfiles independientes aún. Requiere pruebas con SN30 y otros modelos.


### Bloqueo con SELECT (v3.10)
Cuando se bloquea, únicamente aparece una franja amarilla gruesa pegada al borde inferior, de todo el ancho; no hay avisos superpuestos. La puntuación y el resto de pantalla permanecen visibles. SELECT vuelve a desbloquear y elimina la franja. La desconexión conserva su aviso independiente.
