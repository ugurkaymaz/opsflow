package com.opsflow.backend;

import com.opsflow.backend.entity.OperationRecord;
import com.opsflow.backend.repository.OperationRecordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class OperationRecordRepositoryTest {

    @Autowired
    private OperationRecordRepository repository;

    @Test
    void shouldSaveOperation() {

        OperationRecord record = new OperationRecord();
        record.setTitle("Repository Test");
        record.setDescription("Testing MySQL repository");
        record.setOperationDate(
                LocalDate.of(2026, 9, 25)
        );
        record.setOperationTime(
                LocalTime.of(12, 30)
        );
        record.setStatus("PLANNED");

        OperationRecord saved =
                repository.saveAndFlush(record);

        assertNotNull(saved.getId());
    }

    @Test
    void shouldFindOperationsByDateRange() {

        OperationRecord record1 = new OperationRecord();
        record1.setTitle("Inside Range");
        record1.setDescription("Should be found");
        record1.setOperationDate(LocalDate.of(2026, 9, 20));
        record1.setOperationTime(LocalTime.of(10, 0));
        record1.setStatus("PLANNED");

        OperationRecord record2 = new OperationRecord();
        record2.setTitle("Outside Range");
        record2.setDescription("Should not be found");
        record2.setOperationDate(LocalDate.of(2026, 10, 10));
        record2.setOperationTime(LocalTime.of(10, 0));
        record2.setStatus("PLANNED");

        repository.save(record1);
        repository.save(record2);
        repository.flush();

        Page<OperationRecord> result =
                repository.findByOperationDateBetween(
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 9, 30),
                        PageRequest.of(0, 10)
                );

        assertEquals(1, result.getTotalElements());
        assertEquals(
                "Inside Range",
                result.getContent().get(0).getTitle()
        );
    }

    @Test
    void shouldFindOperationsByTimeRange() {

        OperationRecord record1 = new OperationRecord();
        record1.setTitle("Inside Time Range");
        record1.setDescription("Should be found");
        record1.setOperationDate(LocalDate.of(2026, 9, 25));
        record1.setOperationTime(LocalTime.of(14, 30));
        record1.setStatus("PLANNED");

        OperationRecord record2 = new OperationRecord();
        record2.setTitle("Outside Time Range");
        record2.setDescription("Should not be found");
        record2.setOperationDate(LocalDate.of(2026, 9, 25));
        record2.setOperationTime(LocalTime.of(20, 30));
        record2.setStatus("PLANNED");

        repository.save(record1);
        repository.save(record2);
        repository.flush();

        Page<OperationRecord> result =
                repository.findByOperationTimeBetween(
                        LocalTime.of(12, 0),
                        LocalTime.of(18, 0),
                        PageRequest.of(0, 10)
                );

        assertEquals(1, result.getTotalElements());
        assertEquals(
                "Inside Time Range",
                result.getContent().get(0).getTitle()
        );
    }
    @Test
    void shouldFindOperationsByDateAndTimeRange() {

        OperationRecord record1 = new OperationRecord();
        record1.setTitle("Matching Operation");
        record1.setDescription("Inside both ranges");
        record1.setOperationDate(LocalDate.of(2026, 9, 20));
        record1.setOperationTime(LocalTime.of(14, 30));
        record1.setStatus("PLANNED");

        OperationRecord record2 = new OperationRecord();
        record2.setTitle("Wrong Time");
        record2.setDescription("Date matches but time does not");
        record2.setOperationDate(LocalDate.of(2026, 9, 20));
        record2.setOperationTime(LocalTime.of(20, 30));
        record2.setStatus("PLANNED");

        OperationRecord record3 = new OperationRecord();
        record3.setTitle("Wrong Date");
        record3.setDescription("Time matches but date does not");
        record3.setOperationDate(LocalDate.of(2026, 10, 10));
        record3.setOperationTime(LocalTime.of(14, 30));
        record3.setStatus("PLANNED");

        repository.save(record1);
        repository.save(record2);
        repository.save(record3);
        repository.flush();

        Page<OperationRecord> result =
                repository.findByOperationDateBetweenAndOperationTimeBetween(
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 9, 30),
                        LocalTime.of(12, 0),
                        LocalTime.of(18, 0),
                        PageRequest.of(0, 10)
                );

        assertEquals(1, result.getTotalElements());
        assertEquals(
                "Matching Operation",
                result.getContent().get(0).getTitle()
        );
    }

    @Test
    void shouldCountOperationsByDateRange() {

        OperationRecord record1 = new OperationRecord();
        record1.setTitle("September Operation 1");
        record1.setDescription("Inside date range");
        record1.setOperationDate(
                LocalDate.of(2026, 9, 20)
        );
        record1.setOperationTime(
                LocalTime.of(10, 0)
        );
        record1.setStatus("PLANNED");

        OperationRecord record2 = new OperationRecord();
        record2.setTitle("September Operation 2");
        record2.setDescription("Inside date range");
        record2.setOperationDate(
                LocalDate.of(2026, 9, 25)
        );
        record2.setOperationTime(
                LocalTime.of(14, 0)
        );
        record2.setStatus("COMPLETED");

        OperationRecord record3 = new OperationRecord();
        record3.setTitle("October Operation");
        record3.setDescription("Outside date range");
        record3.setOperationDate(
                LocalDate.of(2026, 10, 10)
        );
        record3.setOperationTime(
                LocalTime.of(12, 0)
        );
        record3.setStatus("PLANNED");

        repository.save(record1);
        repository.save(record2);
        repository.save(record3);
        repository.flush();

        long count =
                repository.countByOperationDateBetween(
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 9, 30)
                );

        assertEquals(2L, count);
    }

    @Test
    void shouldCountOperationsByStatusAndDateRange() {

        OperationRecord record1 = new OperationRecord();
        record1.setTitle("Planned September 1");
        record1.setDescription("Should be counted");
        record1.setOperationDate(
                LocalDate.of(2026, 9, 20)
        );
        record1.setOperationTime(
                LocalTime.of(10, 0)
        );
        record1.setStatus("PLANNED");

        OperationRecord record2 = new OperationRecord();
        record2.setTitle("Planned September 2");
        record2.setDescription("Should be counted");
        record2.setOperationDate(
                LocalDate.of(2026, 9, 25)
        );
        record2.setOperationTime(
                LocalTime.of(14, 0)
        );
        record2.setStatus("PLANNED");

        OperationRecord record3 = new OperationRecord();
        record3.setTitle("Completed September");
        record3.setDescription("Wrong status");
        record3.setOperationDate(
                LocalDate.of(2026, 9, 22)
        );
        record3.setOperationTime(
                LocalTime.of(16, 0)
        );
        record3.setStatus("COMPLETED");

        OperationRecord record4 = new OperationRecord();
        record4.setTitle("Planned October");
        record4.setDescription("Outside date range");
        record4.setOperationDate(
                LocalDate.of(2026, 10, 10)
        );
        record4.setOperationTime(
                LocalTime.of(11, 0)
        );
        record4.setStatus("PLANNED");

        repository.save(record1);
        repository.save(record2);
        repository.save(record3);
        repository.save(record4);
        repository.flush();

        long count =
                repository.countByStatusAndOperationDateBetween(
                        "PLANNED",
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 9, 30)
                );

        assertEquals(2L, count);
    }

}