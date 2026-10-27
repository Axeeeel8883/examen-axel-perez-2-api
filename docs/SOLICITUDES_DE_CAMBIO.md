# Parte II

La implementación preparada es **SC-A**, en la rama `sc-a-perez` de AgroCampo. El filtro soloAbiertas se propaga desde UiState al repositorio y llega a la consulta SQL local junto con valle y búsqueda; no filtra listas dentro del composable. Tiene pruebas en memoria y debe demostrarse en modo avión. Se registran tres commits reales: conexión del estado, control visual y pruebas/documentación. No se ha recibido una asignación del docente: confirma que SC-A sea la letra que te corresponde.

Si el docente asigna otra solicitud, estas son las modificaciones necesarias (no se presentan como implementaciones terminadas):

- SC-B: migración/semilla idempotente para SUPERVISOR_CALIDAD, privilegios LOTE_LEER, LABOR_LEER y LABOR_EDITAR, política edicionGlobal=true. Crear usuario de prueba. Comprobar edición de otra persona y rechazo de cierre. No agregar if por nombre de rol.
- SC-C: cantidadJornales en entidad y DTO del backend, validación no negativa, modelo y DTO KMP, columna SQLDelight y migración, inserción/mapeo, formulario y detalle, semillas y pruebas. No basta con dibujar un campo.
- SC-D: consulta SQL de lotes con LEFT JOIN/COUNT de labores pendientes o consulta observable agrupada; mapear el resultado a estado de presentación; distintivo por lote. No mantener contadores manuales en ViewModel.

No integres las cuatro alternativas juntas si el docente pide una. Trabaja en la rama de la letra asignada, prueba y conserva al menos tres commits reales durante el bloque.
