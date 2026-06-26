package com.school.dao;

import com.school.model.Nota;
import org.springframework.data.jpa.repository.JpaRepository;

import com.school.model.Estudiante;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EstudianteDao extends JpaRepository<Estudiante, Long>{

    @Query("FROM Estudiante e where e.usuario.username = ?1 and e.usuario.password = ?2 ")
    public Estudiante loginUsuario(String username, String password);

    public Estudiante findByDniAndDni(String username, String password);

    public Estudiante findByDni(String dni);
    
    // 👈 NUEVO MÉTODO
    @Query("SELECT e FROM Estudiante e WHERE e.correo = :correo")
    public Estudiante findByCorreo(@Param("correo") String correo);

}
