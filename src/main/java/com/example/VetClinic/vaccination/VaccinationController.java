package com.example.VetClinic.vaccination;

import com.example.VetClinic.vaccination.dto.CreateProtocolRequest;
import com.example.VetClinic.vaccination.dto.CreateScheduleRequest;
import com.example.VetClinic.vaccination.dto.ProtocolResponse;
import com.example.VetClinic.vaccination.dto.RecordDoseRequest;
import com.example.VetClinic.vaccination.dto.ScheduleResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/vaccinations")
public class VaccinationController {

    private final VaccinationService vaccinationService;

    public VaccinationController(VaccinationService vaccinationService) {
        this.vaccinationService = vaccinationService;
    }

    @PostMapping("/protocols")
    @ResponseStatus(HttpStatus.CREATED)
    public ProtocolResponse createProtocol(@Valid @RequestBody CreateProtocolRequest request) {
        return ProtocolResponse.from(vaccinationService.createProtocol(
                request.vaccineName(), request.species(), request.initialDoses(),
                request.weeksBetweenDoses(), request.boosterIntervalMonths()));
    }

    @GetMapping("/protocols")
    public List<ProtocolResponse> listProtocols(@RequestParam(required = false) Species species) {
        return vaccinationService.getProtocols(species).stream().map(ProtocolResponse::from).toList();
    }

    @PostMapping("/schedules")
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleResponse createSchedule(@Valid @RequestBody CreateScheduleRequest request) {
        return ScheduleResponse.from(vaccinationService.createSchedule(
                request.animalId(), request.protocolId(), request.firstDueDate()));
    }

    /** Puts the animal on every protocol for its species. */
    @PostMapping("/schedules/animal/{animalId}/required")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ScheduleResponse> createRequiredSchedules(
            @PathVariable String animalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate firstDueDate) {
        return vaccinationService.createRequiredSchedules(animalId, firstDueDate).stream()
                .map(ScheduleResponse::from).toList();
    }

    @GetMapping("/schedules/{id}")
    public ScheduleResponse getSchedule(@PathVariable Long id) {
        return ScheduleResponse.from(vaccinationService.getSchedule(id));
    }

    /** Schedules for one animal, or (without animalId) everything due in a date range. */
    @GetMapping("/schedules")
    public List<ScheduleResponse> listSchedules(
            @RequestParam(required = false) String animalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueTo) {
        List<VaccinationSchedule> result;
        if (animalId != null) {
            result = vaccinationService.getSchedulesForAnimal(animalId);
        } else {
            LocalDate from = dueFrom != null ? dueFrom : LocalDate.now();
            LocalDate to = dueTo != null ? dueTo : from.plusDays(30);
            result = vaccinationService.getDueBetween(from, to);
        }
        return result.stream().map(ScheduleResponse::from).toList();
    }

    @PostMapping("/schedules/{id}/doses")
    public ScheduleResponse recordDose(@PathVariable Long id, @Valid @RequestBody RecordDoseRequest request) {
        return ScheduleResponse.from(vaccinationService.recordDose(id, request.doseDate()));
    }
}
