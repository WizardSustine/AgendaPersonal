package com.example.agendapersonal.ui.registro;
import android.content.Context;
import com.example.agendapersonal.model.*;
import com.example.agendapersonal.repository.*;
import java.util.ArrayList;
import java.util.List;
/** Adaptadores tipados: cada escritura usa los repositorios existentes y su registro de cambios. */
public final class Fuentes {
 private Fuentes() {}
 public static Fuente abrir(Context context, String deviceId, String tabla) {
 switch(tabla) { case "personas": { final PersonaRepository repo = new PersonaRepository(context, deviceId);
 return new Fuente() {
 public List<Registro> todos() { List<Registro> resultado = new ArrayList<>(); for (Persona m : repo.obtenerTodos()) { Registro r = new Registro(); r.poner("id", m.getId());
r.poner("ownerId", m.getOwnerId());
r.poner("nombre", m.getNombre());
r.poner("otroDato", m.getOtroDato());
r.poner("createdAt", m.getCreatedAt());
r.poner("updatedAt", m.getUpdatedAt());
r.poner("revision", m.getRevision());
r.poner("deletedAt", m.getDeletedAt()); resultado.add(r); } return resultado; }
 public boolean guardar(Registro r, boolean nuevo) { Persona m = new Persona(); m.setId(r.get("id"));
m.setOwnerId(r.get("ownerId"));
m.setNombre(r.get("nombre"));
m.setOtroDato(r.get("otroDato"));
m.setCreatedAt(r.get("createdAt"));
m.setUpdatedAt(r.get("updatedAt"));
m.setRevision(r.texto("revision").isEmpty() ? 1 : Integer.parseInt(r.texto("revision")));
m.setDeletedAt(r.get("deletedAt")); boolean ok = nuevo ? repo.insertar(m) : repo.actualizar(m); if(ok){r.poner("id",m.getId());com.example.agendapersonal.eventos.SeriesEvento.cambios(context);} return ok; }
 public boolean eliminar(String id) { boolean ok=repo.eliminar(id);if(ok)com.example.agendapersonal.eventos.SeriesEvento.cambios(context);return ok; }
 public void close() { repo.cerrar(); }
 }; }
case "direcciones": { final DireccionRepository repo = new DireccionRepository(context, deviceId);
 return new Fuente() {
 public List<Registro> todos() { List<Registro> resultado = new ArrayList<>(); for (Direccion m : repo.obtenerTodos()) { Registro r = new Registro(); r.poner("id", m.getId());
r.poner("ownerId", m.getOwnerId());
r.poner("personaId", m.getPersonaId());
r.poner("calle", m.getCalle());
r.poner("numero", m.getNumero());
r.poner("localidad", m.getLocalidad());
r.poner("provincia", m.getProvincia());
r.poner("referencia", m.getReferencia());
r.poner("createdAt", m.getCreatedAt());
r.poner("updatedAt", m.getUpdatedAt());
r.poner("revision", m.getRevision());
r.poner("deletedAt", m.getDeletedAt()); resultado.add(r); } return resultado; }
 public boolean guardar(Registro r, boolean nuevo) { Direccion m = new Direccion(); m.setId(r.get("id"));
m.setOwnerId(r.get("ownerId"));
m.setPersonaId(r.get("personaId"));
m.setCalle(r.get("calle"));
m.setNumero(r.get("numero"));
m.setLocalidad(r.get("localidad"));
m.setProvincia(r.get("provincia"));
m.setReferencia(r.get("referencia"));
m.setCreatedAt(r.get("createdAt"));
m.setUpdatedAt(r.get("updatedAt"));
m.setRevision(r.texto("revision").isEmpty() ? 1 : Integer.parseInt(r.texto("revision")));
m.setDeletedAt(r.get("deletedAt")); boolean ok = nuevo ? repo.insertar(m) : repo.actualizar(m); if(ok){r.poner("id",m.getId());com.example.agendapersonal.eventos.SeriesEvento.cambios(context);} return ok; }
 public boolean eliminar(String id) { boolean ok=repo.eliminar(id);if(ok)com.example.agendapersonal.eventos.SeriesEvento.cambios(context);return ok; }
 public void close() { repo.cerrar(); }
 }; }
case "materias": { final MateriaRepository repo = new MateriaRepository(context, deviceId);
 return new Fuente() {
 public List<Registro> todos() { List<Registro> resultado = new ArrayList<>(); for (Materia m : repo.obtenerTodos()) { Registro r = new Registro(); r.poner("id", m.getId());
r.poner("ownerId", m.getOwnerId());
r.poner("institucionId", m.getInstitucionId());
r.poner("nombre", m.getNombre());
r.poner("createdAt", m.getCreatedAt());
r.poner("updatedAt", m.getUpdatedAt());
r.poner("revision", m.getRevision());
r.poner("deletedAt", m.getDeletedAt()); resultado.add(r); } return resultado; }
 public boolean guardar(Registro r, boolean nuevo) { Materia m = new Materia(); m.setId(r.get("id"));
m.setOwnerId(r.get("ownerId"));
m.setInstitucionId(r.get("institucionId"));
m.setNombre(r.get("nombre"));
m.setCreatedAt(r.get("createdAt"));
m.setUpdatedAt(r.get("updatedAt"));
m.setRevision(r.texto("revision").isEmpty() ? 1 : Integer.parseInt(r.texto("revision")));
m.setDeletedAt(r.get("deletedAt")); boolean ok = nuevo ? repo.insertar(m) : repo.actualizar(m); if(ok){r.poner("id",m.getId());com.example.agendapersonal.eventos.SeriesEvento.cambios(context);} return ok; }
 public boolean eliminar(String id) { boolean ok=repo.eliminar(id);if(ok)com.example.agendapersonal.eventos.SeriesEvento.cambios(context);return ok; }
 public void close() { repo.cerrar(); }
 }; }
case "estudios": { final EstudioRepository repo = new EstudioRepository(context, deviceId);
 return new Fuente() {
 public List<Registro> todos() { List<Registro> resultado = new ArrayList<>(); for (Estudio m : repo.obtenerTodos()) { Registro r = new Registro(); r.poner("id", m.getId());
r.poner("ownerId", m.getOwnerId());
r.poner("fechaEntrega", m.getFechaEntrega());
r.poner("hora", m.getHora());
r.poner("actividad", m.getActividad());
r.poner("materiaId", m.getMateriaId());
r.poner("institucionId", m.getInstitucionId());
r.poner("prioridad", m.getPrioridad());
r.poner("estadoEspecial", m.getEstadoEspecial());
r.poner("createdAt", m.getCreatedAt());
r.poner("updatedAt", m.getUpdatedAt());
r.poner("revision", m.getRevision());
r.poner("deletedAt", m.getDeletedAt()); resultado.add(r); } return resultado; }
 public boolean guardar(Registro r, boolean nuevo) { Estudio m = new Estudio(); m.setId(r.get("id"));
m.setOwnerId(r.get("ownerId"));
m.setFechaEntrega(r.get("fechaEntrega"));
m.setHora(r.get("hora"));
m.setActividad(r.get("actividad"));
m.setMateriaId(r.get("materiaId"));
m.setInstitucionId(r.get("institucionId"));
m.setPrioridad(r.get("prioridad"));
m.setEstadoEspecial(r.get("estadoEspecial"));
m.setCreatedAt(r.get("createdAt"));
m.setUpdatedAt(r.get("updatedAt"));
m.setRevision(r.texto("revision").isEmpty() ? 1 : Integer.parseInt(r.texto("revision")));
m.setDeletedAt(r.get("deletedAt")); boolean ok = nuevo ? repo.insertar(m) : repo.actualizar(m); if(ok){r.poner("id",m.getId());com.example.agendapersonal.eventos.SeriesEvento.cambios(context);} return ok; }
 public boolean eliminar(String id) { boolean ok=repo.eliminar(id);if(ok)com.example.agendapersonal.eventos.SeriesEvento.cambios(context);return ok; }
 public void close() { repo.cerrar(); }
 }; }
case "clasificaciones_sociales": { final ClasificacionSocialRepository repo = new ClasificacionSocialRepository(context, deviceId);
 return new Fuente() {
 public List<Registro> todos() { List<Registro> resultado = new ArrayList<>(); for (ClasificacionSocial m : repo.obtenerTodos()) { Registro r = new Registro(); r.poner("id", m.getId());
r.poner("ownerId", m.getOwnerId());
r.poner("nombre", m.getNombre());
r.poner("createdAt", m.getCreatedAt());
r.poner("updatedAt", m.getUpdatedAt());
r.poner("revision", m.getRevision());
r.poner("deletedAt", m.getDeletedAt()); resultado.add(r); } return resultado; }
 public boolean guardar(Registro r, boolean nuevo) { ClasificacionSocial m = new ClasificacionSocial(); m.setId(r.get("id"));
m.setOwnerId(r.get("ownerId"));
m.setNombre(r.get("nombre"));
m.setCreatedAt(r.get("createdAt"));
m.setUpdatedAt(r.get("updatedAt"));
m.setRevision(r.texto("revision").isEmpty() ? 1 : Integer.parseInt(r.texto("revision")));
m.setDeletedAt(r.get("deletedAt")); boolean ok = nuevo ? repo.insertar(m) : repo.actualizar(m); if(ok){r.poner("id",m.getId());com.example.agendapersonal.eventos.SeriesEvento.cambios(context);} return ok; }
 public boolean eliminar(String id) { boolean ok=repo.eliminar(id);if(ok)com.example.agendapersonal.eventos.SeriesEvento.cambios(context);return ok; }
 public void close() { repo.cerrar(); }
 }; }
case "sociales": { final SocialRepository repo = new SocialRepository(context, deviceId);
 return new Fuente() {
 public List<Registro> todos() { List<Registro> resultado = new ArrayList<>(); for (Social m : repo.obtenerTodos()) { Registro r = new Registro(); r.poner("id", m.getId());
r.poner("ownerId", m.getOwnerId());
r.poner("fecha", m.getFecha());
r.poner("hora", m.getHora());
r.poner("actividad", m.getActividad());
r.poner("personaId", m.getPersonaId());
r.poner("clasificacionId", m.getClasificacionId());
r.poner("prioridad", m.getPrioridad());
r.poner("estadoEspecial", m.getEstadoEspecial());
r.poner("createdAt", m.getCreatedAt());
r.poner("updatedAt", m.getUpdatedAt());
r.poner("revision", m.getRevision());
r.poner("deletedAt", m.getDeletedAt()); resultado.add(r); } return resultado; }
 public boolean guardar(Registro r, boolean nuevo) { Social m = new Social(); m.setId(r.get("id"));
m.setOwnerId(r.get("ownerId"));
m.setFecha(r.get("fecha"));
m.setHora(r.get("hora"));
m.setActividad(r.get("actividad"));
m.setPersonaId(r.get("personaId"));
m.setClasificacionId(r.get("clasificacionId"));
m.setPrioridad(r.get("prioridad"));
m.setEstadoEspecial(r.get("estadoEspecial"));
m.setCreatedAt(r.get("createdAt"));
m.setUpdatedAt(r.get("updatedAt"));
m.setRevision(r.texto("revision").isEmpty() ? 1 : Integer.parseInt(r.texto("revision")));
m.setDeletedAt(r.get("deletedAt")); boolean ok = nuevo ? repo.insertar(m) : repo.actualizar(m); if(ok){r.poner("id",m.getId());com.example.agendapersonal.eventos.SeriesEvento.cambios(context);} return ok; }
 public boolean eliminar(String id) { boolean ok=repo.eliminar(id);if(ok)com.example.agendapersonal.eventos.SeriesEvento.cambios(context);return ok; }
 public void close() { repo.cerrar(); }
 }; }
case "especialidades": { final EspecialidadRepository repo = new EspecialidadRepository(context, deviceId);
 return new Fuente() {
 public List<Registro> todos() { List<Registro> resultado = new ArrayList<>(); for (Especialidad m : repo.obtenerTodos()) { Registro r = new Registro(); r.poner("id", m.getId());
r.poner("ownerId", m.getOwnerId());
r.poner("nombre", m.getNombre());
r.poner("createdAt", m.getCreatedAt());
r.poner("updatedAt", m.getUpdatedAt());
r.poner("revision", m.getRevision());
r.poner("deletedAt", m.getDeletedAt()); resultado.add(r); } return resultado; }
 public boolean guardar(Registro r, boolean nuevo) { Especialidad m = new Especialidad(); m.setId(r.get("id"));
m.setOwnerId(r.get("ownerId"));
m.setNombre(r.get("nombre"));
m.setCreatedAt(r.get("createdAt"));
m.setUpdatedAt(r.get("updatedAt"));
m.setRevision(r.texto("revision").isEmpty() ? 1 : Integer.parseInt(r.texto("revision")));
m.setDeletedAt(r.get("deletedAt")); boolean ok = nuevo ? repo.insertar(m) : repo.actualizar(m); if(ok){r.poner("id",m.getId());com.example.agendapersonal.eventos.SeriesEvento.cambios(context);} return ok; }
 public boolean eliminar(String id) { boolean ok=repo.eliminar(id);if(ok)com.example.agendapersonal.eventos.SeriesEvento.cambios(context);return ok; }
 public void close() { repo.cerrar(); }
 }; }
case "salud": { final SaludRepository repo = new SaludRepository(context, deviceId);
 return new Fuente() {
 public List<Registro> todos() { List<Registro> resultado = new ArrayList<>(); for (Salud m : repo.obtenerTodos()) { Registro r = new Registro(); r.poner("id", m.getId());
r.poner("ownerId", m.getOwnerId());
r.poner("fecha", m.getFecha());
r.poner("hora", m.getHora());
r.poner("actividad", m.getActividad());
r.poner("especialidadId", m.getEspecialidadId());
r.poner("institucionId", m.getInstitucionId());
r.poner("prioridad", m.getPrioridad());
r.poner("estadoEspecial", m.getEstadoEspecial());
r.poner("createdAt", m.getCreatedAt());
r.poner("updatedAt", m.getUpdatedAt());
r.poner("revision", m.getRevision());
r.poner("deletedAt", m.getDeletedAt()); resultado.add(r); } return resultado; }
 public boolean guardar(Registro r, boolean nuevo) { Salud m = new Salud(); m.setId(r.get("id"));
m.setOwnerId(r.get("ownerId"));
m.setFecha(r.get("fecha"));
m.setHora(r.get("hora"));
m.setActividad(r.get("actividad"));
m.setEspecialidadId(r.get("especialidadId"));
m.setInstitucionId(r.get("institucionId"));
m.setPrioridad(r.get("prioridad"));
m.setEstadoEspecial(r.get("estadoEspecial"));
m.setCreatedAt(r.get("createdAt"));
m.setUpdatedAt(r.get("updatedAt"));
m.setRevision(r.texto("revision").isEmpty() ? 1 : Integer.parseInt(r.texto("revision")));
m.setDeletedAt(r.get("deletedAt")); boolean ok = nuevo ? repo.insertar(m) : repo.actualizar(m); if(ok){r.poner("id",m.getId());com.example.agendapersonal.eventos.SeriesEvento.cambios(context);} return ok; }
 public boolean eliminar(String id) { boolean ok=repo.eliminar(id);if(ok)com.example.agendapersonal.eventos.SeriesEvento.cambios(context);return ok; }
 public void close() { repo.cerrar(); }
 }; }
case "trabajo": { final TrabajoRepository repo = new TrabajoRepository(context, deviceId);
 return new Fuente() {
 public List<Registro> todos() { List<Registro> resultado = new ArrayList<>(); for (Trabajo m : repo.obtenerTodos()) { Registro r = new Registro(); r.poner("id", m.getId());
r.poner("ownerId", m.getOwnerId());
r.poner("fechaEntrega", m.getFechaEntrega());
r.poner("hora", m.getHora());
r.poner("actividad", m.getActividad());
r.poner("asunto", m.getAsunto());
r.poner("clasificacion", m.getClasificacion());
r.poner("personaId", m.getPersonaId());
r.poner("prioridad", m.getPrioridad());
r.poner("estadoEspecial", m.getEstadoEspecial());
r.poner("createdAt", m.getCreatedAt());
r.poner("updatedAt", m.getUpdatedAt());
r.poner("revision", m.getRevision());
r.poner("deletedAt", m.getDeletedAt()); resultado.add(r); } return resultado; }
 public boolean guardar(Registro r, boolean nuevo) { Trabajo m = new Trabajo(); m.setId(r.get("id"));
m.setOwnerId(r.get("ownerId"));
m.setFechaEntrega(r.get("fechaEntrega"));
m.setHora(r.get("hora"));
m.setActividad(r.get("actividad"));
m.setAsunto(r.get("asunto"));
m.setClasificacion(r.get("clasificacion"));
m.setPersonaId(r.get("personaId"));
m.setPrioridad(r.get("prioridad"));
m.setEstadoEspecial(r.get("estadoEspecial"));
m.setCreatedAt(r.get("createdAt"));
m.setUpdatedAt(r.get("updatedAt"));
m.setRevision(r.texto("revision").isEmpty() ? 1 : Integer.parseInt(r.texto("revision")));
m.setDeletedAt(r.get("deletedAt")); boolean ok = nuevo ? repo.insertar(m) : repo.actualizar(m); if(ok){r.poner("id",m.getId());com.example.agendapersonal.eventos.SeriesEvento.cambios(context);} return ok; }
 public boolean eliminar(String id) { boolean ok=repo.eliminar(id);if(ok)com.example.agendapersonal.eventos.SeriesEvento.cambios(context);return ok; }
 public void close() { repo.cerrar(); }
 }; }
case "freelance": { final FreelanceRepository repo = new FreelanceRepository(context, deviceId);
 return new Fuente() {
 public List<Registro> todos() { List<Registro> resultado = new ArrayList<>(); for (Freelance m : repo.obtenerTodos()) { Registro r = new Registro(); r.poner("id", m.getId());
r.poner("ownerId", m.getOwnerId());
r.poner("valor", m.getValor());
r.poner("fechaInicio", m.getFechaInicio());
r.poner("horaInicio", m.getHoraInicio());
r.poner("fechaFinalizacion", m.getFechaFinalizacion());
r.poner("horaFinalizacion", m.getHoraFinalizacion());
r.poner("actividad", m.getActividad());
r.poner("clienteId", m.getClienteId());
r.poner("direccionId", m.getDireccionId());
r.poner("clasificacion", m.getClasificacion());
r.poner("estadoEspecial", m.getEstadoEspecial());
r.poner("observacion", m.getObservacion());
r.poner("createdAt", m.getCreatedAt());
r.poner("updatedAt", m.getUpdatedAt());
r.poner("revision", m.getRevision());
r.poner("deletedAt", m.getDeletedAt()); resultado.add(r); } return resultado; }
 public boolean guardar(Registro r, boolean nuevo) { Freelance m = new Freelance(); m.setId(r.get("id"));
m.setOwnerId(r.get("ownerId"));
m.setValor(r.texto("valor").isEmpty() ? null : Double.valueOf(r.texto("valor")));
m.setFechaInicio(r.get("fechaInicio"));
m.setHoraInicio(r.get("horaInicio"));
m.setFechaFinalizacion(r.get("fechaFinalizacion"));
m.setHoraFinalizacion(r.get("horaFinalizacion"));
m.setActividad(r.get("actividad"));
m.setClienteId(r.get("clienteId"));
m.setDireccionId(r.get("direccionId"));
m.setClasificacion(r.get("clasificacion"));
m.setEstadoEspecial(r.get("estadoEspecial"));
m.setObservacion(r.get("observacion"));
m.setCreatedAt(r.get("createdAt"));
m.setUpdatedAt(r.get("updatedAt"));
m.setRevision(r.texto("revision").isEmpty() ? 1 : Integer.parseInt(r.texto("revision")));
m.setDeletedAt(r.get("deletedAt")); boolean ok = nuevo ? repo.insertar(m) : repo.actualizar(m); if(ok){r.poner("id",m.getId());com.example.agendapersonal.eventos.SeriesEvento.cambios(context);} return ok; }
 public boolean eliminar(String id) { boolean ok=repo.eliminar(id);if(ok)com.example.agendapersonal.eventos.SeriesEvento.cambios(context);return ok; }
 public void close() { repo.cerrar(); }
 }; }
case "clasificaciones_economia": { final ClasificacionEconomiaRepository repo = new ClasificacionEconomiaRepository(context, deviceId);
 return new Fuente() {
 public List<Registro> todos() { List<Registro> resultado = new ArrayList<>(); for (ClasificacionEconomia m : repo.obtenerTodos()) { Registro r = new Registro(); r.poner("id", m.getId());
r.poner("ownerId", m.getOwnerId());
r.poner("nombre", m.getNombre());
r.poner("createdAt", m.getCreatedAt());
r.poner("updatedAt", m.getUpdatedAt());
r.poner("revision", m.getRevision());
r.poner("deletedAt", m.getDeletedAt()); resultado.add(r); } return resultado; }
 public boolean guardar(Registro r, boolean nuevo) { ClasificacionEconomia m = new ClasificacionEconomia(); m.setId(r.get("id"));
m.setOwnerId(r.get("ownerId"));
m.setNombre(r.get("nombre"));
m.setCreatedAt(r.get("createdAt"));
m.setUpdatedAt(r.get("updatedAt"));
m.setRevision(r.texto("revision").isEmpty() ? 1 : Integer.parseInt(r.texto("revision")));
m.setDeletedAt(r.get("deletedAt")); boolean ok = nuevo ? repo.insertar(m) : repo.actualizar(m); if(ok){r.poner("id",m.getId());com.example.agendapersonal.eventos.SeriesEvento.cambios(context);} return ok; }
 public boolean eliminar(String id) { boolean ok=repo.eliminar(id);if(ok)com.example.agendapersonal.eventos.SeriesEvento.cambios(context);return ok; }
 public void close() { repo.cerrar(); }
 }; }
case "ingresos": { final IngresoRepository repo = new IngresoRepository(context, deviceId);
 return new Fuente() {
 public List<Registro> todos() { List<Registro> resultado = new ArrayList<>(); for (Ingreso m : repo.obtenerTodos()) { Registro r = new Registro(); r.poner("id", m.getId());
r.poner("ownerId", m.getOwnerId());
r.poner("fecha", m.getFecha());
r.poner("descripcion", m.getDescripcion());
r.poner("clasificacionId", m.getClasificacionId());
r.poner("importe", m.getImporte());
r.poner("estadoEspecial", m.getEstadoEspecial());
r.poner("createdAt", m.getCreatedAt());
r.poner("updatedAt", m.getUpdatedAt());
r.poner("revision", m.getRevision());
r.poner("deletedAt", m.getDeletedAt()); resultado.add(r); } return resultado; }
 public boolean guardar(Registro r, boolean nuevo) { Ingreso m = new Ingreso(); m.setId(r.get("id"));
m.setOwnerId(r.get("ownerId"));
m.setFecha(r.get("fecha"));
m.setDescripcion(r.get("descripcion"));
m.setClasificacionId(r.get("clasificacionId"));
m.setImporte(r.texto("importe").isEmpty() ? null : Double.valueOf(r.texto("importe")));
m.setEstadoEspecial(r.get("estadoEspecial"));
m.setCreatedAt(r.get("createdAt"));
m.setUpdatedAt(r.get("updatedAt"));
m.setRevision(r.texto("revision").isEmpty() ? 1 : Integer.parseInt(r.texto("revision")));
m.setDeletedAt(r.get("deletedAt")); boolean ok = nuevo ? repo.insertar(m) : repo.actualizar(m); if(ok){r.poner("id",m.getId());com.example.agendapersonal.eventos.SeriesEvento.cambios(context);} return ok; }
 public boolean eliminar(String id) { boolean ok=repo.eliminar(id);if(ok)com.example.agendapersonal.eventos.SeriesEvento.cambios(context);return ok; }
 public void close() { repo.cerrar(); }
 }; }
case "pagos": { final PagoRepository repo = new PagoRepository(context, deviceId);
 return new Fuente() {
 public List<Registro> todos() { List<Registro> resultado = new ArrayList<>(); for (Pago m : repo.obtenerTodos()) { Registro r = new Registro(); r.poner("id", m.getId());
r.poner("ownerId", m.getOwnerId());
r.poner("fecha", m.getFecha());
r.poner("descripcion", m.getDescripcion());
r.poner("clasificacionId", m.getClasificacionId());
r.poner("importe", m.getImporte());
r.poner("periodicidad", m.getPeriodicidad());
r.poner("estadoEspecial", m.getEstadoEspecial());
r.poner("createdAt", m.getCreatedAt());
r.poner("updatedAt", m.getUpdatedAt());
r.poner("revision", m.getRevision());
r.poner("deletedAt", m.getDeletedAt()); resultado.add(r); } return resultado; }
 public boolean guardar(Registro r, boolean nuevo) { Pago m = new Pago(); m.setId(r.get("id"));
m.setOwnerId(r.get("ownerId"));
m.setFecha(r.get("fecha"));
m.setDescripcion(r.get("descripcion"));
m.setClasificacionId(r.get("clasificacionId"));
m.setImporte(r.texto("importe").isEmpty() ? null : Double.valueOf(r.texto("importe")));
m.setPeriodicidad(r.get("periodicidad"));
m.setEstadoEspecial(r.get("estadoEspecial"));
m.setCreatedAt(r.get("createdAt"));
m.setUpdatedAt(r.get("updatedAt"));
m.setRevision(r.texto("revision").isEmpty() ? 1 : Integer.parseInt(r.texto("revision")));
m.setDeletedAt(r.get("deletedAt")); boolean ok = nuevo ? repo.insertar(m) : repo.actualizar(m); if(ok){r.poner("id",m.getId());com.example.agendapersonal.eventos.SeriesEvento.cambios(context);} return ok; }
 public boolean eliminar(String id) { boolean ok=repo.eliminar(id);if(ok)com.example.agendapersonal.eventos.SeriesEvento.cambios(context);return ok; }
 public void close() { repo.cerrar(); }
 }; }
case "gastos": { final GastoRepository repo = new GastoRepository(context, deviceId);
 return new Fuente() {
 public List<Registro> todos() { List<Registro> resultado = new ArrayList<>(); for (Gasto m : repo.obtenerTodos()) { Registro r = new Registro(); r.poner("id", m.getId());
r.poner("ownerId", m.getOwnerId());
r.poner("fecha", m.getFecha());
r.poner("descripcion", m.getDescripcion());
r.poner("clasificacionId", m.getClasificacionId());
r.poner("importe", m.getImporte());
r.poner("estadoEspecial", m.getEstadoEspecial());
r.poner("createdAt", m.getCreatedAt());
r.poner("updatedAt", m.getUpdatedAt());
r.poner("revision", m.getRevision());
r.poner("deletedAt", m.getDeletedAt()); resultado.add(r); } return resultado; }
 public boolean guardar(Registro r, boolean nuevo) { Gasto m = new Gasto(); m.setId(r.get("id"));
m.setOwnerId(r.get("ownerId"));
m.setFecha(r.get("fecha"));
m.setDescripcion(r.get("descripcion"));
m.setClasificacionId(r.get("clasificacionId"));
m.setImporte(r.texto("importe").isEmpty() ? null : Double.valueOf(r.texto("importe")));
m.setEstadoEspecial(r.get("estadoEspecial"));
m.setCreatedAt(r.get("createdAt"));
m.setUpdatedAt(r.get("updatedAt"));
m.setRevision(r.texto("revision").isEmpty() ? 1 : Integer.parseInt(r.texto("revision")));
m.setDeletedAt(r.get("deletedAt")); boolean ok = nuevo ? repo.insertar(m) : repo.actualizar(m); if(ok){r.poner("id",m.getId());com.example.agendapersonal.eventos.SeriesEvento.cambios(context);} return ok; }
 public boolean eliminar(String id) { boolean ok=repo.eliminar(id);if(ok)com.example.agendapersonal.eventos.SeriesEvento.cambios(context);return ok; }
 public void close() { repo.cerrar(); }
 }; }
case "ejercicios": { final EjercicioRepository repo = new EjercicioRepository(context, deviceId);
 return new Fuente() {
 public List<Registro> todos() { List<Registro> resultado = new ArrayList<>(); for (Ejercicio m : repo.obtenerTodos()) { Registro r = new Registro(); r.poner("id", m.getId());
r.poner("ownerId", m.getOwnerId());
r.poner("fecha", m.getFecha());
r.poner("ejercicio", m.getEjercicio());
r.poner("tipo", m.getTipo());
r.poner("musculosTrabajados", m.getMusculosTrabajados());
r.poner("tiempoSegundos", m.getTiempoSegundos());
r.poner("distancia", m.getDistancia());
r.poner("cantidad", m.getCantidad());
r.poner("peso", m.getPeso());
r.poner("createdAt", m.getCreatedAt());
r.poner("updatedAt", m.getUpdatedAt());
r.poner("revision", m.getRevision());
r.poner("deletedAt", m.getDeletedAt()); resultado.add(r); } return resultado; }
 public boolean guardar(Registro r, boolean nuevo) { Ejercicio m = new Ejercicio(); m.setId(r.get("id"));
m.setOwnerId(r.get("ownerId"));
m.setFecha(r.get("fecha"));
m.setEjercicio(r.get("ejercicio"));
m.setTipo(r.get("tipo"));
m.setMusculosTrabajados(r.get("musculosTrabajados"));
m.setTiempoSegundos(r.texto("tiempoSegundos").isEmpty() ? null : Integer.valueOf(r.texto("tiempoSegundos")));
m.setDistancia(r.texto("distancia").isEmpty() ? null : Double.valueOf(r.texto("distancia")));
m.setCantidad(r.texto("cantidad").isEmpty() ? null : Integer.valueOf(r.texto("cantidad")));
m.setPeso(r.texto("peso").isEmpty() ? null : Double.valueOf(r.texto("peso")));
m.setCreatedAt(r.get("createdAt"));
m.setUpdatedAt(r.get("updatedAt"));
m.setRevision(r.texto("revision").isEmpty() ? 1 : Integer.parseInt(r.texto("revision")));
m.setDeletedAt(r.get("deletedAt")); boolean ok = nuevo ? repo.insertar(m) : repo.actualizar(m); if(ok){r.poner("id",m.getId());com.example.agendapersonal.eventos.SeriesEvento.cambios(context);} return ok; }
 public boolean eliminar(String id) { boolean ok=repo.eliminar(id);if(ok)com.example.agendapersonal.eventos.SeriesEvento.cambios(context);return ok; }
 public void close() { repo.cerrar(); }
 }; }
case "ambitos_emocionales": { final AmbitoEmocionalRepository repo = new AmbitoEmocionalRepository(context, deviceId);
 return new Fuente() {
 public List<Registro> todos() { List<Registro> resultado = new ArrayList<>(); for (AmbitoEmocional m : repo.obtenerTodos()) { Registro r = new Registro(); r.poner("id", m.getId());
r.poner("ownerId", m.getOwnerId());
r.poner("nombre", m.getNombre());
r.poner("createdAt", m.getCreatedAt());
r.poner("updatedAt", m.getUpdatedAt());
r.poner("revision", m.getRevision());
r.poner("deletedAt", m.getDeletedAt()); resultado.add(r); } return resultado; }
 public boolean guardar(Registro r, boolean nuevo) { AmbitoEmocional m = new AmbitoEmocional(); m.setId(r.get("id"));
m.setOwnerId(r.get("ownerId"));
m.setNombre(r.get("nombre"));
m.setCreatedAt(r.get("createdAt"));
m.setUpdatedAt(r.get("updatedAt"));
m.setRevision(r.texto("revision").isEmpty() ? 1 : Integer.parseInt(r.texto("revision")));
m.setDeletedAt(r.get("deletedAt")); boolean ok = nuevo ? repo.insertar(m) : repo.actualizar(m); if(ok){r.poner("id",m.getId());com.example.agendapersonal.eventos.SeriesEvento.cambios(context);} return ok; }
 public boolean eliminar(String id) { boolean ok=repo.eliminar(id);if(ok)com.example.agendapersonal.eventos.SeriesEvento.cambios(context);return ok; }
 public void close() { repo.cerrar(); }
 }; }
case "motivos_emocionales": { final MotivoEmocionalRepository repo = new MotivoEmocionalRepository(context, deviceId);
 return new Fuente() {
 public List<Registro> todos() { List<Registro> resultado = new ArrayList<>(); for (MotivoEmocional m : repo.obtenerTodos()) { Registro r = new Registro(); r.poner("id", m.getId());
r.poner("ownerId", m.getOwnerId());
r.poner("nombre", m.getNombre());
r.poner("ambitoId", m.getAmbitoId());
r.poner("createdAt", m.getCreatedAt());
r.poner("updatedAt", m.getUpdatedAt());
r.poner("revision", m.getRevision());
r.poner("deletedAt", m.getDeletedAt()); resultado.add(r); } return resultado; }
 public boolean guardar(Registro r, boolean nuevo) { MotivoEmocional m = new MotivoEmocional(); m.setId(r.get("id"));
m.setOwnerId(r.get("ownerId"));
m.setNombre(r.get("nombre"));
m.setAmbitoId(r.get("ambitoId"));
m.setCreatedAt(r.get("createdAt"));
m.setUpdatedAt(r.get("updatedAt"));
m.setRevision(r.texto("revision").isEmpty() ? 1 : Integer.parseInt(r.texto("revision")));
m.setDeletedAt(r.get("deletedAt")); boolean ok = nuevo ? repo.insertar(m) : repo.actualizar(m); if(ok){r.poner("id",m.getId());com.example.agendapersonal.eventos.SeriesEvento.cambios(context);} return ok; }
 public boolean eliminar(String id) { boolean ok=repo.eliminar(id);if(ok)com.example.agendapersonal.eventos.SeriesEvento.cambios(context);return ok; }
 public void close() { repo.cerrar(); }
 }; }
case "registros_emocionales": { final RegistroEmocionalRepository repo = new RegistroEmocionalRepository(context, deviceId);
 return new Fuente() {
 public List<Registro> todos() { List<Registro> resultado = new ArrayList<>(); for (RegistroEmocional m : repo.obtenerTodos()) { Registro r = new Registro(); r.poner("id", m.getId());
r.poner("ownerId", m.getOwnerId());
r.poner("fechaHora", m.getFechaHora());
r.poner("valoracion", m.getValoracion());
r.poner("motivoId", m.getMotivoId());
r.poner("accionEventoAsociado", m.getAccionEventoAsociado());
r.poner("observaciones", m.getObservaciones());
r.poner("createdAt", m.getCreatedAt());
r.poner("updatedAt", m.getUpdatedAt());
r.poner("revision", m.getRevision());
r.poner("deletedAt", m.getDeletedAt()); resultado.add(r); } return resultado; }
 public boolean guardar(Registro r, boolean nuevo) { RegistroEmocional m = new RegistroEmocional(); m.setId(r.get("id"));
m.setOwnerId(r.get("ownerId"));
m.setFechaHora(r.get("fechaHora"));
m.setValoracion(r.get("valoracion"));
m.setMotivoId(r.get("motivoId"));
m.setAccionEventoAsociado(r.get("accionEventoAsociado"));
m.setObservaciones(r.get("observaciones"));
m.setCreatedAt(r.get("createdAt"));
m.setUpdatedAt(r.get("updatedAt"));
m.setRevision(r.texto("revision").isEmpty() ? 1 : Integer.parseInt(r.texto("revision")));
m.setDeletedAt(r.get("deletedAt")); boolean ok = nuevo ? repo.insertar(m) : repo.actualizar(m); if(ok){r.poner("id",m.getId());com.example.agendapersonal.eventos.SeriesEvento.cambios(context);} return ok; }
 public boolean eliminar(String id) { boolean ok=repo.eliminar(id);if(ok)com.example.agendapersonal.eventos.SeriesEvento.cambios(context);return ok; }
 public void close() { repo.cerrar(); }
 }; }
case "frases_personales": { final FrasePersonalRepository repo = new FrasePersonalRepository(context, deviceId);
 return new Fuente() {
 public List<Registro> todos() { List<Registro> resultado = new ArrayList<>(); for (FrasePersonal m : repo.obtenerTodos()) { Registro r = new Registro(); r.poner("id", m.getId());
r.poner("ownerId", m.getOwnerId());
r.poner("frase", m.getFrase());
r.poner("createdAt", m.getCreatedAt());
r.poner("updatedAt", m.getUpdatedAt());
r.poner("revision", m.getRevision());
r.poner("deletedAt", m.getDeletedAt()); resultado.add(r); } return resultado; }
 public boolean guardar(Registro r, boolean nuevo) { FrasePersonal m = new FrasePersonal(); m.setId(r.get("id"));
m.setOwnerId(r.get("ownerId"));
m.setFrase(r.get("frase"));
m.setCreatedAt(r.get("createdAt"));
m.setUpdatedAt(r.get("updatedAt"));
m.setRevision(r.texto("revision").isEmpty() ? 1 : Integer.parseInt(r.texto("revision")));
m.setDeletedAt(r.get("deletedAt")); boolean ok = nuevo ? repo.insertar(m) : repo.actualizar(m); if(ok){r.poner("id",m.getId());com.example.agendapersonal.eventos.SeriesEvento.cambios(context);} return ok; }
 public boolean eliminar(String id) { boolean ok=repo.eliminar(id);if(ok)com.example.agendapersonal.eventos.SeriesEvento.cambios(context);return ok; }
 public void close() { repo.cerrar(); }
 }; }
case "series_eventos": { final SerieEventoRepository repo = new SerieEventoRepository(context, deviceId);
 return new Fuente() {
 public List<Registro> todos() { List<Registro> resultado = new ArrayList<>(); for (SerieEvento m : repo.obtenerTodos()) { Registro r = new Registro(); r.poner("id", m.getId());
r.poner("ownerId", m.getOwnerId());
r.poner("frecuencia", m.getFrecuencia());
r.poner("configuracionPersonalizada", m.getConfiguracionPersonalizada());
r.poner("fechaFin", m.getFechaFin());
r.poner("createdAt", m.getCreatedAt());
r.poner("updatedAt", m.getUpdatedAt());
r.poner("revision", m.getRevision());
r.poner("deletedAt", m.getDeletedAt()); resultado.add(r); } return resultado; }
 public boolean guardar(Registro r, boolean nuevo) { SerieEvento m = new SerieEvento(); m.setId(r.get("id"));
m.setOwnerId(r.get("ownerId"));
m.setFrecuencia(r.get("frecuencia"));
m.setConfiguracionPersonalizada(r.get("configuracionPersonalizada"));
m.setFechaFin(r.get("fechaFin"));
m.setCreatedAt(r.get("createdAt"));
m.setUpdatedAt(r.get("updatedAt"));
m.setRevision(r.texto("revision").isEmpty() ? 1 : Integer.parseInt(r.texto("revision")));
m.setDeletedAt(r.get("deletedAt")); boolean ok = nuevo ? repo.insertar(m) : repo.actualizar(m); if(ok){r.poner("id",m.getId());com.example.agendapersonal.eventos.SeriesEvento.cambios(context);} return ok; }
 public boolean eliminar(String id) { boolean ok=repo.eliminar(id);if(ok)com.example.agendapersonal.eventos.SeriesEvento.cambios(context);return ok; }
 public void close() { repo.cerrar(); }
 }; }
case "eventos": { final EventoRepository repo = new EventoRepository(context, deviceId);
 return new Fuente() {
 public List<Registro> todos() { List<Registro> resultado = new ArrayList<>(); for (Evento m : repo.obtenerTodos()) { Registro r = new Registro(); r.poner("id", m.getId());
r.poner("ownerId", m.getOwnerId());
r.poner("descripcion", m.getDescripcion());
r.poner("categoria", m.getCategoria());
r.poner("fecha", m.getFecha());
r.poner("hora", m.getHora());
r.poner("personaId", m.getPersonaId());
r.poner("lugar", m.getLugar());
r.poner("estadoEspecial", m.getEstadoEspecial());
r.poner("alarmaActivada", m.isAlarmaActivada());
r.poner("anticipacionMinutos", m.getAnticipacionMinutos());
r.poner("serieId", m.getSerieId());
r.poner("observaciones", m.getObservaciones());
r.poner("createdAt", m.getCreatedAt());
r.poner("updatedAt", m.getUpdatedAt());
r.poner("revision", m.getRevision());
r.poner("deletedAt", m.getDeletedAt()); resultado.add(r); } return resultado; }
 public boolean guardar(Registro r, boolean nuevo) { Evento m = new Evento(); m.setId(r.get("id"));
m.setOwnerId(r.get("ownerId"));
m.setDescripcion(r.get("descripcion"));
m.setCategoria(r.get("categoria"));
m.setFecha(r.get("fecha"));
m.setHora(r.get("hora"));
m.setPersonaId(r.get("personaId"));
m.setLugar(r.get("lugar"));
m.setEstadoEspecial(r.get("estadoEspecial"));
m.setAlarmaActivada(Boolean.parseBoolean(r.texto("alarmaActivada")));
m.setAnticipacionMinutos(r.texto("anticipacionMinutos").isEmpty() ? null : Integer.valueOf(r.texto("anticipacionMinutos")));
m.setSerieId(r.get("serieId"));
m.setObservaciones(r.get("observaciones"));
m.setCreatedAt(r.get("createdAt"));
m.setUpdatedAt(r.get("updatedAt"));
m.setRevision(r.texto("revision").isEmpty() ? 1 : Integer.parseInt(r.texto("revision")));
m.setDeletedAt(r.get("deletedAt")); boolean ok = nuevo ? repo.insertar(m) : repo.actualizar(m); if(ok){r.poner("id",m.getId());com.example.agendapersonal.eventos.SeriesEvento.cambios(context);} return ok; }
 public boolean eliminar(String id) { boolean ok=repo.eliminar(id);if(ok)com.example.agendapersonal.eventos.SeriesEvento.cambios(context);return ok; }
 public void close() { repo.cerrar(); }
 }; }
case "tareas": { final TareaRepository repo = new TareaRepository(context, deviceId);
 return new Fuente() {
 public List<Registro> todos() { List<Registro> resultado = new ArrayList<>(); for (Tarea m : repo.obtenerTodas()) { Registro r = new Registro(); r.poner("id", m.getId());
r.poner("ownerId", m.getOwnerId());
r.poner("descripcion", m.getDescripcion());
r.poner("fechaInicio", m.getFechaInicio());
r.poner("horaInicio", m.getHoraInicio());
r.poner("fechaVencimiento", m.getFechaVencimiento());
r.poner("horaVencimiento", m.getHoraVencimiento());
r.poner("categoria", m.getCategoria());
r.poner("prioridad", m.getPrioridad());
r.poner("estadoEspecial", m.getEstadoEspecial());
r.poner("alarmaActivada", m.isAlarmaActivada());
r.poner("anticipacionMinutos", m.getAnticipacionMinutos());
r.poner("observaciones", m.getObservaciones());
r.poner("createdAt", m.getCreatedAt());
r.poner("updatedAt", m.getUpdatedAt());
r.poner("revision", m.getRevision());
r.poner("deletedAt", m.getDeletedAt()); resultado.add(r); } return resultado; }
 public boolean guardar(Registro r, boolean nuevo) { Tarea m = new Tarea(); m.setId(r.get("id"));
m.setOwnerId(r.get("ownerId"));
m.setDescripcion(r.get("descripcion"));
m.setFechaInicio(r.get("fechaInicio"));
m.setHoraInicio(r.get("horaInicio"));
m.setFechaVencimiento(r.get("fechaVencimiento"));
m.setHoraVencimiento(r.get("horaVencimiento"));
m.setCategoria(r.get("categoria"));
m.setPrioridad(r.get("prioridad"));
m.setEstadoEspecial(r.get("estadoEspecial"));
m.setAlarmaActivada(Boolean.parseBoolean(r.texto("alarmaActivada")));
m.setAnticipacionMinutos(r.texto("anticipacionMinutos").isEmpty() ? null : Integer.valueOf(r.texto("anticipacionMinutos")));
m.setObservaciones(r.get("observaciones"));
m.setCreatedAt(r.get("createdAt"));
m.setUpdatedAt(r.get("updatedAt"));
m.setRevision(r.texto("revision").isEmpty() ? 1 : Integer.parseInt(r.texto("revision")));
m.setDeletedAt(r.get("deletedAt")); boolean ok = nuevo ? repo.insertar(m) : repo.actualizar(m); if(ok){r.poner("id",m.getId());com.example.agendapersonal.eventos.SeriesEvento.cambios(context);} return ok; }
 public boolean eliminar(String id) { boolean ok=repo.eliminar(id);if(ok)com.example.agendapersonal.eventos.SeriesEvento.cambios(context);return ok; }
 public void close() { repo.cerrar(); }
 }; }
case "historial_postergaciones": { final HistorialPostergacionRepository repo = new HistorialPostergacionRepository(context, deviceId);
 return new Fuente() {
 public List<Registro> todos() { List<Registro> resultado = new ArrayList<>(); for (HistorialPostergacion m : repo.obtenerTodos()) { Registro r = new Registro(); r.poner("id", m.getId());
r.poner("ownerId", m.getOwnerId());
r.poner("tipoEntidad", m.getTipoEntidad());
r.poner("entidadId", m.getEntidadId());
r.poner("fechaOriginal", m.getFechaOriginal());
r.poner("nuevaFecha", m.getNuevaFecha());
r.poner("fechaPostergacion", m.getFechaPostergacion());
r.poner("createdAt", m.getCreatedAt());
r.poner("updatedAt", m.getUpdatedAt());
r.poner("revision", m.getRevision());
r.poner("deletedAt", m.getDeletedAt()); resultado.add(r); } return resultado; }
 public boolean guardar(Registro r, boolean nuevo) { HistorialPostergacion m = new HistorialPostergacion(); m.setId(r.get("id"));
m.setOwnerId(r.get("ownerId"));
m.setTipoEntidad(r.get("tipoEntidad"));
m.setEntidadId(r.get("entidadId"));
m.setFechaOriginal(r.get("fechaOriginal"));
m.setNuevaFecha(r.get("nuevaFecha"));
m.setFechaPostergacion(r.get("fechaPostergacion"));
m.setCreatedAt(r.get("createdAt"));
m.setUpdatedAt(r.get("updatedAt"));
m.setRevision(r.texto("revision").isEmpty() ? 1 : Integer.parseInt(r.texto("revision")));
m.setDeletedAt(r.get("deletedAt")); boolean ok = nuevo ? repo.insertar(m) : repo.actualizar(m); if(ok){r.poner("id",m.getId());com.example.agendapersonal.eventos.SeriesEvento.cambios(context);} return ok; }
 public boolean eliminar(String id) { boolean ok=repo.eliminar(id);if(ok)com.example.agendapersonal.eventos.SeriesEvento.cambios(context);return ok; }
 public void close() { repo.cerrar(); }
 }; }
case "registros_investigacion": { final RegistroInvestigacionRepository repo = new RegistroInvestigacionRepository(context, deviceId);
 return new Fuente() {
 public List<Registro> todos() { List<Registro> resultado = new ArrayList<>(); for (RegistroInvestigacion m : repo.obtenerTodos()) { Registro r = new Registro(); r.poner("id", m.getId());
r.poner("ownerId", m.getOwnerId());
r.poner("idOriginal", m.getIdOriginal());
r.poner("datosOriginales", m.getDatosOriginales());
r.poner("createdAt", m.getCreatedAt());
r.poner("updatedAt", m.getUpdatedAt());
r.poner("revision", m.getRevision());
r.poner("deletedAt", m.getDeletedAt()); resultado.add(r); } return resultado; }
 public boolean guardar(Registro r, boolean nuevo) { RegistroInvestigacion m = new RegistroInvestigacion(); m.setId(r.get("id"));
m.setOwnerId(r.get("ownerId"));
m.setIdOriginal(r.get("idOriginal"));
m.setDatosOriginales(r.get("datosOriginales"));
m.setCreatedAt(r.get("createdAt"));
m.setUpdatedAt(r.get("updatedAt"));
m.setRevision(r.texto("revision").isEmpty() ? 1 : Integer.parseInt(r.texto("revision")));
m.setDeletedAt(r.get("deletedAt")); boolean ok = nuevo ? repo.insertar(m) : repo.actualizar(m); if(ok){r.poner("id",m.getId());com.example.agendapersonal.eventos.SeriesEvento.cambios(context);} return ok; }
 public boolean eliminar(String id) { boolean ok=repo.eliminar(id);if(ok)com.example.agendapersonal.eventos.SeriesEvento.cambios(context);return ok; }
 public void close() { repo.cerrar(); }
 }; }
case "observaciones_ri": { final ObservacionRIRepository repo = new ObservacionRIRepository(context, deviceId);
 return new Fuente() {
 public List<Registro> todos() { List<Registro> resultado = new ArrayList<>(); for (ObservacionRI m : repo.obtenerTodos()) { Registro r = new Registro(); r.poner("id", m.getId());
r.poner("ownerId", m.getOwnerId());
r.poner("registroInvestigacionId", m.getRegistroInvestigacionId());
r.poner("observacion", m.getObservacion());
r.poner("createdAt", m.getCreatedAt());
r.poner("updatedAt", m.getUpdatedAt());
r.poner("revision", m.getRevision());
r.poner("deletedAt", m.getDeletedAt()); resultado.add(r); } return resultado; }
 public boolean guardar(Registro r, boolean nuevo) { ObservacionRI m = new ObservacionRI(); m.setId(r.get("id"));
m.setOwnerId(r.get("ownerId"));
m.setRegistroInvestigacionId(r.get("registroInvestigacionId"));
m.setObservacion(r.get("observacion"));
m.setCreatedAt(r.get("createdAt"));
m.setUpdatedAt(r.get("updatedAt"));
m.setRevision(r.texto("revision").isEmpty() ? 1 : Integer.parseInt(r.texto("revision")));
m.setDeletedAt(r.get("deletedAt")); boolean ok = nuevo ? repo.insertar(m) : repo.actualizar(m); if(ok){r.poner("id",m.getId());com.example.agendapersonal.eventos.SeriesEvento.cambios(context);} return ok; }
 public boolean eliminar(String id) { boolean ok=repo.eliminar(id);if(ok)com.example.agendapersonal.eventos.SeriesEvento.cambios(context);return ok; }
 public void close() { repo.cerrar(); }
 }; } default: throw new IllegalArgumentException("Tabla no admitida: " + tabla); }
 }
}
