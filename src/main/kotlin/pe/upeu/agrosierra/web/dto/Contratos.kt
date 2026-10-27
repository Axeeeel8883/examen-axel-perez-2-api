package pe.upeu.agrosierra.web.dto
import org.springframework.data.domain.Page
data class Pagina<T>(val contenido:List<T>, val pagina:Int, val tamanio:Int,
 val totalElementos:Long,val totalPaginas:Int,val ultima:Boolean)
fun <T> Page<T>.dto()=Pagina(content,number,size,totalElements,totalPages,isLast)
data class Credenciales(val username:String="",val password:String="")
data class LaborRequest(val loteId:Long=0,val tipo:String="",val fecha:String="",
 val cantidad:Double=0.0,val observacion:String="",val responsable:String="")
data class UsuarioRequest(val username:String="",val password:String="",val nombre:String="",val rol:String="")
