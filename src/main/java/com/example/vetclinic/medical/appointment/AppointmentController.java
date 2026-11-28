package com.example.vetclinic.medical.appointment;

import com.example.vetclinic.medical.appointment.dto.AppointmentResponse;
import com.example.vetclinic.medical.appointment.dto.BookAppointmentRequest;
import com.example.vetclinic.medical.appointment.dto.RescheduleAppointmentRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse book(@Valid @RequestBody BookAppointmentRequest request) {
        return AppointmentResponse.from(appointmentService.book(
                request.animalId(), request.vetId(), request.locationId(),
                request.start(), request.durationMinutes(), request.type()));
    }

    @GetMapping("/{id}")
    public AppointmentResponse get(@PathVariable("id") String reference) {
        return AppointmentResponse.from(appointmentService.getByReference(reference));
    }

    /** Filter by vetId, by animalId, or by a from/to range (defaults to the next 7 days). */
    @GetMapping
    public List<AppointmentResponse> list(
            @RequestParam(required = false) Long vetId,
            @RequestParam(required = false) String animalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        List<Appointment> result;
        if (vetId != null) {
            result = appointmentService.findForVet(vetId);
        } else if (animalId != null) {
            result = appointmentService.findForAnimal(animalId);
        } else {
            LocalDateTime rangeFrom = from != null ? from : LocalDate.now().atStartOfDay();
            LocalDateTime rangeTo = to != null ? to : rangeFrom.plusDays(7);
            result = appointmentService.findBetween(rangeFrom, rangeTo);
        }
        return result.stream().map(AppointmentResponse::from).toList();
    }

    @PutMapping("/{id}/reschedule")
    public AppointmentResponse reschedule(@PathVariable("id") String reference,
                                          @Valid @RequestBody RescheduleAppointmentRequest request) {
        return AppointmentResponse.from(
                appointmentService.reschedule(reference, request.start(), request.durationMinutes()));
    }

    @PostMapping("/{id}/cancel")
    public AppointmentResponse cancel(@PathVariable("id") String reference) {
        return AppointmentResponse.from(appointmentService.cancel(reference));
    }

    @PostMapping("/{id}/complete")
    public AppointmentResponse complete(@PathVariable("id") String reference) {
        return AppointmentResponse.from(appointmentService.complete(reference));
    }

    @PostMapping("/{id}/no-show")
    public AppointmentResponse noShow(@PathVariable("id") String reference) {
        return AppointmentResponse.from(appointmentService.markNoShow(reference));
    }
}
