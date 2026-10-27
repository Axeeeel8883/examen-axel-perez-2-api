# Demostración reproducible

1. Arranca AgroAPI con gradlew bootRun. Ejecuta las pruebas y comprueba ocho lotes en una base nueva.
2. Sin token, GET /api/v1/lotes -> 401. Login con contraseña incorrecta -> 401.
3. Login con admin, POST /labores -> 403. Consulta /usuarios/me y muestra privilegios.
4. Login con tecnico1 desde Android. Sincroniza y observa padrón, buscador, valle y fecha de actualización.
5. Lote 7: campaña cerrada. Un POST directo -> 409 (RN-01).
6. Técnico: edita una labor propia reciente -> 200; una ajena o registrada hace más de 24 horas -> 403. El jefe sí puede editar cualquier labor de una campaña abierta.
7. Fumigación en lote 1 con cantidad 999 -> 400 con validationErrors.cantidad. En Android muestra el mensaje bajo Cantidad.
8. Jefe: cierre del lote 2 sin cosecha -> 409. En un lote con cosecha -> 200. Esta acción modifica la demostración: usa una base de laboratorio.
9. Activa modo avión en Android. La lista permanece. Crea una labor: queda pendiente con ID negativo.
10. Cierra y vuelve a abrir: mientras la sesión no venza, la base permite consultar datos sin red.
11. Desactiva modo avión: el monitor intenta sincronizar; también puedes usar el botón. La fila pasa a ID positivo y desaparece la marca pendiente. Repite sincronización: no se duplica.
12. Perfil muestra rol y privilegios. Cierra sesión y comprueba que vuelve al login. El token se borra.
13. SC-A: combina Solo campañas abiertas con valle y búsqueda en modo avión. Verifica que cambiar el filtro no hace peticiones de red.
14. Muestra reportes de tests, el gráfico Git y la etiqueta v1.0-unidad2.

No se distribuye la base del servidor con la entrega: al iniciar desde una carpeta limpia se generan las semillas originales. No elimines datos de una instalación real para repetir demostraciones.
