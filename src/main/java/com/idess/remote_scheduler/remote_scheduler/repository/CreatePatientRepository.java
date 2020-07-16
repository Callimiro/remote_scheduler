package com.idess.remote_scheduler.remote_scheduler.repository;

import com.idess.remote_scheduler.remote_scheduler.entity.Patient;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@Repository
public class CreatePatientRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void insertWithQuery(Patient patient) {
        entityManager.createNativeQuery("INSERT INTO patients (injurity, name, sevirity) VALUES (?,?,?)")
                .setParameter(1, patient.getInjurity())
                .setParameter(2, patient.getName())
                .setParameter(3,patient.getSevirity())
                .executeUpdate();
    }
}
