package com.school.service;

import java.util.List;
import java.util.Optional;

import com.school.model.*;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayOutputStream;  // 👈 ESTA IMPORTACIÓN
import org.apache.poi.ss.usermodel.Row;

import com.school.dao.MatriculaDao;

@Service
public class MatriculaServiceImpl implements MatriculaService{

	@Autowired
	private MatriculaDao matriculaDao;

	@Override
	@Transactional(readOnly = false)
	public Matricula save(Matricula matricula) {
		return matriculaDao.save(matricula);
	}

	@Override
	@Transactional
	public Optional<Matricula> getMatriculaById(Long id) {
		// TODO Auto-generated method stub
		return matriculaDao.findById(id);
	}

	@Override
	@Transactional
	public List<Matricula> findAll() {
		// TODO Auto-generated method stub
		return matriculaDao.findAll();
	}

	@Override
	@Transactional(readOnly = false)
	public boolean delete(Long id) {
		// TODO Auto-generated method stub
		return getMatriculaById(id).map(m -> {
			matriculaDao.deleteById(id);
			return true;
		}).orElse(false);
	}

	@Override
	public List<Nivel> getNiveles() {
		return matriculaDao.getNiveles();
	}

	@Override
	public List<Turno> getTurnos() {
		return matriculaDao.getTurnos();
	}

	@Override
	public List<DiaSemana> getDias() {
		return matriculaDao.getDias();
	}

	@Override
	public List<Matricula> getMatriculasPorEstudiante(Long id) {
		return matriculaDao.getMatriculasPorEstudiante(id);
	}
	
	/*@Override
	public byte[] generarReporteNotasExcel(String tipo, Long idCurso, Long idGrado, String bimestre) {
    try {
        System.out.println("Generando Excel notas - Curso: " + idCurso + ", Grado: " + idGrado + ", Bimestre: " + bimestre);
        
        // 👈 PASAR idGrado también
        List<Object[]> datos = matriculaDao.getNotasPorCursoYBimestre(idCurso, idGrado, bimestre);
        
        if (datos == null || datos.isEmpty()) {
            System.out.println("No hay datos de notas para estos filtros");
            return null;
        }
        
        System.out.println("Encontrados " + datos.size() + " registros");
        
        // Resto del código igual...
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Notas");
        
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Estudiante");
        header.createCell(1).setCellValue("Nota");
        header.createCell(2).setCellValue("Grado");
        header.createCell(3).setCellValue("Bimestre");
        
        int rowNum = 1;
        for (Object[] rowData : datos) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rowData[0] != null ? rowData[0].toString() : "");
            
            Object notaObj = rowData[1];
            if (notaObj != null) {
                if (notaObj instanceof Integer) {
                    row.createCell(1).setCellValue((Integer) notaObj);
                } else if (notaObj instanceof Double) {
                    row.createCell(1).setCellValue((Double) notaObj);
                }
            } else {
                row.createCell(1).setCellValue(0);
            }
            
            // 👈 AGREGAR GRADO (opcional, para verificar)
            row.createCell(2).setCellValue(rowData[2] != null ? rowData[2].toString() : "");
            row.createCell(3).setCellValue(bimestre);
        }
        
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        
        System.out.println("Excel generado, tamaño: " + out.size() + " bytes");
        
        return out.toByteArray();
        
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }*/

	@Override
	public byte[] generarReporteNotasExcel(String tipo, Long idCurso, Long idGrado, String bimestre) {
    try {
        System.out.println("=====================================");
        System.out.println("idCurso recibido: " + idCurso);
        System.out.println("idGrado recibido: " + idGrado);
        System.out.println("bimestre recibido: " + bimestre);
        
        // Obtener datos con los parámetros
        List<Object[]> datos = matriculaDao.getNotasPorCursoYBimestre(idCurso, idGrado, bimestre);
        
        System.out.println("Cantidad de registros encontrados: " + (datos != null ? datos.size() : 0));
        
        if (datos == null || datos.isEmpty()) {
            System.out.println("⚠️ NO HAY DATOS con esos parámetros");
            return null;
        }
        
        System.out.println("Generando Excel con " + datos.size() + " registros");
        
        // Crear Excel con Apache POI
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Notas");
        
        // Cabecera
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Estudiante");
        header.createCell(1).setCellValue("Nota");
        header.createCell(2).setCellValue("Bimestre");
        
        // Datos
        int rowNum = 1;
        for (Object[] rowData : datos) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rowData[0] != null ? rowData[0].toString() : "");
            
            Object notaObj = rowData[1];
            if (notaObj != null) {
                if (notaObj instanceof Integer) {
                    row.createCell(1).setCellValue((Integer) notaObj);
                } else if (notaObj instanceof Double) {
                    row.createCell(1).setCellValue((Double) notaObj);
                }
            } else {
                row.createCell(1).setCellValue(0);
            }
            
            row.createCell(2).setCellValue(bimestre != null ? bimestre : "");
        }
        
        // Auto ajustar columnas
        for (int i = 0; i < 3; i++) {
            sheet.autoSizeColumn(i);
        }
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();
        
        System.out.println("Excel generado correctamente, tamaño: " + out.size() + " bytes");
        System.out.println("=====================================");
        
        return out.toByteArray();
        
    } catch (Exception e) {
        System.err.println("ERROR al generar Excel de notas: " + e.getMessage());
        e.printStackTrace();
        return null;
    }
}

}

