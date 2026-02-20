package edu.eci.arsw.blueprints.persistence;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;


@Repository
@Primary  // ← Esta anotación hace que Spring use esta implementación en lugar de InMemory
public class PostgresBlueprintPersistence implements BlueprintPersistence {


    private final JdbcTemplate jdbc;


    // Inyectamos JdbcTemplate a través del constructor (Spring lo hará automáticamente)

    public PostgresBlueprintPersistence(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }


    /**
     * Guarda un nuevo blueprint en la base de datos.
     * 1. Verificar si ya existe (UNIQUE constraint en author+name)
     * 2. Insertar blueprint en tabla blueprints (obtener ID autogenerado)
     * 3. Insertar todos los puntos en tabla points (con orden correcto)
     */
    @Override
    public void saveBlueprint(Blueprint bp) throws BlueprintPersistenceException {

        
        String checkSql = "SELECT COUNT(*) FROM blueprints WHERE author = ? AND name = ?";
        Integer count = jdbc.queryForObject(checkSql, Integer.class, bp.getAuthor(), bp.getName());
        
        if (count != null && count > 0) {
            throw new BlueprintPersistenceException(
                "Blueprint already exists: " + bp.getAuthor() + "/" + bp.getName()
            );
        }

        String insertBlueprintSql = "INSERT INTO blueprints (author, name) VALUES (?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                insertBlueprintSql, 
                Statement.RETURN_GENERATED_KEYS 
            );
            ps.setString(1, bp.getAuthor());
            ps.setString(2, bp.getName());
            return ps;
        }, keyHolder);

        Long blueprintId = keyHolder.getKey().longValue();

 
        
        if (!bp.getPoints().isEmpty()) {
            String insertPointSql = 
                "INSERT INTO points (x, y, point_order, blueprint_id) VALUES (?, ?, ?, ?)";
            
            List<Point> points = bp.getPoints();
            for (int i = 0; i < points.size(); i++) {
                Point p = points.get(i);
                jdbc.update(insertPointSql, p.x(), p.y(), i, blueprintId);
            }
        }
    }

 
    @Override
    public Blueprint getBlueprint(String author, String name) throws BlueprintNotFoundException {
        try { 
            String blueprintSql = "SELECT id, author, name FROM blueprints WHERE author = ? AND name = ?";
            Map<String, Object> row = jdbc.queryForMap(blueprintSql, author, name);
            Long blueprintId = ((Number) row.get("id")).longValue();
 
            
            String pointsSql = 
                "SELECT x, y FROM points WHERE blueprint_id = ? ORDER BY point_order";
            
            List<Point> points = jdbc.query(pointsSql, new PointRowMapper(), blueprintId);

         
            return new Blueprint(author, name, points);

        } catch (EmptyResultDataAccessException e) {
            throw new BlueprintNotFoundException(
                "Blueprint not found: " + author + "/" + name
            );
        }
    }

    // Obtiene todos los blueprints de un autor específico.
  
    @Override
    public Set<Blueprint> getBlueprintsByAuthor(String author) throws BlueprintNotFoundException {

        
        String blueprintsSql = "SELECT id, name FROM blueprints WHERE author = ?";
        List<Map<String, Object>> rows = jdbc.queryForList(blueprintsSql, author);

        if (rows.isEmpty()) {
            throw new BlueprintNotFoundException("No blueprints for author: " + author);
        }

        Set<Blueprint> result = new HashSet<>();
        
        for (Map<String, Object> row : rows) {
            Long blueprintId = ((Number) row.get("id")).longValue();
            String name = (String) row.get("name");

            String pointsSql = 
                "SELECT x, y FROM points WHERE blueprint_id = ? ORDER BY point_order";
            List<Point> points = jdbc.query(pointsSql, new PointRowMapper(), blueprintId);

            result.add(new Blueprint(author, name, points));
        }

        return result;
    }

    @Override
    public Set<Blueprint> getAllBlueprints() {
        String blueprintsSql = "SELECT id, author, name FROM blueprints";
        List<Map<String, Object>> rows = jdbc.queryForList(blueprintsSql);

    
        Set<Blueprint> result = new HashSet<>();

        for (Map<String, Object> row : rows) {
            Long blueprintId = ((Number) row.get("id")).longValue();
            String author = (String) row.get("author");
            String name = (String) row.get("name");

            String pointsSql = 
                "SELECT x, y FROM points WHERE blueprint_id = ? ORDER BY point_order";
            List<Point> points = jdbc.query(pointsSql, new PointRowMapper(), blueprintId);

            result.add(new Blueprint(author, name, points));
        }

        return result;
    }

    // Agrega un nuevo punto a un blueprint existente (al final de la lista de puntos).



    @Override
    public void addPoint(String author, String name, int x, int y) throws BlueprintNotFoundException {
        try {
         
            String getBlueprintIdSql = 
                "SELECT id FROM blueprints WHERE author = ? AND name = ?";
            Long blueprintId = jdbc.queryForObject(getBlueprintIdSql, Long.class, author, name);

      
            
            String getMaxOrderSql = 
                "SELECT COALESCE(MAX(point_order), -1) FROM points WHERE blueprint_id = ?";
            Integer maxOrder = jdbc.queryForObject(getMaxOrderSql, Integer.class, blueprintId);

 
            int newOrder = (maxOrder != null ? maxOrder : -1) + 1;
            
            String insertPointSql = 
                "INSERT INTO points (x, y, point_order, blueprint_id) VALUES (?, ?, ?, ?)";
            jdbc.update(insertPointSql, x, y, newOrder, blueprintId);

        } catch (EmptyResultDataAccessException e) {
            throw new BlueprintNotFoundException(
                "Blueprint not found: " + author + "/" + name
            );
        }
    }

    // RowMapper para convertir filas de la tabla points en objetos Point (esto es una clase auxiliar)

   
    private static class PointRowMapper implements RowMapper<Point> {
        @Override
        public Point mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Point(rs.getInt("x"), rs.getInt("y"));
        }
    }
}
