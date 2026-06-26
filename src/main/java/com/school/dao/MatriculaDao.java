package com.school.dao;

import com.school.model.DiaSemana;
import com.school.model.Nivel;
import com.school.model.Turno;
import org.springframework.data.jpa.repository.JpaRepository;

import com.school.model.Matricula;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatriculaDao extends JpaRepository<Matricula, Long>{

    @Query("FROM Turno")
    public List<Turno> getTurnos();

    @Query("FROM Nivel")
    public List<Nivel> getNiveles();

    @Query("FROM DiaSemana")
    public List<DiaSemana> getDias();

    @Query("FROM Matricula m where m.estudiante.id = ?1")
    public List<Matricula> getMatriculasPorEstudiante(Long id);

    // Metodo de filtraado de Notas 
    // En MatriculaDao.java
@Query("SELECT e.nombres AS estudiante, " +
       "CASE WHEN :bimestre = 'Bim1' THEN n.nota_bim1 " +
            "WHEN :bimestre = 'Bim2' THEN n.nota_bim2 " +
            "WHEN :bimestre = 'Bim3' THEN n.nota_bim3 " +
            "WHEN :bimestre = 'Bim4' THEN n.nota_bim4 " +
            "ELSE n.promedio_final END AS nota " +
       "FROM Nota n " +
       "JOIN n.estudiante e " +
       "JOIN n.curso c " +
       "JOIN e.grado g " +
       "WHERE c.id = :idCurso " +
       "AND g.id = :idGrado")
List<Object[]> getNotasPorCursoYBimestre(@Param("idCurso") Long idCurso, 
                                          @Param("idGrado") Long idGrado,
                                          @Param("bimestre") String bimestre);
}
