package com.utp.myproject.controller;


import com.utp.myproject.model.Cita;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @PersistenceContext
    private EntityManager entityManager;

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