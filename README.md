# AgroAPI - examen Axel Perez 2

Backend real del caso AgroSierra. Spring Boot 3.4.5, Kotlin 2.1.21, Spring Security, JPA, H2 persistente y JWT HS512.

## Ejecución

Requiere JDK 21 y acceso a Maven Central la primera vez. Desde esta carpeta:

```powershell
.\gradlew.bat bootRun
```

En Linux/macOS: `sh gradlew bootRun`. Un solo comando inicia el servicio en `http://localhost:8080` y carga 8 lotes, 20 labores y 3 usuarios en una base nueva. La aplicación Android usa `http://10.0.2.2:8080/api/v1/`, que apunta al equipo anfitrión desde el emulador.

Pruebas: `.\gradlew.bat test`. JAR: `.\gradlew.bat bootJar`, luego `java -jar build/libs/AgroAPI-1.0.0.jar`.

Usuarios de demostración: `tecnico1`, `jefe1`, `admin`; contraseña de los tres: `Agro2026*`. Las contraseñas se guardan con BCrypt. Estos usuarios son datos de laboratorio.

## Contrato

| Método | Ruta relativa a /api/v1 | Privilegio | Éxito |
|---|---|---|---|
| POST | /auth/login | Público | 200 |
| GET | /usuarios/me | Autenticado | 200 |
| GET | /lotes?pagina=0&tamanio=20&valle=Mantaro&campaniaCerrada=false | LOTE_LEER | 200 |
| GET | /lotes/{id} | LOTE_LEER | 200 |
| GET | /lotes/{id}/labores?pagina=0&tamanio=20 | LABOR_LEER | 200 |
| POST | /labores | LABOR_CREAR | 201 |
| PUT | /labores/{id} | LABOR_EDITAR | 200 |
| DELETE | /labores/{id} | LABOR_ELIMINAR | 204, sin cuerpo |
| POST | /lotes/{id}/cerrar-campania | CAMPANIA_CERRAR | 200 |
| POST | /usuarios | USUARIO_GESTIONAR | 201 |

Login recibe `{"username":"tecnico1","password":"Agro2026*"}`. Devuelve `token`, `expiraEnSegundos` (3600), `usuario`, `nombre`, `rol`, `privilegios`, `id` y `edicionGlobal`. El token lleva el identificador del usuario, los privilegios, `iat` y `exp`. No hay renovación.

Labor recibe `loteId: Long`, `tipo: RIEGO|FUMIGACION|COSECHA`, `fecha: AAAA-MM-DD`, `cantidad: Double > 0`, `observacion: String`. El responsable se obtiene del usuario autenticado; el cliente no puede suplantarlo. POST admite la cabecera opcional `Idempotency-Key`, usada por AgroCampo para reintentos seguros.

Los listados devuelven `contenido`, `pagina`, `tamanio`, `totalElementos`, `totalPaginas`, `ultima`. Las páginas comienzan en cero; tamaño de 1 a 100. Las labores se ordenan por fecha e ID descendentes. El filtro de campaña es `campaniaCerrada`.

Los errores devuelven `timestamp`, `status`, `error`, `message`, `path`, `validationErrors`. 400: campos inválidos; 401: sesión ausente, alterada o vencida; 403: falta de privilegio o edición ajena/fuera de plazo; 404: recurso inexistente; 409: campaña cerrada o cierre sin cosecha; 500: error interno sin detalles sensibles.

## Roles

| Privilegio | TECNICO | JEFE_CAMPO | ADMINISTRADOR |
|---|---|---|---|
| LOTE_LEER | Sí | Sí | Sí |
| LABOR_LEER | Sí | Sí | Sí |
| LABOR_CREAR | Sí | Sí | No |
| LABOR_EDITAR | Propias, antes de 24 h | Todas | No |
| LABOR_ELIMINAR | No | Sí | No |
| CAMPANIA_CERRAR | No | Sí | No |
| USUARIO_GESTIONAR | No | No | Sí |

`SecurityConfig` protege todas las rutas salvo el login. `JwtAuthenticationFilter` verifica firma y expiración, carga el usuario activo y construye sus authorities desde la base. `@PreAuthorize` verifica privilegios en los endpoints; no hay comparaciones de nombres de rol en controladores. `Rol.edicionGlobal` expresa la política de edición y permite ampliarla a nuevos roles.

## Reglas y persistencia

`servicio/AgroServicio.kt` aplica RN-01 a RN-04 dentro de transacciones: no registrar/corregir en campaña cerrada; edición propia antes de 24 horas salvo política global; cosecha obligatoria para cerrar; fumigación hasta hectáreas por dosis máxima. Se bloquea la fila del lote para coordinar cierre y registro concurrentes.

La base H2 se guarda en `data/agro`. La clave de firma de desarrollo se genera aleatoriamente y se conserva en `data/jwt.key`; no se versiona. Puede suministrarse `JWT_SECRET` con al menos 64 bytes aleatorios codificados en Base64. No publiques `data/` ni la clave. En producción usa HTTPS, un gestor de secretos y una base administrada.

## Paquetes

`config`: JWT, filtro, seguridad, errores y semillas. `seguridad`: usuarios, roles, privilegios y autenticación. `dominio`: Lote/Labor. `repositorio`: JPA. `servicio`: reglas y transacciones. `web`: rutas y DTO.

## Verificación

`AgroApiTest` cubre semillas, paginación, credenciales incorrectas, token ausente/alterado, privilegios, RN-01 a RN-04 y crear/editar/eliminar con idempotencia. El paquete de entrega incluye además 13 peticiones HTTP ejecutadas contra el proceso real, con un token firmado y vencido.

Desarrollo asistido por IA el 27/10/2026. El historial generado en esta entrega no acredita trabajo durante semanas anteriores. La defensa y las evidencias deben corresponder a lo realmente comprendido y ejecutado.
