# MatchBoard — borrador de politica de privacidad

**Estado:** borrador técnico; antes de publicar debe alojarse una URL pública, identificarse al responsable y comprobarse contra la compilación final.

MatchBoard v3.3 está diseñada para funcionar sin conexión a Internet. En esta compilación no se integran SDK publicitarios, analítica, inicio de sesión ni envío de resultados a servidores.

La aplicación guarda en el dispositivo las configuraciones de visualización, asignaciones de botones y el estado del partido mediante almacenamiento local WebView. Los mandos se detectan mediante la API de dispositivos de entrada de Android.

No se solicita permiso de Internet ni Bluetooth para emparejamiento; el usuario vincula el mando en Ajustes de Android. No se comparten esos datos con el desarrollador desde el código de esta versión.

El usuario puede eliminar los datos desde los ajustes de Android, en Almacenamiento de la aplicación, mediante «Borrar datos»; ello también elimina puntuaciones y preferencias.

**Pendiente para publicación:** titular/contacto, URL pública, revisión de dependencias y declaración de seguridad de datos de Google Play.
