package com.optistock.movimiento;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovimientoRepository extends JpaRepository<MovimientoInventario, Integer> {
    List<MovimientoInventario> findAllByOrderByFechaDesc();

    List<MovimientoInventario> findByProductoIdProducto(Integer idProducto);
}