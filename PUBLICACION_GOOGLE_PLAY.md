# Preparación para Google Play — MatchBoard v3.3

- applicationId preservado: `cl.marcadorpadel.sn30` para continuidad con instalaciones anteriores. **No cambiar después de publicar**.
- compileSdk / targetSdk: 36; minSdk: 23. Se requiere probar compatibilidad real con Android 10 y Android moderno.
- GitHub Actions genera **APK debug exclusivamente para pruebas**, no AAB de publicación firmado.
- Para publicar: generar `bundleRelease` y configurar firma mediante secretos protegidos en CI; no almacenar claves en el repositorio. Habilitar Play App Signing.
- Completar privacidad, contacto, ficha Play, capturas auténticas, pruebas cerradas cuando aplique, pruebas Android 16 y declaración Data Safety.
- Sin anuncios, sin SDK publicitarios y sin solicitud de permiso Internet en esta versión.
- Revisar seguridad de WebView y comportamiento con edge-to-edge al apuntar a API 36.
- Compatibilidad multidispositivo HID: misma asignación para todos; perfiles individuales por control siguen pendientes.
- El motor de reglas actualmente es específico de pádel. Otros deportes requerirán separación posterior del motor, no están implementados.
