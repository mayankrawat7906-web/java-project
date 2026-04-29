package services;

import java.util.ArrayList;
import java.util.List;
import reports.Report;
import users.Worker;

public class AssignmentService {
    private List<Worker> workers;
    private List<AssignmentReport> assignments = new ArrayList<>();
    private int rrIndex = 0;

    public AssignmentService(List<Worker> workers) {
        this.workers = workers;
    }

    public boolean assignReport(Report report) {
        if (workers.isEmpty()) return false;

        // 1. Priority: Workers with 0 reports (assigned via Round Robin)
        for (int i = 0; i < workers.size(); i++) {
            int idx = (rrIndex + i) % workers.size();
            Worker w = workers.get(idx);
            if (w.getAssignedCount() == 0) {
                assignToWorker(report, w);
                rrIndex = (idx + 1) % workers.size();
                return true;
            }
        }

        // 2. Priority: Worker whose LAST assigned report is within 5km
        Worker closestUnder5km = null;
        double minDistanceUnder5km = Double.MAX_VALUE;

        for (Worker w : workers) {
            Report lastReport = findLastReportForWorker(w.getId());
            if (lastReport != null) {
                double dist = reports.GeoUtils.haversineKm(report.getLat(), report.getLon(),
                                                           lastReport.getLat(), lastReport.getLon());
                if (dist < 5.0 && dist < minDistanceUnder5km) {
                    minDistanceUnder5km = dist;
                    closestUnder5km = w;
                }
            }
        }

        if (closestUnder5km != null) {
            assignToWorker(report, closestUnder5km);
            return true;
        }

        // 3. Fallback: Worker whose LAST assigned report is closest overall
        Worker overallClosest = null;
        double minOverallDist = Double.MAX_VALUE;

        for (Worker w : workers) {
            Report lastReport = findLastReportForWorker(w.getId());
            if (lastReport != null) {
                double dist = reports.GeoUtils.haversineKm(report.getLat(), report.getLon(),
                                                           lastReport.getLat(), lastReport.getLon());
                if (dist < minOverallDist) {
                    minOverallDist = dist;
                    overallClosest = w;
                }
            }
        }

        if (overallClosest != null) {
            assignToWorker(report, overallClosest);
            return true;
        } else {
            // Ultimate fallback
            assignToWorker(report, workers.get(rrIndex));
            rrIndex = (rrIndex + 1) % workers.size();
            return true;
        }
    }

    public void assignPendingReports(List<Report> allReports) {
        if (workers.isEmpty()) return;
        for (Report r : allReports) {
            if (Report.PENDING.equals(r.getStatus()) && r.getAssignedWorkerId() == null) {
                assignReport(r);
            }
        }
    }

    private void assignToWorker(Report report, Worker w) {
        report.setAssignedWorkerId(w.getId());
        report.setStatus(Report.ASSIGNED);
        w.incrementAssignedCount();
        assignments.add(new AssignmentReport(report, w.getId()));
    }

    private Report findLastReportForWorker(String wid) {
        for (int i = assignments.size() - 1; i >= 0; i--) {
            if (assignments.get(i).getWorkerId().equals(wid)) {
                return assignments.get(i).getReport();
            }
        }
        return null;
    }

    public boolean reassignReport(Report report, String workerId, List<Worker> workersList) {
        Worker newWorker = null;
        for (Worker w : workersList) {
            if (w.getId().equals(workerId)) {
                newWorker = w;
                break;
            }
        }
        if (newWorker != null) {
            report.setAssignedWorkerId(newWorker.getId());
            newWorker.incrementAssignedCount();
            assignments.add(new AssignmentReport(report, newWorker.getId()));
            return true;
        }
        return false;
    }

    public void showAssignmentReports() {
        System.out.println("\n--- Assignment History ---");
        for (AssignmentReport ar : assignments) {
            System.out.println("  " + ar);
        }
    }

    public List<AssignmentReport> getAssignments() { return assignments; }
}
