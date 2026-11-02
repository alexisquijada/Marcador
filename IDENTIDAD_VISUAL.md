# MatchBoard — Guía de identidad visual v1.0

**Estado:** lineamientos de implementación propuestos a partir del ícono aprobado el 2 de noviembre de 2026. Los valores HEX y fuentes siguientes son una estandarización técnica aproximada del diseño, no una extracción colorimétrica exacta de la imagen.

## Identidad aprobada
- Nombre visible: **MatchBoard**.
- Ícono: cuadrado redondeado, fondo verde deportivo oscuro, líneas curvas verde lima, letras MB blancas, palabra MatchBoard blanca y lima, panel de resultado rojo 2 / azul 1.
- No incluir referencias exclusivas a pádel, tenis, pelotas, raquetas ni canchas.
- El ícono es la referencia principal del lenguaje visual.

## Paleta de trabajo (tokens sugeridos)
| Token | Hex | Aplicación |
|---|---|---|
| `brand.green.dark` | `#062F16` | Fondo de encabezados e identidad |
| `brand.green.deep` | `#031C0D` | Fondo profundo y degradado |
| `brand.lime` | `#91EF00` | Acciones principales, resaltados |
| `brand.lime.soft` | `#C4FF47` | Destellos y borde destacado |
| `ui.background` | `#101A13` | Fondo del menú oscuro |
| `ui.surface` | `#1A2A1E` | Tarjetas y paneles |
| `ui.border` | `#36553D` | Separadores y bordes discretos |
| `ui.text.primary` | `#FFFFFF` | Títulos y valores |
| `ui.text.secondary` | `#BED0C1` | Ayudas y subtítulos |
| `score.red` | `#F62222` | Equipo rojo |
| `score.blue` | `#087AF5` | Equipo azul |
| `feedback.warning` | `#FFCA3A` | Avisos y bloqueos |

## Tipografía propuesta
- Pantallas de configuración, textos y acciones: **Roboto** (Regular 400, Medium 500, Bold 700); fallback `sans-serif` de Android.
- Identidad/logo `MB`: estilo **sans-serif geométrico negrita inclinada**, conservar el dibujo del ícono como recurso gráfico, sin intentar recrearlo con una fuente.
- Números de los marcadores: **NO CAMBIAR** las tipografías ni estilos existentes; respetar opciones LED y sólido.
- Texto mínimo menú: 14sp; cuerpo 16sp; títulos de sección 20–24sp; área táctil mínima 48dp.

## Componentes de configuración
- Encabezado con logo MB, título MatchBoard y acceso al marcador al tocar cualquiera.
- Botones principales verde lima sobre oscuro, texto oscuro de alto contraste.
- Botones secundarios verde oscuro con borde verde medio y texto blanco.
- Paneles y opciones con esquinas redondeadas (12–16dp), separadores sobrios.
- Estados: conectado (verde lima), desconectado (rojo), bloqueado (amarillo).
- Asignación de controles: capturar pulsación física y mostrar botón detectado; no cambiar mapeos por defecto X/B rojo, A/Y azul y SELECT bloqueo.
- Navegación: Nuevo Partido primero; eliminar Inicio independiente; sección Controles genérica.

## Límites del rediseño
- Aplicar la identidad **al ícono, encabezado y menús de configuración**.
- **No modificar** la composición, tipografías, colores, estilos LED/sólidos, distribución vertical/horizontal ni opciones lado a lado/apilado del marcador existente.
- Mantener el mismo `applicationId` Android para permitir actualizaciones de la aplicación.
- Antes de distribuir la APK, verificar contraste, lectura en Nexus 10, modo horizontal/vertical y tamaños de ícono Android.
