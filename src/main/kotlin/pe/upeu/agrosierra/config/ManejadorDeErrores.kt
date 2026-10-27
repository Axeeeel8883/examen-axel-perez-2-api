package pe.upeu.agrosierra.config
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.security.access.AccessDeniedException
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.time.LocalDateTime
class Fallo(val codigo:Int,override val message:String,val campos:Map<String,String> = emptyMap()):RuntimeException(message)
data class ErrorDto(val timestamp:String=LocalDateTime.now().toString(),val status:Int,
 val error:String,val message:String,val path:String,val validationErrors:Map<String,String> = emptyMap())
fun error(codigo:Int,mensaje:String,ruta:String,campos:Map<String,String> = emptyMap())=
 ErrorDto(status=codigo,error=HttpStatus.valueOf(codigo).reasonPhrase,message=mensaje,path=ruta,validationErrors=campos)
@RestControllerAdvice class ManejadorDeErrores {
 @ExceptionHandler(Fallo::class) fun fallo(e:Fallo,r:HttpServletRequest)=ResponseEntity.status(e.codigo).body(error(e.codigo,e.message,r.requestURI,e.campos))
 @ExceptionHandler(AccessDeniedException::class) fun acceso(e:AccessDeniedException,r:HttpServletRequest)=ResponseEntity.status(403).body(error(403,"No tiene el privilegio requerido",r.requestURI))
 @ExceptionHandler(HttpMessageNotReadableException::class,MethodArgumentTypeMismatchException::class)
 fun formato(e:Exception,r:HttpServletRequest)=ResponseEntity.badRequest().body(error(400,"Formato de petición inválido",r.requestURI))
 @ExceptionHandler(Exception::class) fun otro(e:Exception,r:HttpServletRequest):ResponseEntity<ErrorDto> {
  org.slf4j.LoggerFactory.getLogger(javaClass).error("Error interno en {}",r.requestURI,e)
  return ResponseEntity.internalServerError().body(error(500,"No se pudo completar la operación",r.requestURI))
 }
}
