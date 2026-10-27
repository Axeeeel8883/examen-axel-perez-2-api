package pe.upeu.agrosierra.web
import org.springframework.web.bind.annotation.*
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.http.HttpStatus
import org.springframework.data.domain.PageRequest
import pe.upeu.agrosierra.config.Fallo
import pe.upeu.agrosierra.repositorio.*
import pe.upeu.agrosierra.servicio.AgroServicio
import pe.upeu.agrosierra.web.dto.*
@RestController @RequestMapping("/api/v1")
class AgroController(val servicio:AgroServicio,val lotes:LoteRepository,val labores:LaborRepository) {
 private fun pagina(p:Int,t:Int):PageRequest { if(p<0 || t !in 1..100) throw Fallo(400,"Página o tamaño inválido");return PageRequest.of(p,t) }
 @GetMapping("/lotes") @PreAuthorize("hasAuthority('LOTE_LEER')")
 fun listar(@RequestParam(defaultValue="0") pagina:Int,@RequestParam(defaultValue="20") tamanio:Int,@RequestParam(required=false) valle:String?,@RequestParam(required=false) campaniaCerrada:Boolean?)=lotes.filtrar(valle?.takeIf{it.isNotBlank()},campaniaCerrada,pagina(pagina,tamanio)).dto()
 @GetMapping("/lotes/{id}") @PreAuthorize("hasAuthority('LOTE_LEER')") fun lote(@PathVariable id:Long)=servicio.lote(id)
 @GetMapping("/lotes/{id}/labores") @PreAuthorize("hasAuthority('LABOR_LEER')")
 fun listarLabores(@PathVariable id:Long,@RequestParam(defaultValue="0") pagina:Int,@RequestParam(defaultValue="20") tamanio:Int):Pagina<pe.upeu.agrosierra.dominio.Labor> {
  servicio.lote(id);return labores.findByLoteIdOrderByFechaDescIdDesc(id,pagina(pagina,tamanio)).dto()
 }
 @PostMapping("/labores") @PreAuthorize("hasAuthority('LABOR_CREAR')") @ResponseStatus(HttpStatus.CREATED)
 fun crear(@RequestBody r:LaborRequest,a:Authentication,@RequestHeader(value="Idempotency-Key",required=false) clave:String?)=servicio.registrar(r,a.name.toLong(),clave)
 @PutMapping("/labores/{id}") @PreAuthorize("hasAuthority('LABOR_EDITAR')")
 fun editar(@PathVariable id:Long,@RequestBody r:LaborRequest,a:Authentication)=servicio.actualizar(id,r,a.name.toLong())
 @DeleteMapping("/labores/{id}") @PreAuthorize("hasAuthority('LABOR_ELIMINAR')") @ResponseStatus(HttpStatus.NO_CONTENT)
 fun eliminar(@PathVariable id:Long)=servicio.eliminar(id)
 @PostMapping("/lotes/{id}/cerrar-campania") @PreAuthorize("hasAuthority('CAMPANIA_CERRAR')")
 fun cerrar(@PathVariable id:Long)=servicio.cerrar(id)
}
