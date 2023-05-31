package pers.summer502.j8zyeinkappstore.model;

import java.util.ArrayList;
import java.util.List;

public class ServerLogDTO {
    private String lastlogid;

    private final List<LogInfo> logs = new ArrayList<>();

    public void addLogs(String logId, String logStr) {
        LogInfo lastLogInfo = new LogInfo(logId, logStr);
        this.logs.add(lastLogInfo);
        this.lastlogid = logId;
    }

    public List<LogInfo> getLogs() {
        return logs;
    }

    public String getLastlogid() {
        return lastlogid;
    }

    private static class LogInfo {
        private String id;
        private String logStr;

        public LogInfo(String id, String logStr) {
            this.id = id;
            this.logStr = logStr;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLogStr() {
            return logStr;
        }

        public void setLogStr(String logStr) {
            this.logStr = logStr;
        }
    }
}
