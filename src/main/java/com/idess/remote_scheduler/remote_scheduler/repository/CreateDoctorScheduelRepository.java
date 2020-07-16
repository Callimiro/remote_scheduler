package com.idess.remote_scheduler.remote_scheduler.repository;

import com.idess.remote_scheduler.remote_scheduler.entity.Doctors_schedule;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@Repository
public class CreateDoctorScheduelRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void insertWithQuery(Doctors_schedule doctors_schedule) {
        entityManager.createNativeQuery("INSERT INTO doctor_schedule (id, patient_id, doctor_id,position_in_queue ) VALUES (?,?,?,?)")
                .setParameter(1, doctors_schedule.getId())
                .setParameter(2, doctors_schedule.getPatient_id())
                .setParameter(3, doctors_schedule.getDoctor_id())
                .setParameter(4, doctors_schedule.getPosition_in_queue())
                .executeUpdate();
    }

    @Transactional
    public void updateWithQuery(int doctor_id,int position_in_queue){
        entityManager.createNativeQuery("Update doctor_schedule SET position_in_queue = position_in_queue+1 WHERE doctor_id = ?1 and position_in_queue >= ?2")
                .setParameter(1, doctor_id)
                .setParameter(2,position_in_queue)
                .executeUpdate();
    }
}
