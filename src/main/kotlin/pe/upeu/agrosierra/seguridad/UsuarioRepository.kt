package pe.upeu.agrosierra.seguridad
import org.springframework.data.jpa.repository.JpaRepository
interface UsuarioRepository: JpaRepository<Usuario,Long> { fun findByUsername(username:String): Usuario? }
interface RolRepository: JpaRepository<Rol,String>
interface PrivilegioRepository: JpaRepository<Privilegio,String>
