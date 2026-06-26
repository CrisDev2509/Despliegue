/*package com.school.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "notas")
public class Nota implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer nota_bim1;
    private Integer nota_bim2;
    private Integer nota_bim3;
    private Integer nota_bim4;
    private Double promedio_final;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "curso_id")
    private Curso curso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"notas", "hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "estudiante_id")
    private Estudiante estudiante;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNota_bim1() {
        return nota_bim1;
    }

    public void setNota_bim1(Integer nota_bim1) {
        this.nota_bim1 = nota_bim1;
    }

    public Integer getNota_bim2() {
        return nota_bim2;
    }

    public void setNota_bim2(Integer nota_bim2) {
        this.nota_bim2 = nota_bim2;
    }

    public Integer getNota_bim3() {
        return nota_bim3;
    }

    public void setNota_bim3(Integer nota_bim3) {
        this.nota_bim3 = nota_bim3;
    }

    public Integer getNota_bim4() {
        return nota_bim4;
    }

    public void setNota_bim4(Integer nota_bim4) {
        this.nota_bim4 = nota_bim4;
    }

    public Double getPromedio_final() {
        promedio_final = Double.valueOf((nota_bim1 + nota_bim2 + nota_bim3 + nota_bim4)/4);
        return promedio_final;
    }

    public void setPromedio_final(Double promedio_final) {
        this.promedio_final = promedio_final;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    private static final long serialVersionUID = 1L;
}*/

package com.school.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "notas")
public class Nota implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer nota_bim1;
    private Integer nota_bim2;
    private Integer nota_bim3;
    private Integer nota_bim4;
    
    @Column(columnDefinition = "DECIMAL(5,2)")  // 👈 AGREGAR ESTO
    private Double promedio_final;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "curso_id")
    private Curso curso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"notas", "hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "estudiante_id")
    private Estudiante estudiante;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNota_bim1() {
        return nota_bim1;
    }

    public void setNota_bim1(Integer nota_bim1) {
        this.nota_bim1 = nota_bim1;
        calcularPromedio();
    }

    public Integer getNota_bim2() {
        return nota_bim2;
    }

    public void setNota_bim2(Integer nota_bim2) {
        this.nota_bim2 = nota_bim2;
        calcularPromedio();
    }

    public Integer getNota_bim3() {
        return nota_bim3;
    }

    public void setNota_bim3(Integer nota_bim3) {
        this.nota_bim3 = nota_bim3;
        calcularPromedio();
    }

    public Integer getNota_bim4() {
        return nota_bim4;
    }

    public void setNota_bim4(Integer nota_bim4) {
        this.nota_bim4 = nota_bim4;
        calcularPromedio();
    }

    public void calcularPromedio() {
        int sum = 0;
        int count = 0;
        
        if (nota_bim1 != null) { 
            sum += nota_bim1; 
            count++; 
        }
        if (nota_bim2 != null) { 
            sum += nota_bim2; 
            count++; 
        }
        if (nota_bim3 != null) { 
            sum += nota_bim3; 
            count++; 
        }
        if (nota_bim4 != null) { 
            sum += nota_bim4; 
            count++; 
        }
        
        if (count > 0) {
            this.promedio_final = Double.valueOf(sum) / count;
        } else {
            this.promedio_final = 0.0;
        }
    }

    public Double getPromedio_final() {
        if (promedio_final == null && (nota_bim1 != null || nota_bim2 != null || nota_bim3 != null || nota_bim4 != null)) {
            calcularPromedio();
        }
        return promedio_final;
    }

    public void setPromedio_final(Double promedio_final) {
        this.promedio_final = promedio_final;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    @PrePersist
    @PreUpdate
    public void antesDeGuardar() {
        calcularPromedio();
    }

    private static final long serialVersionUID = 1L;
}