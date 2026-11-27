package com.example.VetClinic.vaccination;

import com.example.VetClinic.clientsandpatients.Animal;
import com.example.VetClinic.clientsandpatients.Client;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VaccinationScheduleTest {

    private final Animal rex = new Animal("PET-0001", "Rex", Species.DOG,
            new Client("OWN-0001", "Jane Doe", "jane@example.com", "0123"));

    @Test
    void primarySeriesUsesWeeksBetweenDosesThenSwitchesToBoosters() {
        VaccinationProtocol dhpp = new VaccinationProtocol("DHPP", Species.DOG, 3, 3, 12);
        VaccinationSchedule schedule = new VaccinationSchedule(rex, dhpp, LocalDate.of(2026, 1, 1));

        schedule.recordDose(LocalDate.of(2026, 1, 1));
        assertThat(schedule.getNextDue()).isEqualTo(LocalDate.of(2026, 1, 22));

        schedule.recordDose(LocalDate.of(2026, 1, 22));
        assertThat(schedule.getNextDue()).isEqualTo(LocalDate.of(2026, 2, 12));
        assertThat(schedule.isInPrimarySeries()).isTrue();

        schedule.recordDose(LocalDate.of(2026, 2, 12));
        assertThat(schedule.getDosesGiven()).isEqualTo(3);
        assertThat(schedule.isInPrimarySeries()).isFalse();
        assertThat(schedule.getNextDue()).isEqualTo(LocalDate.of(2027, 2, 12));
    }

    @Test
    void rabiesBoosterIsDueAgainTwelveMonthsAfterEachDose() {
        VaccinationProtocol rabies = new VaccinationProtocol("Rabies", Species.DOG, 1, 0, 12);
        VaccinationSchedule schedule = new VaccinationSchedule(rex, rabies, LocalDate.of(2026, 3, 10));
        assertThat(schedule.calculateNextDue()).isEqualTo(LocalDate.of(2026, 3, 10));

        schedule.recordDose(LocalDate.of(2026, 3, 10));
        assertThat(schedule.getNextDue()).isEqualTo(LocalDate.of(2027, 3, 10));

        // Booster given a few days late: the next one is counted from the actual dose date.
        schedule.recordDose(LocalDate.of(2027, 3, 14));
        assertThat(schedule.getNextDue()).isEqualTo(LocalDate.of(2028, 3, 14));
        assertThat(schedule.getDosesGiven()).isEqualTo(2);
    }

    @Test
    void doseDatedBeforeThePreviousDoseIsRejected() {
        VaccinationProtocol rabies = new VaccinationProtocol("Rabies", Species.DOG, 1, 0, 12);
        VaccinationSchedule schedule = new VaccinationSchedule(rex, rabies, LocalDate.of(2026, 3, 10));
        schedule.recordDose(LocalDate.of(2026, 3, 10));

        assertThrows(IllegalArgumentException.class, () -> schedule.recordDose(LocalDate.of(2026, 3, 1)));
        assertThat(schedule.getDosesGiven()).isEqualTo(1);
    }
}
