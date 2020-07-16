package com.idess.remote_scheduler.remote_scheduler.repository;

import com.idess.remote_scheduler.remote_scheduler.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient,Long> {
    Optional<Patient> findPatientById(int id);
    @Query(value = "SELECT * FROM patients where id = 1?",nativeQuery = true)
    Patient findById(int id);
    
    Patient findPatientByName(String name);
}
