# MatchBoard — Guía de identidad visual v1.0

**Estado:** identidad de menús propuesta e ícono Android definitivo especificado el 2 de noviembre de 2026. Los valores HEX y fuentes siguientes son una estandarización técnica aproximada del diseño, no una extracción colorimétrica exacta de la imagen.

## Identidad aprobada
- Nombre visible: **MatchBoard**.
- Ícono Android definitivo: fondo verde continuo, MB blanco grande y panel rojo 2 / azul 1, sin palabra MatchBoard ni imagen negra de fondo. Ver especificación de ícono adaptativo más abajo.
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
- Identificador Android previsto desde v3.4: `cl.matchboard.app`. Mantenerlo estable en las siguientes versiones y utilizar firma compatible para actualizaciones.
- Antes de distribuir la APK, verificar contraste, lectura en Nexus 10, modo horizontal/vertical y tamaños de ícono Android.

## Especificación final aprobada — ícono de launcher Android

**Objetivo:** máxima legibilidad en tamaño pequeño y relleno completo de la máscara circular, sin márgenes blancos ni bloque negro. No alterar los diseños del marcador dentro de la app.

- **Adaptive icon, capa background:** verde sólido `#0B4F24` como color de referencia. Debe extenderse al 100 % del lienzo, incluidas las zonas que Android podría revelar al recortar o animar. No usar el fondo blanco ni la fotografía cuadrada original como fondo.
- **Adaptive icon, capa foreground:** fondo totalmente transparente. Contiene únicamente `MB` en blanco, con trazo grueso y alto contraste, y debajo un panel compacto con `2` blanco sobre rojo y `1` blanco sobre azul. Mantener suficiente separación y no dibujar bordes finos.
- **Jerarquía:** `MB` domina el ícono y ocupa todo el ancho útil posible **dentro del área segura**; marcador secundario claramente legible. Sin nombre MatchBoard escrito dentro del ícono, sin pádel/tenis, sin elementos decorativos que compitan con MB.
- **Área segura Android:** diseñar sobre lienzo adaptativo de 108 × 108 dp; los elementos que deban ser visibles deben permanecer dentro del círculo central de 66 dp de diámetro (centro en 54,54). El background sí debe rellenar las 108 × 108 dp. La máscara final puede ser circular, cuadrada redondeada u otra según el launcher.
- **Recursos:** `mipmap-anydpi-v26/ic_launcher.xml` e `ic_launcher_round.xml` con `<background>` verde sólido y `<foreground>` transparente; generar recursos legacy `mipmap-*` y `play_store/icono_512.png` con el mismo criterio de borde a borde.
- **Tipografía del monograma:** letras MB como recurso vectorial o trazado gráfico grueso, no texto pequeño; verificar que no haya clipping de las letras en recortes circulares.
- **Control de calidad obligatorio:** revisar en fondos claro y oscuro, en máscara circular y cuadrada redondeada y en una vista equivalente a 48 × 48 px. Verificar lectura clara de `MB`, `2` y `1`, y ausencia de aro blanco, borde blanco o rectángulo negro. Comprobar el resultado en dispositivo real.
- **Alcance de esta aprobación:** es una especificación de diseño. No certifica que los recursos actuales de la APK estén corregidos; esa implementación y sus pruebas se realizarán por separado.


## Anexo: especificación de aprobación v3.5
# MatchBoard — Guía de identidad visual v1.0

**Estado:** identidad de menús propuesta e ícono Android definitivo especificado el 2 de noviembre de 2026. Los valores HEX y fuentes siguientes son una estandarización técnica aproximada del diseño, no una extracción colorimétrica exacta de la imagen.

## Identidad aprobada
- Nombre visible: **MatchBoard**.
- Ícono Android definitivo: fondo verde continuo, MB blanco grande y panel rojo 2 / azul 1, sin palabra MatchBoard ni imagen negra de fondo. Ver especificación de ícono adaptativo más abajo.
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
- Identificador Android previsto desde v3.4: `cl.matchboard.app`. Mantenerlo estable en las siguientes versiones y utilizar firma compatible para actualizaciones.
- Antes de distribuir la APK, verificar contraste, lectura en Nexus 10, modo horizontal/vertical y tamaños de ícono Android.

## Especificación final aprobada — ícono de launcher Android

**Objetivo:** máxima legibilidad en tamaño pequeño y relleno completo de la máscara circular, sin márgenes blancos ni bloque negro. No alterar los diseños del marcador dentro de la app.

- **Adaptive icon, capa background:** verde sólido `#0B4F24` como color de referencia. Debe extenderse al 100 % del lienzo, incluidas las zonas que Android podría revelar al recortar o animar. No usar el fondo blanco ni la fotografía cuadrada original como fondo.
- **Adaptive icon, capa foreground:** fondo totalmente transparente. Contiene únicamente `MB` en blanco, con trazo grueso y alto contraste, y debajo un panel compacto con `2` blanco sobre rojo y `1` blanco sobre azul. Mantener suficiente separación y no dibujar bordes finos.
- **Jerarquía:** `MB` domina el ícono y ocupa todo el ancho útil posible **dentro del área segura**; marcador secundario claramente legible. Sin nombre MatchBoard escrito dentro del ícono, sin pádel/tenis, sin elementos decorativos que compitan con MB.
- **Área segura Android:** diseñar sobre lienzo adaptativo de 108 × 108 dp; los elementos que deban ser visibles deben permanecer dentro del círculo central de 66 dp de diámetro (centro en 54,54). El background sí debe rellenar las 108 × 108 dp. La máscara final puede ser circular, cuadrada redondeada u otra según el launcher.
- **Recursos:** `mipmap-anydpi-v26/ic_launcher.xml` e `ic_launcher_round.xml` con `<background>` verde sólido y `<foreground>` transparente; generar recursos legacy `mipmap-*` y `play_store/icono_512.png` con el mismo criterio de borde a borde.
- **Tipografía del monograma:** letras MB como recurso vectorial o trazado gráfico grueso, no texto pequeño; verificar que no haya clipping de las letras en recortes circulares.
- **Control de calidad obligatorio:** revisar en fondos claro y oscuro, en máscara circular y cuadrada redondeada y en una vista equivalente a 48 × 48 px. Verificar lectura clara de `MB`, `2` y `1`, y ausencia de aro blanco, borde blanco o rectángulo negro. Comprobar el resultado en dispositivo real.
- **Alcance de esta aprobación:** es una especificación de diseño. No certifica que los recursos actuales de la APK estén corregidos; esa implementación y sus pruebas se realizarán por separado.


## Aclaración aprobada v3.6 — fuente visual prevalente

El ícono oficial es la composición original verde MatchBoard incluida como `ICONO_APROBADO_REFERENCIA.png`: conserva MB blanco con su forma original, texto MatchBoard blanco/lima, marcador 2 rojo/1 azul y líneas verdes. La guía simplificada anterior (sin texto) no sustituye al arte original. Se adapta el fondo para Android sin rediseñar la composición.


## v3.7 — Dos variantes obligatorias del ícono

- **Promocional / Google Play**: conservar íntegro `play_store/icono_512.png` y `ICONO_APROBADO_REFERENCIA.png` de v3.6: MB, palabra MatchBoard, líneas verdes y marcador.
- **Launcher Android**: versión simplificada sin texto MatchBoard dentro de la figura. Letras MB blancas grandes y números 2 rojo / 1 azul debajo. Fondo sólido verde `#0B4F24`, en capa de background real; foreground PNG RGBA transparente. Sin bloque negro, sin borde blanco.
- El launcher se debe inspeccionar visualmente en máscara circular y tamaños 48 / 72 / 144 / 288 px.
- Las variantes tienen funciones diferentes: no reemplazar la marca promocional con el ícono simplificado.


## v3.8 — Uso obligatorio del diseño original aprobado
- Fuente gráfica inmutable: `ICONO_APROBADO_REFERENCIA.png`. No redibujar MB ni los números.
- Google Play: conservar `play_store/icono_512.png` de la composición original aprobada; incluye texto MatchBoard.
- Launcher: recortes de MB y del marcador 2/1 tomados directamente de la imagen original, sin la palabra MatchBoard, sobre fondo adaptativo verde completo `#0B4F24`.
- No reconstruir letras con fuentes del sistema ni alterar colores de los elementos.
- Revisar visualmente ícono circular en el dispositivo; la vista previa es una simulación.


## v3.9 — Corrección del fondo original (obligatoria)
- El diseño aprobado `ICONO_APROBADO_REFERENCIA.png` es la fuente gráfica maestra.
- Fondo del launcher: **la textura verde original con degradados y líneas verde lima**, nunca verde plano.
- Conserva las letras MB y los recuadros 2 / 1 originales. Elimina exclusivamente el texto pequeño MatchBoard en el launcher.
- Google Play conserva `play_store/icono_512.png` y el logotipo completo.
- Recursos adaptativos `ic_launcher.xml` y `ic_launcher_round.xml` usan la misma ilustración de fondo a pantalla completa; no hay caja negra o borde blanco añadido.
- La máscara del launcher puede recortar extremos de la ilustración. Validar visualmente en Android.
