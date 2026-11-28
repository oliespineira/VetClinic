package com.example.vetclinic.medical.treatment;

import com.example.vetclinic.medical.treatment.dto.MedicalRecordResponse;
import com.example.vetclinic.medical.treatment.dto.TreatmentRequests;
import com.example.vetclinic.medical.treatment.dto.TreatmentResponse;
import com.example.vetclinic.security.EmployeeUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for medical records and treatments. Deliberately thin: it reads the
 * logged-in employee, validates the body, calls TreatmentService and turns the result
 * into a DTO. Every rule lives in the service.
 */
@RestController
@RequestMapping("/api")
public class TreatmentController {

    private final TreatmentService treatmentService;

    public TreatmentController(TreatmentService treatmentService) {
        this.treatmentService = treatmentService;
    }

    // ---------------------------------------------------------------- records

    @PostMapping("/medical-records")
    @ResponseStatus(HttpStatus.CREATED)
    public MedicalRecordResponse openRecord(@AuthenticationPrincipal EmployeeUserDetails me,
                                            @Valid @RequestBody TreatmentRequests.OpenRecord request) {
        return MedicalRecordResponse.from(treatmentService.openRecord(me.getEmployeeId(),
                request.animalId(), request.appointmentReference(), request.weightKg(),
                request.diagnosis(), request.notes()));
    }

    @GetMapping("/medical-records/{id}")
    public MedicalRecordResponse getRecord(@PathVariable Long id) {
        return MedicalRecordResponse.from(treatmentService.getRecord(id));
    }

    /** The animal's medical history, e.g. GET /api/medical-records?animalId=PET-0001 */
    @GetMapping("/medical-records")
    public List<MedicalRecordResponse> getHistory(@RequestParam String animalId) {
        return treatmentService.getHistory(animalId).stream().map(MedicalRecordResponse::from).toList();
    }

    // ------------------------------------------------------------- treatments

    @PostMapping("/medical-records/{id}/vaccines")
    @ResponseStatus(HttpStatus.CREATED)
    public TreatmentResponse giveVaccine(@AuthenticationPrincipal EmployeeUserDetails me, @PathVariable Long id,
                                         @Valid @RequestBody TreatmentRequests.GiveVaccine request) {
        return TreatmentResponse.from(treatmentService.recordVaccine(me.getEmployeeId(), id,
                request.vaccineName(), request.doseNumber(), request.vaccinationScheduleId(),
                request.productId(), request.cost()));
    }

    @PostMapping("/medical-records/{id}/medications")
    @ResponseStatus(HttpStatus.CREATED)
    public TreatmentResponse giveMedication(@AuthenticationPrincipal EmployeeUserDetails me, @PathVariable Long id,
                                            @Valid @RequestBody TreatmentRequests.GiveMedication request) {
        return TreatmentResponse.from(treatmentService.recordMedication(me.getEmployeeId(), id,
                request.medicineName(), request.dosePerKgMg(), request.durationDays(),
                request.productId(), request.cost()));
    }

    @PutMapping("/treatments/{id}/cost")
    public TreatmentResponse changeCost(@AuthenticationPrincipal EmployeeUserDetails me, @PathVariable Long id,
                                        @Valid @RequestBody TreatmentRequests.ChangeCost request) {
        return TreatmentResponse.from(treatmentService.changeCost(me.getEmployeeId(), id, request.newCost()));
    }

    // ---------------------------------------------------------------- surgery

    @PostMapping("/medical-records/{id}/surgeries")
    @ResponseStatus(HttpStatus.CREATED)
    public TreatmentResponse planSurgery(@AuthenticationPrincipal EmployeeUserDetails me, @PathVariable Long id,
                                         @Valid @RequestBody TreatmentRequests.PlanSurgery request) {
        return TreatmentResponse.from(treatmentService.planSurgery(me.getEmployeeId(), id,
                request.procedure(), request.estimatedCost()));
    }

    @PostMapping("/surgeries/{id}/consent")
    public TreatmentResponse signConsent(@PathVariable Long id,
                                         @Valid @RequestBody TreatmentRequests.SignConsent request) {
        return TreatmentResponse.from(treatmentService.signConsent(id, request.accepted()));
    }

    @PostMapping("/surgeries/{id}/complete")
    public TreatmentResponse completeSurgery(@AuthenticationPrincipal EmployeeUserDetails me, @PathVariable Long id,
                                             @Valid @RequestBody TreatmentRequests.CompleteSurgery request) {
        return TreatmentResponse.from(treatmentService.completeSurgery(me.getEmployeeId(), id,
                request.finalCost(), request.followUpStart(), request.followUpMinutes()));
    }

    @PostMapping("/surgeries/{id}/cancel")
    public TreatmentResponse cancelSurgery(@AuthenticationPrincipal EmployeeUserDetails me, @PathVariable Long id) {
        return TreatmentResponse.from(treatmentService.cancelSurgery(me.getEmployeeId(), id));
    }
}
