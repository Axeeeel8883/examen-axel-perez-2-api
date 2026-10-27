package pe.upeu.agrosierra.config
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import pe.upeu.agrosierra.seguridad.Usuario
import java.util.Date
import java.nio.file.*
import java.util.Base64
@Service class JwtService {
 // Clave persistente fuera del código; JWT_SECRET permite administrarla en producción.
 private val clave=Keys.hmacShaKeyFor(run {
  val externa=System.getenv("JWT_SECRET")
  if(!externa.isNullOrBlank()) Base64.getDecoder().decode(externa) else {
   val archivo=Path.of("data","jwt.key"); Files.createDirectories(archivo.parent)
   if(!Files.exists(archivo)) {
    val bytes=ByteArray(64); java.security.SecureRandom().nextBytes(bytes)
    Files.writeString(archivo,Base64.getEncoder().encodeToString(bytes),StandardOpenOption.CREATE_NEW)
   }
   Base64.getDecoder().decode(Files.readString(archivo).trim())
  }
 })
 fun emitir(u:Usuario)=Jwts.builder().subject(u.id.toString())
  .claim("privilegios",u.rol.privilegios.map{it.nombre}).issuedAt(Date())
  .expiration(Date(System.currentTimeMillis()+3600000)).signWith(clave,Jwts.SIG.HS512).compact()
 fun verificar(t:String):Long=Jwts.parser().verifyWith(clave).build().parseSignedClaims(t).payload.subject.toLong()
}
