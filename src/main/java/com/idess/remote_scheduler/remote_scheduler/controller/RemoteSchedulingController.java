package com.idess.remote_scheduler.remote_scheduler.controller;

import com.idess.remote_scheduler.remote_scheduler.entity.Doctor;
import com.idess.remote_scheduler.remote_scheduler.entity.Doctors_schedule;
import com.idess.remote_scheduler.remote_scheduler.entity.Patient;
import com.idess.remote_scheduler.remote_scheduler.entity.PatientScheduelingForm;
import com.idess.remote_scheduler.remote_scheduler.repository.CreateDoctorScheduelRepository;
import com.idess.remote_scheduler.remote_scheduler.repository.DoctorRepository;
import com.idess.remote_scheduler.remote_scheduler.repository.DoctorScheduleRepository;
import com.idess.remote_scheduler.remote_scheduler.repository.PatientRepository;
import com.idess.remote_scheduler.remote_scheduler.service.RemoteSchedulingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RemoteSchedulingController {

    @Autowired
    DoctorRepository doctorRepository;
    @Autowired
    RemoteSchedulingService remoteSchedulingService;
    @Autowired
    PatientRepository patientRepository;
    @Autowired
    DoctorScheduleRepository doctorScheduleRepository;
    @Autowired
    CreateDoctorScheduelRepository createDoctorScheduelRepository;

    @PostMapping(value = "/remote_sched")
    ResponseEntity<String> getRemoteSched(@RequestBody PatientScheduelingForm patientScheduelingForm){
        List<Doctor> doctorList = doctorRepository.findAll();
        Doctor doctor = remoteSchedulingService.doctorsSortingResult(doctorList,patientScheduelingForm);
        Patient patient = patientRepository.findPatientByName(patientScheduelingForm.getPatientName());
        int position = remoteSchedulingService.orderWaitingList(doctor,patient);
        createDoctorScheduelRepository.updateWithQuery(doctor.getId(),position);
        Doctors_schedule doctorsSchedule = new Doctors_schedule();
        doctorsSchedule.setDoctor_id(doctor.getId());
        doctorsSchedule.setPatient_id(patient.getId());
        doctorsSchedule.setPosition_in_queue(position);
        createDoctorScheduelRepository.insertWithQuery(doctorsSchedule);
        return ResponseEntity.ok("doctor id is "+doctor.getId()+" and position is "+position);
    }

    @GetMapping(value = "/test")
    String getTest(){
        return "this is working";
    }
}
