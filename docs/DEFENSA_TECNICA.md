# Guía de defensa - AgroSierra

Estas respuestas describen el código entregado. Ensaya mostrando cada archivo y ejecutando la operación; no afirmes haber hecho pruebas que no puedas repetir.

1. **¿Dónde se verifica el privilegio?** En AgroAPI, `config/SecurityConfig.kt` exige autenticación y `web/AgroController.kt` usa `@PreAuthorize(hasAuthority(...))`. `config/JwtAuthenticationFilter.kt` construye las authorities desde el usuario de la base. Prueba POST /labores con admin: 403 aunque se invoque fuera de Android. Ocultar botones solo mejora la experiencia.

2. **¿Qué lleva el JWT?** `config/JwtService.kt` firma HS512 con clave persistente, coloca `sub` (ID), `privilegios`, `iat` y `exp`. La carga se codifica en Base64URL y no está cifrada. El filtro verifica firma y expiración; un token vencido devuelve 401. No existe refresh.

3. **¿Dónde se guarda?** `androidApp/.../AlmacenSeguroAndroid.kt` cifra la sesión con AES-GCM. SharedPreferences contiene IV y texto cifrado; la clave AES no sale de Android Keystore. Las preferencias sin cifrar revelarían un bearer que permite actuar como su titular.

4. **¿Quién añade Authorization?** `shared/.../data/remote/ClienteHttp.kt`, dentro de `defaultRequest`, lee el almacén y añade Bearer solo a rutas protegidas. Centralizarlo evita olvidos y duplicación. Logging sanea la cabecera y se desactiva en release.

5. **¿401 o 403?** `MapeoErrores.kt` traduce cada código. El callback 401 de `AgroRepositorioOffline.red()` borra el almacén y el StateFlow de sesión. El 403 se convierte en SinPrivilegio, muestra mensaje y mantiene la sesión. Las credenciales incorrectas tienen su propio mensaje durante el login.

6. **Recorrido del dato:** backend `dominio/Entidades.kt` -> `repositorio/Repositorios.kt` -> `servicio/AgroServicio.kt` -> `web/AgroController.kt` -> JSON -> aplicación `AgroApi.kt` -> `dto/Dto.kt` -> `dominio()` -> `AgroRepositorioOffline.kt` -> `BaseLocal.kt`/AgroDb.sq -> Flow -> `AgroViewModel.kt` -> `AgroApp.kt` -> Compose.

7. **¿Por qué observar SQLite?** La red puede faltar. `BaseLocal.lotes()` usa `asFlow().mapToList()` sobre SQLDelight. La UI observa siempre la base; sincronizar solo la modifica. Muestra el listado con modo avión.

8. **Registro offline:** `guardar()` asigna un ID negativo, guarda pendiente y operación CREAR, e intenta enviar. Sin conexión la fila queda visible. Su claveCliente se conserva para reintentar el mismo POST. Muestra la fila y la consulta `siguienteIdLocal`.

9. **Orden de sincronización:** Mutex -> enviar pendientes -> descargar todas las páginas -> reemplazar sincronizadas en una transacción -> guardar fecha. Si se descargara y borrara primero, se podría perder lo creado sin red. La sustitución provisional/definitiva es atómica y el POST idempotente evita duplicados ante respuestas perdidas.

10. **¿Cómo se prueba?** `commonTest/.../RepositorioTest.kt` usa MockEngine para respuestas y fallos; `jvmTest/.../BasePrueba.kt` aporta JdbcSqliteDriver en memoria. `subirAntesDeBajar` registra las rutas y verifica su orden. `registrarSinRed` comprueba la fila pendiente. Ejecuta `:shared:jvmTest` y muestra el reporte.

11. **Validación por campo:** AgroServicio.validar genera Fallo(400, ..., campos); ManejadorDeErrores serializa validationErrors; ErrorDto lo recibe; MapeoErrores produce ErrorApi.Validacion; ViewModel copia campos a UiState y Campo los dibuja bajo cada entrada. Provoca una fumigación de cantidad superior al máximo.

12. **Rol nuevo:** agrega el rol, sus privilegios y su política edicionGlobal a las tablas por una migración/semilla idempotente. Usuarios se asignan al rol. No cambies controladores ni compares strings de nombres de rol. El login y /usuarios/me ya devuelven privilegios y la UI los usa. Para SC-B: SUPERVISOR_CALIDAD = LOTE_LEER + LABOR_LEER + LABOR_EDITAR, edicionGlobal=true, sin CAMPANIA_CERRAR.

13. **Git y autoría:** muestra `git log --graph --oneline --all` y la rama sc-a-perez. Explica qué se adaptó desde PharmaMobil y qué se agregó con asistencia. No inventes un conflicto: si no ocurrió, dilo y explica cómo resolverías uno en la rama feature antes de fusionar. Los commits actuales no reemplazan el historial de trabajo previo de la unidad.
