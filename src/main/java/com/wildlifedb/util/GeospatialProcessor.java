package com.wildlifedb.util;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTWriter;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

public class GeospatialProcessor {
    
    private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/test";
    private static final String MYSQL_USER = "test";
    private static final String MYSQL_PASSWORD = "test";
    
    public static void main(String[] args) {
        try {
            // Read shapefile and process geospatial data
            processShapefileData();
            
            // Connect to MySQL and process database operations
            processDatabaseOperations();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void processShapefileData() throws Exception {
        // Read shapefile
        File shapeFile = new File("project\\backend\\src\\main\\java\\team15\\backend\\LocationPopulation\\tl_2024_06_place.shp");
        
        // Check if shapefile exists
        if (!shapeFile.exists()) {
            System.err.println("Shapefile not found: " + shapeFile.getAbsolutePath());
            System.out.println("Please ensure the shapefile is in the correct location.");
            return;
        }
        
        // Check for required shapefile components
        File shxFile = new File(shapeFile.getParent(), "tl_2024_06_place.shx");
        File dbfFile = new File(shapeFile.getParent(), "tl_2024_06_place.dbf");
        File prjFile = new File(shapeFile.getParent(), "tl_2024_06_place.prj");
        
        System.out.println("Checking shapefile components:");
        System.out.println(".shp file: " + (shapeFile.exists() ? "Found" : "Missing"));
        System.out.println(".shx file: " + (shxFile.exists() ? "Found" : "Missing (will continue without index)"));
        System.out.println(".dbf file: " + (dbfFile.exists() ? "Found" : "Missing"));
        System.out.println(".prj file: " + (prjFile.exists() ? "Found" : "Missing (will assume WGS84)"));
        
        Map<String, Object> map = new HashMap<>();
        map.put("url", shapeFile.toURI().toURL());
        
        DataStore dataStore = DataStoreFinder.getDataStore(map);
        if (dataStore == null) {
            System.err.println("Could not create datastore for shapefile");
            return;
        }
        
        String typeName = dataStore.getTypeNames()[0];
        
        FeatureSource<SimpleFeatureType, SimpleFeature> source = 
            dataStore.getFeatureSource(typeName);
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = 
            source.getFeatures();
        
        // Handle CRS transformation safely
        CoordinateReferenceSystem sourceCRS = source.getSchema().getCoordinateReferenceSystem();
        CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326");
        MathTransform transform = null;
        
        if (sourceCRS != null) {
            System.out.println("Source CRS: " + sourceCRS.getName());
            try {
                // Try lenient transformation first (allows approximate transformations)
                transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
                System.out.println("Using lenient transformation");
            } catch (Exception e) {
                System.out.println("Lenient transformation failed, assuming data is already in WGS84");
                // Create identity transform as fallback
                transform = CRS.findMathTransform(targetCRS, targetCRS, false);
            }
        } else {
            System.out.println("No CRS information found, assuming data is already in WGS84 (EPSG:4326)");
            // Create identity transform (no transformation needed)
            transform = CRS.findMathTransform(targetCRS, targetCRS, false);
        }
        
        // Create GeoJSON output
        writeToGeoJSON(collection, transform);
        
        // Print first 5 features (equivalent to gdf.head(5))
        printFirstFiveFeatures(collection, transform);
        
        dataStore.dispose();
    }
    
    private static void writeToGeoJSON(
            FeatureCollection<SimpleFeatureType, SimpleFeature> collection,
            MathTransform transform) throws Exception {
        
        // Simple GeoJSON export using manual JSON writing
        File geoJsonFile = new File("project\\backend\\src\\main\\java\\team15\\backend\\LocationPopulation\\us_cities.geojson");
        try (FileWriter writer = new FileWriter(geoJsonFile)) {
            writer.write("{\n\"type\": \"FeatureCollection\",\n\"features\": [\n");
            
            WKTWriter wktWriter = new WKTWriter();
            boolean first = true;
            
            try (FeatureIterator<SimpleFeature> features = collection.features()) {
                while (features.hasNext()) {
                    SimpleFeature feature = features.next();
                    
                    if (!first) {
                        writer.write(",\n");
                    }
                    first = false;
                    
                    // Get geometry and transform it
                    Geometry geometry = (Geometry) feature.getDefaultGeometry();
                    if (geometry != null) {
                        Geometry transformedGeometry = JTS.transform(geometry, transform);
                        
                        // Simple GeoJSON feature format
                        writer.write("{\n");
                        writer.write("  \"type\": \"Feature\",\n");
                        writer.write("  \"properties\": {\n");
                        writer.write("    \"name\": \"" + feature.getAttribute("NAMELSAD") + "\"\n");
                        writer.write("  },\n");
                        writer.write("  \"geometry\": " + geometryToGeoJSON(transformedGeometry) + "\n");
                        writer.write("}");
                    }
                }
            }
            
            writer.write("\n]\n}");
        }
        
        System.out.println("GeoJSON file created: us_cities.geojson");
    }
    
    private static String geometryToGeoJSON(Geometry geometry) {
        if (geometry == null) return "null";
        
        String geomType = geometry.getGeometryType();
        
        switch (geomType) {
            case "Point":
                return String.format("{\"type\": \"Point\", \"coordinates\": [%f, %f]}", 
                    geometry.getCoordinate().x, geometry.getCoordinate().y);
                    
            case "Polygon":
                return polygonToGeoJSON(geometry);
                
            case "MultiPolygon":
                return multiPolygonToGeoJSON(geometry);
                
            case "LineString":
                return lineStringToGeoJSON(geometry);
                
            case "MultiLineString":
                return multiLineStringToGeoJSON(geometry);
                
            default:
                // For unsupported types, return a simple point at the centroid
                org.locationtech.jts.geom.Coordinate centroid = geometry.getCentroid().getCoordinate();
                return String.format("{\"type\": \"Point\", \"coordinates\": [%f, %f]}", 
                    centroid.x, centroid.y);
        }
    }
    
    private static String polygonToGeoJSON(Geometry polygon) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\": \"Polygon\", \"coordinates\": [");
        
        org.locationtech.jts.geom.Coordinate[] coords = polygon.getCoordinates();
        sb.append("[");
        for (int i = 0; i < coords.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(String.format("[%f, %f]", coords[i].x, coords[i].y));
        }
        sb.append("]");
        sb.append("]}");
        
        return sb.toString();
    }
    
    private static String multiPolygonToGeoJSON(Geometry multiPolygon) {
        // Simplified - just use the centroid for complex geometries
        org.locationtech.jts.geom.Coordinate centroid = multiPolygon.getCentroid().getCoordinate();
        return String.format("{\"type\": \"Point\", \"coordinates\": [%f, %f]}", 
            centroid.x, centroid.y);
    }
    
    private static String lineStringToGeoJSON(Geometry lineString) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\": \"LineString\", \"coordinates\": [");
        
        org.locationtech.jts.geom.Coordinate[] coords = lineString.getCoordinates();
        for (int i = 0; i < coords.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(String.format("[%f, %f]", coords[i].x, coords[i].y));
        }
        sb.append("]}");
        
        return sb.toString();
    }
    
    private static String multiLineStringToGeoJSON(Geometry multiLineString) {
        // Simplified - just use the centroid for complex geometries
        org.locationtech.jts.geom.Coordinate centroid = multiLineString.getCentroid().getCoordinate();
        return String.format("{\"type\": \"Point\", \"coordinates\": [%f, %f]}", 
            centroid.x, centroid.y);
    }
    
    private static void printFirstFiveFeatures(
            FeatureCollection<SimpleFeatureType, SimpleFeature> collection,
            MathTransform transform) throws Exception {
        
        System.out.println("First 5 features:");
        try (FeatureIterator<SimpleFeature> features = collection.features()) {
            int count = 0;
            while (features.hasNext() && count < 5) {
                SimpleFeature feature = features.next();
                System.out.println("Feature " + count + ": " + feature.getAttributes());
                count++;
            }
        }
    }
    
    private static void processDatabaseOperations() throws Exception {
        Connection connection = null;
        PreparedStatement selectStmt = null;
        PreparedStatement insertStmt = null;
        
        try {
            // Connect to MySQL
            connection = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASSWORD);
            
            // Disable autocommit for transaction control
            connection.setAutoCommit(false);
            
            System.out.println("Connected to MySQL successfully!");
            
            // Process shapefile data for database insertion
            insertGeospatialData(connection);
            
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            // Rollback transaction on error
            if (connection != null) {
                try {
                    connection.rollback();
                    System.out.println("Transaction rolled back due to error");
                } catch (SQLException rollbackEx) {
                    System.err.println("Error during rollback: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            // Clean up resources
            if (selectStmt != null) selectStmt.close();
            if (insertStmt != null) insertStmt.close();
            if (connection != null) {
                // Restore autocommit before closing
                connection.setAutoCommit(true);
                connection.close();
            }
        }
    }
    
    private static void insertGeospatialData(Connection connection) throws Exception {
        // Read shapefile again for database insertion
        File shapeFile = new File("project\\backend\\src\\main\\java\\team15\\backend\\LocationPopulation\\tl_2024_06_place.shp");
        
        if (!shapeFile.exists()) {
            System.err.println("Shapefile not found for database insertion: " + shapeFile.getAbsolutePath());
            return;
        }
        
        Map<String, Object> map = new HashMap<>();
        map.put("url", shapeFile.toURI().toURL());
        
        DataStore dataStore = DataStoreFinder.getDataStore(map);
        if (dataStore == null) {
            System.err.println("Could not create datastore for database insertion");
            return;
        }
        
        String typeName = dataStore.getTypeNames()[0];
        
        FeatureSource<SimpleFeatureType, SimpleFeature> source = 
            dataStore.getFeatureSource(typeName);
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = 
            source.getFeatures();
        
        // Prepare INSERT statement - let MySQL auto-generate the ID
        String sql = "INSERT INTO `location`(`location_id`, `biome`, `city`, `geometric_shape`, `state`) " +
                    "VALUES (?, 'NA', ?, ST_GeomFromText(?, 4326), 'CA')";
        
        PreparedStatement insertStmt = connection.prepareStatement(sql);
        
        // Handle CRS transformation safely
        CoordinateReferenceSystem sourceCRS = source.getSchema().getCoordinateReferenceSystem();
        CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326");
        MathTransform transform = null;
        
        if (sourceCRS != null) {
            try {
                // Try lenient transformation first
                transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
                System.out.println("Using lenient transformation for database insertion");
            } catch (Exception e) {
                System.out.println("Transformation failed, assuming data is already in WGS84");
                // Create identity transform as fallback
                transform = CRS.findMathTransform(targetCRS, targetCRS, false);
            }
        } else {
            // Create identity transform if no CRS info
            transform = CRS.findMathTransform(targetCRS, targetCRS, false);
        }
        
        WKTWriter wktWriter = new WKTWriter();
        int index = 0;
        
        try (FeatureIterator<SimpleFeature> features = collection.features()) {
            while (features.hasNext()) {
                SimpleFeature feature = features.next();
                
                // Get geometry and transform it
                Geometry geometry = (Geometry) feature.getDefaultGeometry();
                if (geometry != null) {
                    Geometry transformedGeometry = null;
                    if (sourceCRS != null) {
                        transformedGeometry = JTS.transform(geometry, transform);
                    } else {
                        // No transformation needed if no CRS info
                        transformedGeometry = geometry;
                    }
                    
                    String geometryWKT = wktWriter.write(transformedGeometry);
                    
                    // Check WKT length and truncate if necessary
                    if (geometryWKT.length() > 65535) { // Assuming TEXT column (65535 chars)
                        System.out.println("Warning: Geometry WKT too long (" + geometryWKT.length() + 
                                         " chars), using centroid instead for feature " + index);
                        // Use centroid as a simpler geometry
                        org.locationtech.jts.geom.Point centroid = transformedGeometry.getCentroid();
                        geometryWKT = wktWriter.write(centroid);
                    }
                    
                    // Get city name (adjust attribute name as needed)
                    String cityName = (String) feature.getAttribute("NAMELSAD");
                    if (cityName != null) {
                        cityName = cityName.replace("'", ""); // Clean single quotes
                    } else {
                        cityName = "Unknown"; // Default value
                    }
                    
                    // Set parameters and execute
                    insertStmt.setInt(1, index);
                    insertStmt.setString(2, cityName);
                    insertStmt.setString(3, geometryWKT);
                    
                    insertStmt.executeUpdate();
                    index++;
                }
            }
        }
        
        // Commit transaction
        connection.commit();
        insertStmt.close();
        dataStore.dispose();
        
        System.out.println("Inserted " + index + " records into location table");
    }
}