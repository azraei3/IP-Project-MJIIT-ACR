package com.example.demo.service;

import com.example.demo.model.LogEntry;
import com.example.demo.model.User;
import com.example.demo.repository.LogRepository; // You need to create this Interface interface LogRepository extends JpaRepository<LogEntry, Long> {}
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.data.domain.Sort;

@Service
public class LogService {
    @Autowired private LogRepository logRepository;

    public void log(User user, String action, String details) {
        LogEntry log = new LogEntry(user, action, details);
        logRepository.save(log);
    }

    public List<LogEntry> getAllLogs() {
        return logRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
    }
}