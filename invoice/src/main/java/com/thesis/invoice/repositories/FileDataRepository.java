package com.thesis.invoice.repositories;

import com.thesis.invoice.entities.FileData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

public interface FileDataRepository extends JpaRepository<FileData, Long> {
    Optional<FileData> findByName(String name);
    List<FileData> findFileDataByDateBetween(Date date1, Date date2);
}
