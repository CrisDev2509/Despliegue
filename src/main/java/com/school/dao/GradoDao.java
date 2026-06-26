/*package com.school.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.school.model.Grado;

public interface GradoDao extends JpaRepository<Grado, Long>{

}*/
package com.school.dao;

import com.school.model.Grado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GradoDao extends JpaRepository<Grado, Long> {

    // 👈 AGREGAR ESTE MÉTODO
    @Query("SELECT g FROM Grado g WHERE g.nivel.id = :nivelId")
    List<Grado> findByNivelId(@Param("nivelId") Long nivelId);
}