package pe.upeu.agrosierra.servicio
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pe.upeu.agrosierra.config.Fallo
import pe.upeu.agrosierra.dominio.*
import pe.upeu.agrosierra.repositorio.*
import pe.upeu.agrosierra.seguridad.*
import pe.upeu.agrosierra.web.dto.LaborRequest
import java.time.*
@Service class AgroServicio(val lotes:LoteRepository,val labores:LaborRepository,val usuarios:UsuarioRepository) {
 fun lote(id:Long)=lotes.findById(id).orElseThrow{Fallo(404,"El lote no existe")}
 private fun bloqueado(id:Long)=lotes.bloquear(id)?:throw Fallo(404,"El lote no existe")
 private fun validar(r:LaborRequest,l:Lote):TipoLabor {
  val errores=mutableMapOf<String,String>()
  val tipo=TipoLabor.entries.find{it.name==r.tipo}
  if(tipo==null) errores["tipo"]="Seleccione RIEGO, FUMIGACION o COSECHA"
  try { LocalDate.parse(r.fecha) } catch(e:Exception) { errores["fecha"]="Use una fecha ISO: AAAA-MM-DD" }
  if(!r.cantidad.isFinite() || r.cantidad<=0) errores["cantidad"]="Debe ser mayor que cero"
  if(r.observacion.length>1000) errores["observacion"]="Máximo 1000 caracteres"
  if(tipo==TipoLabor.FUMIGACION && r.cantidad>l.hectareas*l.dosisMaximaPorHectarea) errores["cantidad"]="La dosis máxima del lote es ${l.hectareas*l.dosisMaximaPorHectarea}"
  if(errores.isNotEmpty()) throw Fallo(400,"Datos inválidos",errores)
  return tipo!!
 }
 @Transactional fun registrar(r:LaborRequest,uid:Long,clave:String?):Labor {
  val l=bloqueado(r.loteId)
  if(clave!=null) {
   if(clave.length>120) throw Fallo(400,"Idempotency-Key demasiado largo")
   val previa=labores.findByClaveCliente("$uid:$clave")
   if(previa!=null) return previa
  }
  if(l.campaniaCerrada) throw Fallo(409,"No se registran labores en una campaña cerrada")
  val tipo=validar(r,l);val u=usuarios.findById(uid).orElseThrow()
  return labores.save(Labor(loteId=l.id,tipo=tipo,fecha=r.fecha,responsable=u.username,usuarioId=uid,cantidad=r.cantidad,observacion=r.observacion,claveCliente=clave?.let{"$uid:$it"}))
 }
 @Transactional fun actualizar(id:Long,r:LaborRequest,uid:Long):Labor {
  val labor=labores.findById(id).orElseThrow{Fallo(404,"La labor no existe")}
  val l=bloqueado(labor.loteId)
  if(l.campaniaCerrada) throw Fallo(409,"No se corrigen labores en una campaña cerrada")
  val u=usuarios.findById(uid).orElseThrow()
  // Política almacenada en el rol: permite nuevos roles sin comparar nombres.
  if(!u.rol.edicionGlobal && (labor.usuarioId!=uid || !Instant.now().isBefore(labor.registradoEn.plus(Duration.ofHours(24))))) throw Fallo(403,"Solo puede corregir sus labores dentro de las primeras 24 horas")
  if(r.loteId!=labor.loteId) throw Fallo(400,"No se puede cambiar de lote",mapOf("loteId" to "El lote es inmutable"))
  labor.tipo=validar(r,l);labor.fecha=r.fecha;labor.cantidad=r.cantidad;labor.observacion=r.observacion
  return labores.save(labor)
 }
 @Transactional fun eliminar(id:Long) {
  val labor=labores.findById(id).orElseThrow{Fallo(404,"La labor no existe")}
  bloqueado(labor.loteId);labores.delete(labor)
 }
 @Transactional fun cerrar(id:Long):Lote {
  val l=bloqueado(id)
  if(!labores.existsByLoteIdAndTipo(id,TipoLabor.COSECHA)) throw Fallo(409,"Debe registrar al menos una COSECHA antes de cerrar la campaña")
  l.campaniaCerrada=true;return lotes.save(l)
 }
}
