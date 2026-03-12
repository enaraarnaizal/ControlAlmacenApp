package com.example.controlalmacen.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.controlalmacen.model.Albaran;

import java.util.List;

@Dao
public interface AlbaranDao {

    @Insert
    long insert(Albaran albaran);

    @Update
    void update(Albaran albaran);

    // Obtener todos los albaranes ordenados por fecha descendente
    @Query("SELECT * FROM albaranes ORDER BY fechaMs DESC")
    List<Albaran> getAll();

    // Obtener albaranes en un rango de fechas
    @Query("SELECT * FROM albaranes WHERE fechaMs >= :fechaInicio AND fechaMs <= :fechaFin ORDER BY fechaMs DESC")
    List<Albaran> getAlbaranesPorRango(long fechaInicio, long fechaFin);

    // Obtener albaran por ID
    @Query("SELECT * FROM albaranes WHERE id = :id LIMIT 1")
    Albaran getById(long id);

    // Eliminar albaran
    @Query("DELETE FROM albaranes WHERE id = :id")
    void deleteById(long id);

    // Obtener albaranes por lista de IDs
    @Query("SELECT * FROM albaranes WHERE id IN (:ids) ORDER BY fechaMs DESC")
    List<Albaran> getAlbaranesPorIds(List<Long> ids);

    // Contar albaranes pagados en un rango
    @Query("SELECT COUNT(*) FROM albaranes WHERE fechaMs >= :fechaInicio AND fechaMs <= :fechaFin AND pagado = 1")
    int contarAlbaranesPagados(long fechaInicio, long fechaFin);

    // Contar albaranes sin pagar en un rango
    @Query("SELECT COUNT(*) FROM albaranes WHERE fechaMs >= :fechaInicio AND fechaMs <= :fechaFin AND pagado = 0")
    int contarAlbaranesSinPagar(long fechaInicio, long fechaFin);
}

