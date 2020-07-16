package com.idess.remote_scheduler.remote_scheduler.repository;

import com.idess.remote_scheduler.remote_scheduler.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor,Long> {

    Optional<Doctor> findDoctorById(int id);
    List<Doctor> findAllBySpeciality(int speciality);

}
