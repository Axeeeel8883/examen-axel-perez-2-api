package pe.upeu.agrosierra.seguridad
import jakarta.persistence.*
@Entity class Privilegio(@Id var nombre: String = "")
@Entity class Rol(
 @Id var nombre: String = "",
 @ManyToMany(fetch=FetchType.EAGER) var privilegios: MutableSet<Privilegio> = mutableSetOf(),
 var edicionGlobal: Boolean = false
)
@Entity class Usuario(
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) var id: Long = 0,
 @Column(unique=true) var username: String = "",
 var password: String = "",
 var nombre: String = "",
 @ManyToOne(fetch=FetchType.EAGER) var rol: Rol = Rol(),
 var activo: Boolean = true
)
