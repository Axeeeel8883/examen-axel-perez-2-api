package pe.upeu.agrosierra.config
import org.springframework.context.annotation.*
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import com.fasterxml.jackson.databind.ObjectMapper
@Configuration @EnableMethodSecurity
class SecurityConfig {
 @Bean fun passwordEncoder()=BCryptPasswordEncoder()
 @Bean fun cadena(http:HttpSecurity,filtro:JwtAuthenticationFilter,json:ObjectMapper):SecurityFilterChain {
  http.csrf{it.disable()}.sessionManagement{it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)}
   .authorizeHttpRequests{it.requestMatchers("/api/v1/auth/login").permitAll().anyRequest().authenticated()}
   .exceptionHandling { e ->
    e.authenticationEntryPoint{r,s,_ -> s.status=401;s.contentType="application/json";json.writeValue(s.writer,error(401,"Debe iniciar sesión",r.requestURI))}
    e.accessDeniedHandler{r,s,_ -> s.status=403;s.contentType="application/json";json.writeValue(s.writer,error(403,"No tiene el privilegio requerido",r.requestURI))}
   }.addFilterBefore(filtro,UsernamePasswordAuthenticationFilter::class.java)
  return http.build()
 }
}
