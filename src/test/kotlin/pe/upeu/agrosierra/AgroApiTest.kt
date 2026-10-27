package pe.upeu.agrosierra
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.http.MediaType
import com.fasterxml.jackson.databind.ObjectMapper
import pe.upeu.agrosierra.repositorio.*
import java.time.Instant
@SpringBootTest(properties=["spring.datasource.url=jdbc:h2:mem:pruebas;DB_CLOSE_DELAY=-1","spring.jpa.hibernate.ddl-auto=create-drop"])
@AutoConfigureMockMvc @TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class AgroApiTest {
 @Autowired lateinit var mvc:MockMvc
 @Autowired lateinit var json:ObjectMapper
 @Autowired lateinit var labores:LaborRepository
 @Autowired lateinit var lotes:LoteRepository
 private fun token(u:String="tecnico1"):String {
  val r=mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("""{"username":"$u","password":"Agro2026*"}""")).andExpect(status().isOk).andReturn()
  return json.readTree(r.response.contentAsString)["token"].asText()
 }
 private fun body(lote:Int=1,tipo:String="RIEGO",cantidad:Double=1.0)="""{"loteId":$lote,"tipo":"$tipo","fecha":"2026-10-27","cantidad":$cantidad,"observacion":"Prueba"}"""
 @Test @Order(1) fun `semillas y paginacion`() {
  assertEquals(8,lotes.count());assertEquals(20,labores.count())
  mvc.perform(get("/api/v1/lotes?tamanio=3&pagina=0").header("Authorization","Bearer ${token()}"))
   .andExpect(status().isOk).andExpect(jsonPath("$.contenido.length()").value(3)).andExpect(jsonPath("$.totalElementos").value(8))
 }
 @Test fun `credenciales incorrectas no autentican`() {mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("""{"username":"admin","password":"incorrecta"}""")).andExpect(status().isUnauthorized)}
 @Test fun `token ausente y alterado son 401`() {
  mvc.perform(get("/api/v1/lotes")).andExpect(status().isUnauthorized)
  mvc.perform(get("/api/v1/lotes").header("Authorization","Bearer alterado")).andExpect(status().isUnauthorized)
 }
 @Test fun `administrador no registra y tecnico no elimina`() {
  mvc.perform(post("/api/v1/labores").header("Authorization","Bearer ${token("admin")}").contentType(MediaType.APPLICATION_JSON).content(body())).andExpect(status().isForbidden)
  mvc.perform(delete("/api/v1/labores/1").header("Authorization","Bearer ${token()}")).andExpect(status().isForbidden)
 }
 @Test fun `RN01 campania cerrada`() {mvc.perform(post("/api/v1/labores").header("Authorization","Bearer ${token()}").contentType(MediaType.APPLICATION_JSON).content(body(7))).andExpect(status().isConflict)}
 @Test fun `RN04 dosis maxima con mensaje por campo`() {mvc.perform(post("/api/v1/labores").header("Authorization","Bearer ${token()}").contentType(MediaType.APPLICATION_JSON).content(body(1,"FUMIGACION",999.0))).andExpect(status().isBadRequest).andExpect(jsonPath("$.validationErrors.cantidad").exists())}
 @Test fun `RN03 sin cosecha no cierra`() {mvc.perform(post("/api/v1/lotes/2/cerrar-campania").header("Authorization","Bearer ${token("jefe1")}")).andExpect(status().isConflict)}
 @Test fun `RN02 tecnico no edita labor antigua`() {
  val l=labores.findById(1).orElseThrow();val antes=l.registradoEn;l.registradoEn=Instant.now().minusSeconds(90000);labores.save(l)
  try {mvc.perform(put("/api/v1/labores/1").header("Authorization","Bearer ${token()}").contentType(MediaType.APPLICATION_JSON).content(body())).andExpect(status().isForbidden)}
  finally {l.registradoEn=antes;labores.save(l)}
 }
 @Test fun `crear idempotente editar y eliminar 204`() {
  val t=token("jefe1");val clave=java.util.UUID.randomUUID().toString()
  fun crear()=mvc.perform(post("/api/v1/labores").header("Authorization","Bearer $t").header("Idempotency-Key",clave).contentType(MediaType.APPLICATION_JSON).content(body())).andExpect(status().isCreated).andReturn()
  val id=json.readTree(crear().response.contentAsString)["id"].asLong()
  assertEquals(id,json.readTree(crear().response.contentAsString)["id"].asLong())
  mvc.perform(put("/api/v1/labores/$id").header("Authorization","Bearer $t").contentType(MediaType.APPLICATION_JSON).content(body(cantidad=2.0))).andExpect(status().isOk).andExpect(jsonPath("$.cantidad").value(2.0))
  mvc.perform(delete("/api/v1/labores/$id").header("Authorization","Bearer $t")).andExpect(status().isNoContent).andExpect(content().string(""))
 }
}
