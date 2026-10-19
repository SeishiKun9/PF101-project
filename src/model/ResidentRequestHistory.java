package model;

import java.time.LocalDateTime;

/** One resident action on a request (e.g. cancelling), saved to resident_history.txt. */
public class ResidentRequestHistory {
    private final String residentId;
    private final String requestId;
    private final String requestType;
    private final String action;
    private final String requestSummary;
    private final LocalDateTime updatedAt;

    public ResidentRequestHistory(String residentId, String requestId, String requestType,
                                  String action, String requestSummary, LocalDateTime updatedAt) {
        this.residentId = residentId;
        this.requestId = requestId;
        this.requestType = requestType;
        this.action = action;
        this.requestSummary = requestSummary == null ? "" : requestSummary;
        this.updatedAt = updatedAt;
    }

    public String getResidentId() { return residentId; }
    public String getRequestId() { return requestId; }
    public String getRequestType() { return requestType; }
    public String getAction() { return action; }
    public String getRequestSummary() { return requestSummary; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    /** Writes one line: resident|request|type|action|summary|timestamp */
    public String toFileLine() {
        return String.join("|", residentId, requestId, requestType, action,
                AssistanceRequest.encodeField(requestSummary),
                updatedAt.format(AssistanceRequest.DT_FMT));
    }

    /** Reads one line written by toFileLine(). Returns null if the line is malformed. */
    public static ResidentRequestHistory fromFileLine(String line) {
        String[] f = line.split("\\|", -1);
        if (f.length != 6) return null;
        try {
            return new ResidentRequestHistory(f[0], f[1], f[2], f[3],
                    AssistanceRequest.decodeField(f[4]),
                    LocalDateTime.parse(f[5], AssistanceRequest.DT_FMT));
        } catch (Exception e) {
            return null;
        }
    }
}