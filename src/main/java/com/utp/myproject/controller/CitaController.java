package com.utp.myproject.controller;


import com.utp.myproject.model.Cita;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @PersistenceContext
    private EntityManager entityManager;

    @org.springframework.beans.factory.annotation.Autowired
    private RestTemplate restTemplate;

    @Value("${notificaciones.url}")
    private String notificacionesUrl;

    // READ ALL - GET /api/citas
    @GetMapping
    public List<Cita> listarTodas() {
        return entityManager.createQuery("SELECT c FROM Cita c", Cita.class).getResultList();
    }

    // READ ONE - GET /api/citas/1
    @GetMapping("/{id}")
    public Cita obtenerCita(@PathVariable Integer id) {
        Cita cita = entityManager.find(Cita.class, id);
        if (cita == null) {
            throw new RuntimeException("Cita no encontrada");
        }
        return cita;
    }

    // CREATE - POST /api/citas
    @PostMapping
    @Transactional
    public Cita crearCita(@RequestBody Cita cita) {
        // Asegurar que fecha y hora estén en el formato correcto
        entityManager.persist(cita);

        // Llamar al Servicio Secundario de notificaciones (con fallback)
        try {
            Map<String, String> datos = new HashMap<>();
            datos.put("paciente", cita.getPaciente());
            datos.put("medico", cita.getMedico());
            datos.put("fecha", cita.getFecha().toString());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(datos, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(notificacionesUrl, request, String.class);
            System.out.println("Notificación enviada correctamente: " + response.getBody());

        } catch (Exception e) {
            // Fallback: si el servicio de notificaciones falla, la cita se crea igual
            System.out.println("ADVERTENCIA: No se pudo enviar la notificación. Motivo: " + e.getMessage());
            System.out.println("La cita se creó correctamente de todas formas (fallback activado).");
        }


        return cita;
    }

    // UPDATE - PUT /api/citas/1
    @PutMapping("/{id}")
    @Transactional
    public Cita actualizarCita(@PathVariable Integer id, @RequestBody Cita citaActualizada) {
        Cita cita = entityManager.find(Cita.class, id);
        if (cita == null) {
            throw new RuntimeException("Cita no encontrada");
        }

        cita.setPaciente(citaActualizada.getPaciente());
        cita.setMedico(citaActualizada.getMedico());
        cita.setFecha(citaActualizada.getFecha());
        cita.setHora(citaActualizada.getHora());
        cita.setMotivo(citaActualizada.getMotivo());

        return entityManager.merge(cita);
    }

    // DELETE - DELETE /api/citas/1
    @DeleteMapping("/{id}")
    @Transactional
    public String eliminarCita(@PathVariable Integer id) {
        Cita cita = entityManager.find(Cita.class, id);
        if (cita == null) {
            throw new RuntimeException("Cita no encontrada");
        }

        entityManager.remove(cita);
        return "Cita eliminada exitosamente";
    }
}