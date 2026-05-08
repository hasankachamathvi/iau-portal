package com.slt.iau_portal.util;

import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.slt.iau_portal.model.Complaint;

public class ExportUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static String exportComplaintsToCSV(List<Complaint> complaints) {
        StringWriter csv = new StringWriter();
        
        // Write header
        csv.append("CRN,Category,Status,Escalated,Location,Created,Updated,Description\n");
        
        // Write rows
        for (Complaint complaint : complaints) {
            csv.append(escapeCsvField(complaint.getCrn())).append(",");
            csv.append(escapeCsvField(complaint.getCategory())).append(",");
            csv.append(escapeCsvField(complaint.getStatus())).append(",");
            csv.append(complaint.getEscalated() ? "Yes" : "No").append(",");
            csv.append(escapeCsvField(complaint.getLocation())).append(",");
            csv.append(complaint.getCreatedAt().format(DATE_FORMATTER)).append(",");
            csv.append(complaint.getUpdatedAt().format(DATE_FORMATTER)).append(",");
            csv.append(escapeCsvField(complaint.getDescription())).append("\n");
        }
        
        return csv.toString();
    }

    private static String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}
