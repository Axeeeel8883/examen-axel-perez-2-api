package pe.upeu.agrosierra.seguridad
import org.springframework.web.bind.annotation.*
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.access.prepost.PreAuthorize
import pe.upeu.agrosierra.config.*
import pe.upeu.agrosierra.web.dto.*
@RestController @RequestMapping("/api/v1")
class AuthController(val usuarios:UsuarioRepository,val roles:RolRepository,val encoder:BCryptPasswordEncoder,val jwt:JwtService) {
 @PostMapping("/auth/login") fun login(@RequestBody c:Credenciales):Map<String,Any> {
  val u=usuarios.findByUsername(c.username)
  if(u==null || !u.activo || !encoder.matches(c.password,u.password)) throw Fallo(401,"Usuario o contraseña incorrectos")
  return datos(u)+mapOf("token" to jwt.emitir(u),"expiraEnSegundos" to 3600)
 }
 @GetMapping("/usuarios/me") fun me(a:Authentication)=datos(usuarios.findById(a.name.toLong()).orElseThrow())
 @PostMapping("/usuarios") @PreAuthorize("hasAuthority('USUARIO_GESTIONAR')")
 @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
 fun crear(@RequestBody r:UsuarioRequest):Map<String,Any> {
  if(r.username.isBlank() || r.password.length<8 || r.nombre.isBlank()) throw Fallo(400,"Datos inválidos",mapOf("password" to "Mínimo 8 caracteres; nombre y usuario obligatorios"))
  if(usuarios.findByUsername(r.username)!=null) throw Fallo(409,"El usuario ya existe")
  val rol=roles.findById(r.rol).orElseThrow{Fallo(400,"Rol inválido")}
  return datos(usuarios.save(Usuario(username=r.username,password=encoder.encode(r.password),nombre=r.nombre,rol=rol)))
 }
 private fun datos(u:Usuario):Map<String,Any> = mapOf("id" to u.id,"usuario" to u.username,"nombre" to u.nombre,"rol" to u.rol.nombre,"privilegios" to u.rol.privilegios.map{it.nombre},"edicionGlobal" to u.rol.edicionGlobal)
}
