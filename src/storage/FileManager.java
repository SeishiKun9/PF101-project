package storage;

import model.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import static model.AssistanceRequest.decodeField;

public class FileManager {

    private static final int ITEM_START = 11;
    private static final List<String> FOOD_UNITS = List.of("kg", "pcs", "pack", "g", "liter", "ml");
    private static final List<String> MEDICAL_UNITS = List.of("pcs", "box", "bottle", "pack", "tablet", "ml");

    public static String resolveDataDir() {
        try {
            Path dir = Paths.get("").toAbsolutePath().normalize();
            while (dir != null) {
                if (Files.isDirectory(dir.resolve("data")) && Files.isDirectory(dir.resolve("src"))) {
                    return dir.resolve("data").toString();
                }
                dir = dir.getParent();
            }
        } catch (Exception ignored) { }
    
        return Paths.get("").toAbsolutePath().normalize().resolve("data").toString();
    }

    private static final String DATA_DIR = resolveDataDir();
    private static final Path USERS = Paths.get(DATA_DIR, "users.txt");
    private static final Path REQUESTS = Paths.get(DATA_DIR, "requests.txt");
    private static final Path STAFF_HISTORY = Paths.get(DATA_DIR, "staff_history.txt");
    private static final Path RESIDENT_HISTORY = Paths.get(DATA_DIR, "resident_history.txt");

    public FileManager() {
        ensureDataFiles();
    }

    private void ensureDataFiles() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            for (Path p : List.of(USERS, REQUESTS, STAFF_HISTORY, RESIDENT_HISTORY)) {
                if (Files.notExists(p)) Files.createFile(p);
            }
        } catch (IOException e) {
            System.err.println("Could not initialize data files: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------ FILE HELPERS

    private boolean appendLine(Path path, String line) {
        try {
            Files.write(path, List.of(line), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            System.err.println("Could not save to " + path.getFileName()
                    + ". Error: " + e.getMessage());
            return false;
        }
    }

    private boolean writeLines(Path path, List<String> lines) {
        try {
            List<String> validLines = new ArrayList<>();
            for (String l : lines) {
                if (l != null && !l.trim().isEmpty()) {
                    validLines.add(l);
                }
            }
            Files.write(path, validLines, StandardCharsets.UTF_8, 
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            System.err.println("Could not save to " + path.getFileName()
                    + ". Error: " + e.getMessage());
            return false;
        }
    }

    private static <T> List<T> readRecords(Path path, Function<String, T> parser) {
        List<T> records = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(path)) {
                if (line.trim().isEmpty()) continue;
                T record = parser.apply(line);
                if (record != null) records.add(record);
            }
        } catch (IOException e) {
            System.err.println("Error reading " + path.getFileName() + ": " + e.getMessage());
        }
        return records;
    }

    private static String generateNextId(String prefix, long minimum, List<String> ids) {
        long highest = minimum;
        for (String id : ids) {
            if (id == null || !id.toUpperCase().startsWith(prefix)) continue;
            String digits = id.substring(prefix.length());
            if (digits.length() >= 12) continue;
            try {
                highest = Math.max(highest, Long.parseLong(digits));
            } catch (NumberFormatException ignored) { }
        }
        return prefix + (highest + 1);
    }

    // ------------------------------------------------------------ USERS

    public List<User> readUsers() {
        return readRecords(USERS, this::parseUserLine);
    }

    private User parseUserLine(String line) {
        String[] f = line.split("\\|", -1);
        try {
            String name = decodeField(f[1]);
            String pass = decodeField(f[2]);
            switch (f[0]) {
                case "Resident":
                    return new Resident(name, pass, decodeField(f[3]), decodeField(f[4]), decodeField(f[5]));
                case "Admin":
                    return new Admin(name, pass, decodeField(f[3]));
                case "MedicalStaff":
                    return new MedicalStaff(name, pass, decodeField(f[3]));
                case "FoodStaff":
                    return new FoodStaff(name, pass, decodeField(f[3]));
                case "TransportationStaff":
                    return new TransportationStaff(name, pass, decodeField(f[3]));
                case "Staff":
                    return new Staff(name, pass, "Staff", decodeField(f[3]), decodeField(f[4]));
                default:
                    return null;
            }
        } catch (Exception e) {
            System.err.println("Skipping malformed user line: " + line);
            return null;
        }
    }

    public void saveUser(User user) {
        appendLine(USERS, user.toFileLine());
    }

    public void saveAllUsers(List<User> users) {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            lines.add(u.toFileLine());
        }
        writeLines(USERS, lines);
    }

    private String generateNextUserId(String prefix, long minimum, Class<? extends User> roleClass) {
        List<String> ids = new ArrayList<>();
        for (User u : readUsers()) {
            if (roleClass.isInstance(u)) {
                ids.add(u.getUserId());
            }
        }
        return generateNextId(prefix, minimum, ids);
    }

    public String nextResidentId() { return generateNextUserId("RSD", 3000, Resident.class); }
    public String nextStaffId()    { return generateNextUserId("STF", 2000, Staff.class); }
    public String nextAdminId()    { return generateNextUserId("ADM", 1000, Admin.class); }

    // ------------------------------------------------------------ REQUESTS

    public List<AssistanceRequest> readRequests() {
        return readRecords(REQUESTS, this::parseRequestLine);
    }

    public String nextRequestId() {
        List<String> ids = new ArrayList<>();
        for (AssistanceRequest r : readRequests()) {
            ids.add(r.getRequestId());
        }
        return generateNextId("REQ", 3999, ids);
    }

    private AssistanceRequest parseRequestLine(String line) {
        String[] f = line.split("\\|", -1);
        try {
            return "V2".equals(f[0]) ? parseCurrentRequestLine(f) : parseLegacyRequestLine(f);
        } catch (Exception e) {
            System.err.println("Skipping malformed request line: " + e.getMessage());
            return null;
        }
    }

    private AssistanceRequest parseCurrentRequestLine(String[] f) {
        String recordType = decodeField(f[1]);
        String requestId = decodeField(f[2]);
        String residentId = decodeField(f[3]);
        String type = decodeField(f[4]);
        String location = decodeField(f[5]);
        LocalDateTime timeNeeded = LocalDateTime.parse(f[6], AssistanceRequest.DT_FMT);
        String details = decodeField(f[7]);
        String status = decodeField(f[8]);
        LocalDateTime dateRequested = LocalDateTime.parse(f[9], AssistanceRequest.DT_FMT);

        String expectedType = switch (recordType) {
            case "FOOD" -> "Food";
            case "MEDICAL" -> "Medical";
            case "TRANSPORT" -> "Transportation";
            default -> throw new IllegalArgumentException("Unknown request type: " + recordType);
        };
        if (!expectedType.equalsIgnoreCase(type)) {
            throw new IllegalArgumentException("Request type does not match record type");
        }

        if (recordType.equals("TRANSPORT")) {
            if (f.length != 13 && f.length != 14) {
                throw new IllegalArgumentException("Invalid transportation request");
            }
            TransportationRequest req = new TransportationRequest(requestId, residentId,
                    decodeField(f[10]), decodeField(f[11]), Integer.parseInt(f[12]),
                    timeNeeded, details, status, dateRequested);
            req.setAssignedStaffId(extractAssignee(f, 13));
            return req;
        }

        int count = Integer.parseInt(f[10]);
        int itemsEnd = ITEM_START + count * 3;
        if (count < 0 || (f.length != itemsEnd && f.length != itemsEnd + 1)) {
            throw new IllegalArgumentException("Invalid item list");
        }
        String assignee = extractAssignee(f, itemsEnd);

        if (recordType.equals("FOOD")) {
            FoodRequest req = new FoodRequest(requestId, residentId, location, timeNeeded, details, status,
                    dateRequested, parseItems(f, count, FoodItem::new));
            req.setAssignedStaffId(assignee);
            return req;
        }
        MedicalRequest req = new MedicalRequest(requestId, residentId, location, timeNeeded, details, status,
                dateRequested, parseItems(f, count, MedicalItem::new));
        req.setAssignedStaffId(assignee);
        return req;
    }

    private AssistanceRequest parseLegacyRequestLine(String[] f) {
        String requestId = f[1];
        String residentId = f[2];
        String details = f[4];
        String status = f[5];
        LocalDateTime dateRequested = LocalDateTime.parse(f[6], AssistanceRequest.DT_FMT);
        switch (f[0]) {
            case "MEDICAL":
                return new MedicalRequest(requestId, residentId, "", dateRequested, details, status,
                        dateRequested, legacyItems(f[7], f[8], MEDICAL_UNITS, MedicalItem::new));
            case "FOOD":
                return new FoodRequest(requestId, residentId, "", dateRequested, details, status,
                        dateRequested, legacyItems(f[7], f[8], FOOD_UNITS, FoodItem::new));
            case "TRANSPORT":
                LocalDateTime whenNeeded = LocalDateTime.parse(f[9], AssistanceRequest.DT_FMT);
                return new TransportationRequest(requestId, residentId, f[7], f[8], 1, whenNeeded,
                        details, status, dateRequested);
            default:
                throw new IllegalArgumentException("Unknown legacy request type: " + f[0]);
        }
    }

    private interface ItemFactory<T> {
        T create(String name, int quantity, String unit);
    }

    private static <T> List<T> parseItems(String[] f, int count, ItemFactory<T> factory) {
        List<T> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int o = ITEM_START + i * 3;
            items.add(factory.create(decodeField(f[o]), Integer.parseInt(f[o + 1]), decodeField(f[o + 2])));
        }
        return items;
    }

    private static <T> List<T> legacyItems(String name, String quantity, List<String> units, ItemFactory<T> factory) {
        String[] parsed = parseLegacyQuantity(quantity, units);
        return List.of(factory.create(name, Integer.parseInt(parsed[0]), parsed[1]));
    }

    private static String[] parseLegacyQuantity(String quantity, List<String> units) {
        String[] parts = quantity.trim().split("\\s+", 2);
        try {
            int number = Integer.parseInt(parts[0]);
            if (number > 0) {
                String unit = parts.length > 1 ? parts[1].trim() : "";
                String match = "pcs";
                for (String u : units) {
                    if (u.equalsIgnoreCase(unit)) {
                        match = u;
                        break;
                    }
                }
                return new String[]{String.valueOf(number), match};
            }
        } catch (NumberFormatException ignored) { }
        return new String[]{"1", "pcs"};
    }

    private static String extractAssignee(String[] f, int i) {
        return i < f.length ? decodeField(f[i]).trim() : "";
    }

    public void saveRequest(AssistanceRequest request) {
        if (!appendLine(REQUESTS, request.toFileLine())) {
            throw new IllegalStateException("Could not save request to " + REQUESTS);
        }
    }

    public void updateRequestFile(AssistanceRequest updated) {
        List<AssistanceRequest> all = readRequests();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getRequestId().equals(updated.getRequestId())) {
                all.set(i, updated);
                rewriteRequests(all);
                return;
            }
        }
        System.err.println("Could not update missing request: " + updated.getRequestId());
    }

    public void deleteRequestFromFile(String requestId) {
        List<AssistanceRequest> all = readRequests();
        all.removeIf(r -> r.getRequestId().equals(requestId));
        rewriteRequests(all);
    }

    private void rewriteRequests(List<AssistanceRequest> all) {
        List<String> lines = new ArrayList<>();
        for (AssistanceRequest r : all) {
            lines.add(r.toFileLine());
        }
        writeLines(REQUESTS, lines);
    }

    public void updateResidentIdInRequests(String oldResidentId, String newResidentId) {
        List<AssistanceRequest> all = readRequests();
        for (AssistanceRequest request : all) {
            if (request.getResidentId().equals(oldResidentId)) {
                request.setResidentId(newResidentId);
            }
        }
        rewriteRequests(all);
        updateResidentIdInHistory(oldResidentId, newResidentId);
    }

    // ------------------------------------------------------------ RESIDENT HISTORY

    public void recordResidentRequestAction(Resident resident, AssistanceRequest request, String action) {
        ResidentRequestHistory history = new ResidentRequestHistory(resident.getResidentId(),
                request.getRequestId(), request.getType(), action, request.toDisplaySummary(), LocalDateTime.now());
        appendLine(RESIDENT_HISTORY, history.toFileLine());
    }

    public List<ResidentRequestHistory> readResidentRequestHistory() {
        return readRecords(RESIDENT_HISTORY, ResidentRequestHistory::fromFileLine);
    }

    private void updateResidentIdInHistory(String oldResidentId, String newResidentId) {
        List<String> lines = new ArrayList<>();
        for (ResidentRequestHistory e : readResidentRequestHistory()) {
            String id = oldResidentId.equals(e.getResidentId()) ? newResidentId : e.getResidentId();
            lines.add(new ResidentRequestHistory(id, e.getRequestId(), e.getRequestType(),
                    e.getAction(), e.getRequestSummary(), e.getUpdatedAt()).toFileLine());
        }
        writeLines(RESIDENT_HISTORY, lines);
    }

    // ------------------------------------------------------------ STAFF HISTORY

    public void recordStaffRequestAction(Staff staff, AssistanceRequest request, String status) {
        StaffRequestHistory history = new StaffRequestHistory(staff.getStaffId(), request.getRequestId(),
                request.getResidentId(), request.getType(), status, LocalDateTime.now());
        appendLine(STAFF_HISTORY, history.toFileLine());
    }

    public void recordAdminRequestAction(String adminId, AssistanceRequest request, String action) {
        StaffRequestHistory history = new StaffRequestHistory(adminId, request.getRequestId(),
                request.getResidentId(), request.getType(), action, LocalDateTime.now());
        appendLine(STAFF_HISTORY, history.toFileLine());
    }

    public List<StaffRequestHistory> readStaffRequestHistory() {
        return readRecords(STAFF_HISTORY, FileManager::parseStaffHistoryLine);
    }

    private static StaffRequestHistory parseStaffHistoryLine(String line) {
        String[] f = line.split("\\|", -1);
        if (f.length != 6) return null;
        try {
            return new StaffRequestHistory(f[0], f[1], f[2], f[3], f[4], 
                    LocalDateTime.parse(f[5], AssistanceRequest.DT_FMT));
        } catch (Exception e) {
            return null;
        }
    }

    public void updateStaffIdInHistory(String oldStaffId, String newStaffId) {
        List<String> lines = new ArrayList<>();
        for (StaffRequestHistory e : readStaffRequestHistory()) {
            if (e.getStaffId().equals(oldStaffId)) e.setStaffId(newStaffId);
            lines.add(e.toFileLine());
        }
        writeLines(STAFF_HISTORY, lines);
    }
}