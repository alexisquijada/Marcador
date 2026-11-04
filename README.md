# MatchBoard v3.5 — Aplicación Android

Marcador de pádel offline con controles HID reconocidos por Android. Identidad de marca MatchBoard, ampliable en el futuro a otros deportes. **La funcionalidad actual de puntuación sigue siendo de pádel.**

## Archivos permanentes del proyecto

- [IDENTIDAD_VISUAL.md](IDENTIDAD_VISUAL.md) — obligatorio en todas las entregas.
- [CHANGELOG.md](CHANGELOG.md)
- [ARQUITECTURA.md](ARQUITECTURA.md)
- [CONTROLES_BLUETOOTH.md](CONTROLES_BLUETOOTH.md)
- [COMPATIBILIDAD_ANDROID.md](COMPATIBILIDAD_ANDROID.md)
- [PRIVACIDAD.md](PRIVACIDAD.md)
- [PUBLICACION_GOOGLE_PLAY.md](PUBLICACION_GOOGLE_PLAY.md)

## Compilar

Subir a GitHub, revisar Actions -> Compilar MatchBoard v3.5. Genera `app-debug.apk`. Para Play Store hay que completar firma y AAB de publicación. Los diseños del marcador y los mapeos iniciales SN30 permanecen sin cambios.

## v3.4
Revisar MENU_V3_4.md e IDENTIDAD_VISUAL.md antes de modificar el menú.

## v3.5 — Icono adaptativo corregido

El ícono utiliza fondo verde `#0B4F24` en la capa background y solo `MB` blanco + marcador `2 | 1` sobre transparencia en foreground. Sin palabra MatchBoard en el dibujo y sin negro ni bordes blancos. Incluye recursos legacy y vista previa. Menú y lógica de puntuación idénticos a v3.4.

## v3.6 — Restauración del ícono aprobado

Los recursos de Android usan el diseño original aprobado, sin sustituir la tipografía ni eliminar el texto MatchBoard del arte original. El background adaptativo es verde sólido y el primer plano es transparente fuera del dibujo.

## v3.7 — Iconografía

El ícono completo para Google Play se encuentra en `play_store/icono_512.png`. La variante de launcher sin palabra MatchBoard se encuentra en `app/src/main/res/drawable/ic_mb_foreground.png` y su fondo adaptativo en `res/values/colors.xml`. Consultar obligatoriamente `IDENTIDAD_VISUAL.md`.

## v3.8

Ícono aprobado en `ICONO_APROBADO_REFERENCIA.png`; launcher sin texto pequeño, usando recortes del original. Ver `IDENTIDAD_VISUAL.md`. Compilar con `.github/workflows/compilar-apk.yml`.

## v3.9

Ícono del launcher con fondo artístico original (no plano). Ver `ICONO_LAUNCHER_SIN_TEXTO.png` y `VISTA_PREVIA_ICONO_v3_9.png`. `IDENTIDAD_VISUAL.md` permanece obligatorio.


## v3.10 — Bloqueo preventivo SELECT
Al bloquear con SELECT, no aparece un mensaje ni un panel emergente: solo se muestra una franja amarilla gruesa de ancho completo pegada al borde inferior del marcador. No oscurece la pantalla, no ocupa espacio de diseño y no intercepta eventos táctiles. Al desbloquear, desaparece. Se mantiene la alerta de desconexión.


## Marcador continuo y resumen de sets

En la vista horizontal lado a lado, el equipo que saca aparece a la izquierda y conserva su color. El partido permite acumular sets hasta iniciar uno nuevo.

Al completar cada set se muestra un resumen grande con los resultados de los sets registrados y los colores de los equipos. Vuelve automáticamente al marcador tras 10 segundos por defecto. El tiempo se configura en **Apariencia → Duración del resumen de sets**, entre 1 y 300 segundos, y se conserva al reiniciar el partido. También se puede cerrar con **Volver al marcador**; START alterna entre el marcador y el resumen. Al abrirlo con START, el resumen permanece visible hasta volver al marcador. Los puntos se registran únicamente en la vista del marcador.

Los resultados detallados de sets se guardan desde esta función; las partidas antiguas conservan su total de sets, pero no se inventan resultados anteriores. Deshacer el punto que cerró un set elimina ese resultado del resumen. Cambiar el total de sets mediante edición manual borra el detalle anterior; iniciar un nuevo partido borra todo el historial de sets.
