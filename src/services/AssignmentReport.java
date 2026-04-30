package services;

import reports.Report;

public class AssignmentReport {
    private Report report;
    private String workerId;

    public AssignmentReport(Report report, String workerId) {
        this.report = report;
        this.workerId = workerId;
    }

    public Report getReport() { return report; }
    public String getWorkerId() { return workerId; }

    @Override
    public String toString() {
        return "Assignment: [" + report.getId() + "] to Worker [" + workerId + "]";
    }
}
