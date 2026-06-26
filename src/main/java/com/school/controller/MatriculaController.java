/*package com.school.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import com.school.dao.NotaDao;
import com.school.model.*;
import com.school.reportDto.AsistenciaReporte;
import com.school.reportDto.CursoReporte;
import com.school.service.AsistenciaService;
import com.school.service.ClaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.school.service.MatriculaService;

@CrossOrigin(origins = {"http://localhost:4200"})
@RestController
@RequestMapping("/api/matriculas")
public class MatriculaController {

	@Autowired
	private MatriculaService matriculaService;

	@Autowired
	private NotaDao notaDao;

	@Autowired
	private AsistenciaService asistenciaService;

	@Autowired
	private ClaseService claseService;

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/crear")
	public ResponseEntity<?> saveMatricula(@Valid @RequestBody Matricula matricula, BindingResult results){
		
		Matricula matriculaNueva = null;
		Map<String, Object> response = new HashMap<>();
		
		if(results.hasErrors()) {
			List<String> errors = results.getFieldErrors()
					.stream()
					.map(er -> "El campo '" + er.getField() +"' " + er.getDefaultMessage())
					.collect(Collectors.toList());
			
			response.put("errors", errors);
			
			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);
		}
		
		try {
			Estudiante estudiante = matricula.getEstudiante();
			String[] nombres = estudiante.getNombres().split(" ");
			estudiante.setCorreo(nombres[0] + "." + estudiante.getApellidoPaterno());
			estudiante.setCorreo(estudiante.getCorreo().concat("@elamericano.edu.pe").toLowerCase());
			Apoderado apoderado = estudiante.getApoderado();
			estudiante.setApoderado(apoderado);
			matricula.setEstudiante(estudiante);
			matriculaNueva = matriculaService.save(matricula);
		} catch (DataAccessException e) {
			response.put("mensaje", "Error al insertar el aula en la base de datos");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
		}
		
		response.put("matricula", matriculaNueva);
		response.put("mensaje", "La matrícula se ha creado con éxito");
		
		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/niveles")
	public ResponseEntity<List<Nivel>> getNiveles(){
		return new ResponseEntity<>(matriculaService.getNiveles(), HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/turnos")
	public ResponseEntity<List<Turno>> getTurnos(){
		return new ResponseEntity<>(matriculaService.getTurnos(), HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/dias")
	public ResponseEntity<List<DiaSemana>> getDias(){
		return new ResponseEntity<>(matriculaService.getDias(), HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
	@GetMapping("/notas")
	public ResponseEntity<List<Nota>> getNotas(@RequestParam("idCurso") String idCurso, @RequestParam("idAula") String idAula){
		return new ResponseEntity<>(notaDao.notasPorAulaYCurso(Long.parseLong(idCurso), Long.parseLong(idAula)), HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
	@PutMapping("/notas")
	public ResponseEntity<?> UpdateNota(@RequestBody List<Nota> notas){
		List<Nota> notasActualizar = notas;
		Map<String, Object> response = new HashMap<>();


		try {
			notasActualizar = (List<Nota>) notaDao.saveAll(notas);
		} catch (DataAccessException e) {
			response.put("mensaje", "Error al ACTUALIZAR las notas en la base de datos");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));

			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		response.put("mensaje", "Notas actualizadas con éxito!");
		response.put("notas", notasActualizar);

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/getPDF")
	public ResponseEntity<?> getReporteAsistenciaPDF(@RequestParam("fecha") String fecha){
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header("Content-Disposition","attachment; filename="+"asistencias.pdf")
				.body(asistenciaService.generarReporteAsistencia("pdf", fecha));
	}

	@GetMapping("/getXLS")
	public ResponseEntity<?> getReporteAsistenciaXLS(@RequestParam("fecha") String fecha){
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header("Content-Disposition","attachment; filename="+"asistencias.xlsx")
				.body(asistenciaService.generarReporteAsistencia("xls", fecha));
	}

	

	@GetMapping("/getReporteCursoXLS")
public ResponseEntity<?> getReporteCursoXLS(
        @RequestParam("idCurso") String idCurso, 
        @RequestParam("idGrado") String idGrado,
        @RequestParam("bimestre") String bimestre) {  // 👈 AGREGAR bimestre
    
    byte[] excelBytes = matriculaService.generarReporteNotasExcel(
        "xls", 
        Long.parseLong(idCurso), 
        Long.parseLong(idGrado), 
        bimestre
    );
    
    if (excelBytes == null || excelBytes.length == 0) {
        return ResponseEntity.noContent().build();
    }
    
    return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", "attachment; filename=notas.xlsx")
            .body(excelBytes);
}


	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/getAsistenciasPorDia")
	public ResponseEntity<AsistenciaReporte>  obtenerAsistenciasPorDia(@RequestParam("fecha") String fecha){
		return new ResponseEntity<>(asistenciaService.obtenerDatosAsistenciaPorDia(fecha), HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
	@GetMapping("/getAsistenciasFechaAula")
	public ResponseEntity<?>  obtenerAsistenciasPorDia(@RequestParam("fecha") String fecha, @RequestParam("idAula") String idAula){
		return new ResponseEntity<>(asistenciaService.findAsistenciaByFechaAula(fecha, Long.parseLong(idAula)), HttpStatus.OK);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/getCursoReporte")
	public ResponseEntity<List<CursoReporte>> getCursoReporte(@RequestParam("idCurso") String idCurso, @RequestParam("idGrado")  String idGrado){
		List<CursoReporte> cursoReporteList = claseService.getCursoReporte(Long.parseLong(idCurso),Long.parseLong(idGrado));

		return new ResponseEntity<>(cursoReporteList, HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
	@PutMapping("/updateAsistencias")
	public ResponseEntity<?> updateAsistencias(@RequestBody List<Asistencia> asistencias){
		List<Asistencia> asistenciasActualizar = asistencias;
		Map<String, Object> response = new HashMap<>();


		try {
			asistenciasActualizar = asistenciaService.updateAsistencias(asistencias);
		} catch (DataAccessException e) {
			response.put("mensaje", "Error al actualizar las asistencias en la base de datos");
			response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));

			return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		response.put("mensaje", "Las asistencias se actualizaron con éxito!");
		response.put("asistencias", asistenciasActualizar);

		return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/matriculasPorEstudiante")
	public ResponseEntity<List<Matricula>> getMatriculasPorEstudiante(@Param("id") String id) {
		return new ResponseEntity<List<Matricula>>(matriculaService.getMatriculasPorEstudiante(Long.parseLong(id)), HttpStatus.OK);
	}

}*/
package com.school.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import com.school.dao.NotaDao;
import com.school.model.*;
import com.school.reportDto.AsistenciaReporte;
import com.school.reportDto.CursoReporte;
import com.school.security.models.Rol;
import com.school.security.models.Usuario;
import com.school.security.service.IRolService;
import com.school.security.service.IUsuarioService;
import com.school.service.AsistenciaService;
import com.school.service.ClaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.school.service.MatriculaService;

@CrossOrigin(origins = {"http://localhost:4200"})
@RestController
@RequestMapping("/api/matriculas")
public class MatriculaController {

    @Autowired
    private MatriculaService matriculaService;

    @Autowired
    private NotaDao notaDao;

    @Autowired
    private AsistenciaService asistenciaService;

    @Autowired
    private ClaseService claseService;

    @Autowired
    private IUsuarioService usuarioService;

    @Autowired
    private IRolService rolService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/crear")
    public ResponseEntity<?> saveMatricula(@Valid @RequestBody Matricula matricula, BindingResult results){

        Matricula matriculaNueva = null;
        Map<String, Object> response = new HashMap<>();

        if (results.hasErrors()) {
            List<String> errors = results.getFieldErrors()
                    .stream()
                    .map(er -> "El campo '" + er.getField() + "' " + er.getDefaultMessage())
                    .collect(Collectors.toList());

            response.put("errors", errors);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);
        }

        try {
            Estudiante estudiante = matricula.getEstudiante();
            String[] nombres = estudiante.getNombres().split(" ");

            // 1. Generar correo
            String correoGenerado = (nombres[0] + "." + estudiante.getApellidoPaterno() + "@elamericano.edu.pe").toLowerCase();
            estudiante.setCorreo(correoGenerado);

            // 2. Verificar si el correo ya existe como usuario
            if (usuarioService.existsByUsername(correoGenerado)) {
                response.put("mensaje", "El correo " + correoGenerado + " ya está registrado");
                return new ResponseEntity<Map<String, Object>>(response, HttpStatus.BAD_REQUEST);
            }

            // 3. Crear usuario con username = CORREO, password = DNI
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setUsername(correoGenerado);
            nuevoUsuario.setPassword(passwordEncoder.encode(estudiante.getDni()));
            nuevoUsuario.setEnabled(true);

            // 4. Asignar rol ESTUDIANTE
            Rol rolEstudiante = rolService.findByRolNombre(com.school.security.enums.RolNombre.ROLE_ESTUDIANTE).get();
            nuevoUsuario.getRoles().add(rolEstudiante);

            Usuario usuarioCreado = usuarioService.save(nuevoUsuario);
            estudiante.setUsuario(usuarioCreado);

            // 5. Guardar apoderado y estudiante
            Apoderado apoderado = estudiante.getApoderado();
            estudiante.setApoderado(apoderado);
            matricula.setEstudiante(estudiante);
            matriculaNueva = matriculaService.save(matricula);

        } catch (DataAccessException e) {
            response.put("mensaje", "Error al insertar la matrícula en la base de datos");
            response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response.put("matricula", matriculaNueva);
        response.put("mensaje", "La matrícula se ha creado con éxito");

        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/niveles")
    public ResponseEntity<List<Nivel>> getNiveles(){
        return new ResponseEntity<>(matriculaService.getNiveles(), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/turnos")
    public ResponseEntity<List<Turno>> getTurnos(){
        return new ResponseEntity<>(matriculaService.getTurnos(), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dias")
    public ResponseEntity<List<DiaSemana>> getDias(){
        return new ResponseEntity<>(matriculaService.getDias(), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    @GetMapping("/notas")
    public ResponseEntity<List<Nota>> getNotas(@RequestParam("idCurso") String idCurso, @RequestParam("idAula") String idAula){
        return new ResponseEntity<>(notaDao.notasPorAulaYCurso(Long.parseLong(idCurso), Long.parseLong(idAula)), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    @PutMapping("/notas")
    public ResponseEntity<?> UpdateNota(@RequestBody List<Nota> notas){
        List<Nota> notasActualizar = notas;
        Map<String, Object> response = new HashMap<>();

        try {
            notasActualizar = (List<Nota>) notaDao.saveAll(notas);
        } catch (DataAccessException e) {
            response.put("mensaje", "Error al ACTUALIZAR las notas en la base de datos");
            response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response.put("mensaje", "Notas actualizadas con éxito!");
        response.put("notas", notasActualizar);
        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getPDF")
    public ResponseEntity<?> getReporteAsistenciaPDF(@RequestParam("fecha") String fecha){
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition","attachment; filename="+"asistencias.pdf")
                .body(asistenciaService.generarReporteAsistencia("pdf", fecha));
    }

    @GetMapping("/getXLS")
    public ResponseEntity<?> getReporteAsistenciaXLS(@RequestParam("fecha") String fecha){
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition","attachment; filename="+"asistencias.xlsx")
                .body(asistenciaService.generarReporteAsistencia("xls", fecha));
    }

    @GetMapping("/getReporteCursoXLS")
    public ResponseEntity<?> getReporteCursoXLS(
            @RequestParam("idCurso") String idCurso,
            @RequestParam("idGrado") String idGrado,
            @RequestParam("bimestre") String bimestre) {

        byte[] excelBytes = matriculaService.generarReporteNotasExcel(
                "xls",
                Long.parseLong(idCurso),
                Long.parseLong(idGrado),
                bimestre
        );

        if (excelBytes == null || excelBytes.length == 0) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=notas.xlsx")
                .body(excelBytes);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getAsistenciasPorDia")
    public ResponseEntity<AsistenciaReporte> obtenerAsistenciasPorDia(@RequestParam("fecha") String fecha){
        return new ResponseEntity<>(asistenciaService.obtenerDatosAsistenciaPorDia(fecha), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    @GetMapping("/getAsistenciasFechaAula")
    public ResponseEntity<?> obtenerAsistenciasPorDia(@RequestParam("fecha") String fecha, @RequestParam("idAula") String idAula){
        return new ResponseEntity<>(asistenciaService.findAsistenciaByFechaAula(fecha, Long.parseLong(idAula)), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/getCursoReporte")
    public ResponseEntity<List<CursoReporte>> getCursoReporte(@RequestParam("idCurso") String idCurso, @RequestParam("idGrado") String idGrado){
        List<CursoReporte> cursoReporteList = claseService.getCursoReporte(Long.parseLong(idCurso), Long.parseLong(idGrado));
        return new ResponseEntity<>(cursoReporteList, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PROFESOR')")
    @PutMapping("/updateAsistencias")
    public ResponseEntity<?> updateAsistencias(@RequestBody List<Asistencia> asistencias){
        List<Asistencia> asistenciasActualizar = asistencias;
        Map<String, Object> response = new HashMap<>();

        try {
            asistenciasActualizar = asistenciaService.updateAsistencias(asistencias);
        } catch (DataAccessException e) {
            response.put("mensaje", "Error al actualizar las asistencias en la base de datos");
            response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        response.put("mensaje", "Las asistencias se actualizaron con éxito!");
        response.put("asistencias", asistenciasActualizar);
        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/matriculasPorEstudiante")
    public ResponseEntity<List<Matricula>> getMatriculasPorEstudiante(@Param("id") String id) {
        return new ResponseEntity<List<Matricula>>(matriculaService.getMatriculasPorEstudiante(Long.parseLong(id)), HttpStatus.OK);
    }
}