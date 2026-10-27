package pe.upeu.agrosierra.config
import jakarta.servlet.*
import jakarta.servlet.http.*
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.stereotype.Component
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import pe.upeu.agrosierra.seguridad.UsuarioRepository
import com.fasterxml.jackson.databind.ObjectMapper
@Component class JwtAuthenticationFilter(val jwt:JwtService,val usuarios:UsuarioRepository,val json:ObjectMapper):OncePerRequestFilter() {
 override fun shouldNotFilter(r:HttpServletRequest)=r.requestURI.removePrefix(r.contextPath)=="/api/v1/auth/login"
 override fun doFilterInternal(r:HttpServletRequest,s:HttpServletResponse,c:FilterChain) {
  val h=r.getHeader("Authorization")
  try {
   if(h==null || !h.startsWith("Bearer ")) throw IllegalArgumentException()
   val u=usuarios.findById(jwt.verificar(h.substring(7))).orElseThrow()
   if(!u.activo) throw IllegalArgumentException()
   SecurityContextHolder.getContext().authentication=UsernamePasswordAuthenticationToken(u.id.toString(),null,u.rol.privilegios.map{SimpleGrantedAuthority(it.nombre)})
  } catch(e:Exception) {
   SecurityContextHolder.clearContext();s.status=401;s.contentType="application/json";s.characterEncoding="UTF-8"
   json.writeValue(s.writer,error(401,"Sesión ausente, inválida o vencida",r.requestURI));return
  }
  c.doFilter(r,s)
 }
}

