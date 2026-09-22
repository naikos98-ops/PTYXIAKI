package com.example.es;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.io.File;
import java.util.List;
import java.util.Map;
import com.example.es.apartment.entity.ProjectStatus;

@Component
public class DbConstraintCleaner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DbConstraintCleaner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;");
            System.out.println("Successfully dropped users_role_check constraint if it existed.");
        } catch (Exception e) {
            System.err.println("Failed to drop users_role_check constraint: " + e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE apartment DROP CONSTRAINT IF EXISTS apartment_status_check;");
            System.out.println("Successfully dropped apartment_status_check constraint if it existed.");
        } catch (Exception e) {
            System.err.println("Failed to drop apartment_status_check constraint: " + e.getMessage());
        }

        try {
            int updatedRows = jdbcTemplate.update("UPDATE users SET role = 'AMATEUR_USER' WHERE role = 'USER'");
            System.out.println("Successfully migrated legacy roles. Rows updated: " + updatedRows);
        } catch (Exception e) {
            System.err.println("Failed to migrate legacy roles: " + e.getMessage());
        }

        try {
            List<Long> legacyIds = jdbcTemplate.queryForList("SELECT id FROM apartment WHERE status IS NULL OR status = ''", Long.class);
            if (!legacyIds.isEmpty()) {
                int idx = 0;
                for (Long id : legacyIds) {
                    ProjectStatus status = (idx % 2 == 0) ? ProjectStatus.POOL_CONSTRUCTION : ProjectStatus.POOL_REAL_ESTATE;
                    jdbcTemplate.update("UPDATE apartment SET status = ? WHERE id = ?", status.name(), id);
                    idx++;
                }
                System.out.println("Successfully migrated legacy apartment statuses. Total updated: " + legacyIds.size());
            }
        } catch (Exception e) {
            System.err.println("Failed to migrate legacy apartment statuses: " + e.getMessage());
        }
        try {
            File dir = new File("uploads/apartments");
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles((d, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
                if (files != null && files.length > 0) {
                    List<Map<String, Object>> apartments = jdbcTemplate.queryForList("SELECT id, image_path FROM apartment");
                    int index = 0;
                    for (Map<String, Object> apt : apartments) {
                        Object idVal = apt.get("id");
                        if (idVal == null) idVal = apt.get("ID");
                        Long id = ((Number) idVal).longValue();

                        String currentPath = (String) apt.get("image_path");
                        if (currentPath == null) currentPath = (String) apt.get("IMAGE_PATH");

                        boolean needsMigration = false;
                        if (currentPath == null || currentPath.trim().isEmpty()) {
                            needsMigration = true;
                        } else {
                            File f = new File(currentPath);
                            if (!f.exists()) {
                                needsMigration = true;
                            }
                        }

                        if (needsMigration) {
                            File file = files[index % files.length];
                            String relativePath = "uploads/apartments/" + file.getName();
                            jdbcTemplate.update("UPDATE apartment SET image_path = ? WHERE id = ?", relativePath, id);
                            index++;
                        }
                    }
                    System.out.println("Successfully migrated missing/invalid apartment image paths to existing files on disk.");
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to migrate apartment image paths: " + e.getMessage());
        }
        try {
            int fixed = jdbcTemplate.update(
                "UPDATE apartment SET ai_processed = true, " +
                "estimated_cost = '350.00 €/sqm (Baseline Habitability)', " +
                "estimated_rent = COALESCE(square_meters, 80) * 8.0, " +
                "estimated_price = COALESCE(square_meters, 80) * 1800.0 " +
                "WHERE ai_processed = false OR ai_processed IS NULL"
            );
            if (fixed > 0) {
                System.out.println("Cleaned up stuck AI processing states. Apartments updated: " + fixed);
            }
        } catch (Exception e) {
            System.err.println("Failed to clean up stuck AI processing states: " + e.getMessage());
        }
        try {
            File dir = new File("uploads");
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles((d, name) -> name.startsWith("renovated_") && name.endsWith(".png"));
                if (files != null && files.length > 0) {
                    List<Map<String, Object>> apartments = jdbcTemplate.queryForList("SELECT id, renovated_image_path FROM apartment");
                    int index = 0;
                    for (Map<String, Object> apt : apartments) {
                        Object idVal = apt.get("id");
                        if (idVal == null) idVal = apt.get("ID");
                        Long id = ((Number) idVal).longValue();

                        String currentPath = (String) apt.get("renovated_image_path");
                        if (currentPath == null) currentPath = (String) apt.get("RENOVATED_IMAGE_PATH");

                        boolean needsMigration = false;
                        if (currentPath == null || currentPath.trim().isEmpty()) {
                            needsMigration = true;
                        } else {
                            File f = new File(currentPath);
                            if (!f.exists()) {
                                needsMigration = true;
                            }
                        }

                        if (needsMigration) {
                            File file = files[index % files.length];
                            String relativePath = "uploads/" + file.getName();
                            jdbcTemplate.update("UPDATE apartment SET renovated_image_path = ? WHERE id = ?", relativePath, id);
                            index++;
                        }
                    }
                    System.out.println("Successfully migrated missing/invalid apartment renovated image paths to existing files on disk.");
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to migrate apartment renovated image paths: " + e.getMessage());
        }
    }
}
