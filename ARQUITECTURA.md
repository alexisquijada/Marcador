# Arquitectura actual y evolución

WebView Android Java que presenta assets/index.html sin conexión, con estado localStorage. `MainActivity` recibe KeyEvent desde mandos HID, detecta conexión y transmite pulsaciones a JavaScript.

El marcador y sus reglas aún se implementan juntos en `index.html`; **no se declara soporte para otros deportes**. Para extender: extraer funciones de reglas por deporte, contratos de puntuación y pruebas de transición antes de modificar la vista.

Los diseños del marcador existentes no se deben modificar al implementar identidad MatchBoard en menús.
