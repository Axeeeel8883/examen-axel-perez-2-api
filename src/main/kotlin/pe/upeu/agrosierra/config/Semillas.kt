package pe.upeu.agrosierra.config
import org.springframework.context.annotation.*
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pe.upeu.agrosierra.seguridad.*
import pe.upeu.agrosierra.dominio.*
import pe.upeu.agrosierra.repositorio.*
import java.time.LocalDate
@Configuration class Semillas {
 @Bean fun datos(pr:PrivilegioRepository,rr:RolRepository,ur:UsuarioRepository,lr:LoteRepository,ar:LaborRepository,e:BCryptPasswordEncoder)=CommandLineRunner {
  if(rr.count()==0L) {
   val nombres=listOf("LOTE_LEER","LABOR_LEER","LABOR_CREAR","LABOR_EDITAR","LABOR_ELIMINAR","CAMPANIA_CERRAR","USUARIO_GESTIONAR")
   val p=nombres.associateWith{pr.save(Privilegio(it))}
   fun rol(n:String,global:Boolean,vararg privilegios:String)=rr.save(Rol(n,privilegios.map{p.getValue(it)}.toMutableSet(),global))
   val tecnico=rol("TECNICO",false,"LOTE_LEER","LABOR_LEER","LABOR_CREAR","LABOR_EDITAR")
   val jefe=rol("JEFE_CAMPO",true,"LOTE_LEER","LABOR_LEER","LABOR_CREAR","LABOR_EDITAR","LABOR_ELIMINAR","CAMPANIA_CERRAR")
   val admin=rol("ADMINISTRADOR",false,"LOTE_LEER","LABOR_LEER","USUARIO_GESTIONAR")
   listOf("tecnico1" to tecnico,"jefe1" to jefe,"admin" to admin).forEach{(n,r)->ur.save(Usuario(username=n,password=e.encode("Agro2026*"),nombre=n,rol=r))}
  }
  if(lr.count()==0L) {
   val u=ur.findByUsername("tecnico1")!!
   (1..8).forEach { n ->
    val l=lr.save(Lote(codigo="LOT-%03d".format(n),nombre="Parcela $n",hectareas=n.toDouble()+1,cultivo=listOf("Papa","Maíz","Quinua")[(n-1)%3],valle=listOf("Mantaro","Sagrado","Colca")[(n-1)%3],campaniaCerrada=n>=7,dosisMaximaPorHectarea=2.0))
    val cantidad=if(n<=4)3 else 2
    repeat(cantidad) { j -> ar.save(Labor(loteId=l.id,tipo=if(n!=2 && j==1)TipoLabor.COSECHA else TipoLabor.RIEGO,fecha=LocalDate.now().minusDays(j.toLong()).toString(),responsable=u.username,usuarioId=u.id,cantidad=1.0,observacion="Registro inicial de campaña")) }
   }
  }
 }
}
