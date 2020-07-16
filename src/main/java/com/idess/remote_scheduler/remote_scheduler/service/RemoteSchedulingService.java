package com.idess.remote_scheduler.remote_scheduler.service;

import com.idess.remote_scheduler.remote_scheduler.entity.Doctor;
import com.idess.remote_scheduler.remote_scheduler.entity.Doctors_schedule;
import com.idess.remote_scheduler.remote_scheduler.entity.Patient;
import com.idess.remote_scheduler.remote_scheduler.entity.PatientScheduelingForm;
import com.idess.remote_scheduler.remote_scheduler.repository.CreatePatientRepository;
import com.idess.remote_scheduler.remote_scheduler.repository.DoctorRepository;
import com.idess.remote_scheduler.remote_scheduler.repository.DoctorScheduleRepository;
import com.idess.remote_scheduler.remote_scheduler.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RemoteSchedulingService {
    @Autowired
    DoctorRepository doctorRepository;
    @Autowired
    DoctorScheduleRepository doctorScheduleRepository;
    @Autowired
    PatientRepository patientRepository;
    @Autowired
    CreatePatientRepository createPatientRepository;
    @Autowired
    RestTemplate restTemplate;

    Logger logger = LoggerFactory.getLogger(RemoteSchedulingService.class);

    public Doctor getDoctorEvaluationResult(Doctor doctor1,long moyt1,int nbpd1, Doctor doctor2, long moyt2,int nbpd2,PatientScheduelingForm patientScheduelingForm){
        Map<String, Object> json = new HashMap<>();
        json.put("senioritiD1", doctor1.getSeniority());
        json.put("specD1", doctor1.getSpeciality());
        json.put("nbD1", nbpd1);
        json.put("moyt1", moyt1);
        json.put("senioritiD2", doctor2.getSeniority());
        json.put("specD2", doctor2.getSpeciality());
        json.put("nbD2", nbpd2);
        json.put("moyt2", moyt2);
        json.put("injP", patientScheduelingForm.getInjurityLevel());
        json.put("SevP", patientScheduelingForm.getSevirityIndex());

        String ResourceUrl="http://localhost:5001";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);


        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(json, headers);
        ResponseEntity<HashMap> response = restTemplate.postForEntity(ResourceUrl+"/predict", entity, HashMap.class);
        logger.info("------------------------"+response.getBody()+"-----------------");
        /*
         *Here we evaluate 2 doctors and return the best one by
         *  Calling the Local Emergency Machine learning Model
         */
        logger.info("the result is :"+response.getBody().get("mgp") );
        if(Float.parseFloat(String.valueOf(response.getBody().get("mgp"))) >= 0.7){
            return doctor1;
        }else{
            return doctor2;
        }

    }

    public Doctor doctorsSortingResult(List<Doctor> doctors, PatientScheduelingForm patientScheduelingForm){
        /*
         *Here we get the List Of Doctors and compare 2 of them each time
         * by calling the getDoctorEvaluationResult Method
         */
        LocalDateTime now = LocalDateTime.now();
        Timestamp time = Timestamp.valueOf(now);

        if (doctors.size()>= 2){
            Doctor doctor1 = doctors.get(0);
            Doctor doctor2 = null;
            List<Doctors_schedule> doctors_schedule1 = doctorScheduleRepository.getDoctors_scheduleByDoctor_id(doctor1.getId());
            long moyt1 = 0;
            long moyt2 = 0;
            int nbpd1,nbpd2 = 0;
            for (Doctors_schedule doctorsSchedule :
                    doctors_schedule1) {
                Patient patient = patientRepository.findById(doctorsSchedule.getPatient_id());
                long wt1 = time.getTime() - (patient.getCreated_at()).getTime();

                wt1 = wt1/(1000*60);
                moyt1 = moyt1+wt1;
            }
            moyt1 = moyt1/doctors_schedule1.size();
            nbpd1 = doctors_schedule1.size();
            logger.info(doctors_schedule1.toString());


            Patient patient2 = null;
            List<Doctors_schedule> doctors_schedule2 = null;
            for (Doctor doctor: doctors ) {
                //logger.warn(patient1.toString());
                //logger.warn(patient2.toString());
                if (doctors.indexOf(doctor)+1<doctors.size()){
                    doctor2 = doctors.get(doctors.indexOf(doctor)+1);
                    doctors_schedule2 = doctorScheduleRepository.getDoctors_scheduleByDoctor_id(doctor2.getId());
                    for (Doctors_schedule doctorsSchedule :
                            doctors_schedule1) {
                        patient2 = patientRepository.findById(doctorsSchedule.getPatient_id());
                        long wt2 = time.getTime() - (patient2.getCreated_at()).getTime();

                        wt2 = wt2/(1000*60);
                        moyt2 = moyt2+wt2;
                        nbpd2 = doctors_schedule1.size();
                    }}
                doctor1 = this.getDoctorEvaluationResult(doctor1,moyt1,nbpd1,doctor2,moyt2,nbpd2,patientScheduelingForm);
            }
            //Saving Patient to database
            Patient patient = new Patient();
            patient.setInjurity(patientScheduelingForm.getInjurityLevel());
            patient.setSevirity(patientScheduelingForm.getSevirityIndex());
            patient.setName(patientScheduelingForm.getPatientName());
            createPatientRepository.insertWithQuery(patient);
            return doctor1;
        }else {
            return doctors.get(0);
        }
    }

    public int orderWaitingList(Doctor doctor,Patient patient){
        List<Doctors_schedule> schedules = doctorScheduleRepository.getDoctors_scheduleByDoctor_id(doctor.getId());
        Patient patient1 = null;
        int position=0;
        for (Doctors_schedule doctorsSchedule :
                schedules) {
            patient1 = patientRepository.findById(doctorsSchedule.getPatient_id());
            patient1 = this.comparePatientsInWaitingQueue(patient,patient1);
            if (patient1.getId() == patient.getId()){
                position = doctorsSchedule.getPosition_in_queue();
                return position;
            }
        }
        return position;
    }

    public Patient comparePatientsInWaitingQueue(Patient patient1,Patient patient2){

        Map<String, Object> json = new HashMap<>();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String ResourceUrl="http://localhost:5002";

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(json, headers);
        ResponseEntity<HashMap> response = restTemplate.postForEntity(ResourceUrl+"/predict", entity, HashMap.class);
        return new Patient();
    }
    public List<Doctor> getDoctorsList(int injurity){
        List<Doctor> doctors = doctorRepository.findAllBySpeciality(injurity);
        return doctors;
    }
}
