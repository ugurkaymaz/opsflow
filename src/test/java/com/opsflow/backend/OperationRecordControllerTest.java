package com.opsflow.backend;

import com.opsflow.backend.controller.OperationRecordController;
import com.opsflow.backend.service.OperationRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.opsflow.backend.entity.OperationRecord;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import java.time.LocalDate;
import java.time.LocalTime;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;



import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OperationRecordController.class)
class OperationRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OperationRecordService service;

    @Test
    void shouldGetAllOperations() throws Exception {

        OperationRecord record = new OperationRecord();
        record.setId(1L);
        record.setTitle("System Maintenance");
        record.setDescription("Database server maintenance");
        record.setOperationDate(LocalDate.of(2026, 9, 21));
        record.setOperationTime(LocalTime.of(10, 30));
        record.setStatus("PLANNED");

        when(service.getAll()).thenReturn(List.of(record));

        mockMvc.perform(get("/api/operations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("System Maintenance"))
                .andExpect(jsonPath("$[0].description")
                        .value("Database server maintenance"))
                .andExpect(jsonPath("$[0].operationDate")
                        .value("2026-09-21"))
                .andExpect(jsonPath("$[0].operationTime")
                        .value("10:30:00"))
                .andExpect(jsonPath("$[0].status").value("PLANNED"));
    }

    @Test
    void shouldGetOperationById() throws Exception {

        OperationRecord record = new OperationRecord();
        record.setId(1L);
        record.setTitle("System Maintenance");
        record.setDescription("Database server maintenance");
        record.setOperationDate(LocalDate.of(2026, 9, 21));
        record.setOperationTime(LocalTime.of(10, 30));
        record.setStatus("PLANNED");

        when(service.getById(1L))
                .thenReturn(java.util.Optional.of(record));

        mockMvc.perform(get("/api/operations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title")
                        .value("System Maintenance"))
                .andExpect(jsonPath("$.description")
                        .value("Database server maintenance"))
                .andExpect(jsonPath("$.operationDate")
                        .value("2026-09-21"))
                .andExpect(jsonPath("$.operationTime")
                        .value("10:30:00"))
                .andExpect(jsonPath("$.status")
                        .value("PLANNED"));
    }


    @Test
    void shouldReturn404WhenOperationNotFound() throws Exception {

        when(service.getById(9999L))
                .thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/operations/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenCreateRequestIsInvalid() throws Exception {

        String invalidJson = """
            {
              "title": "",
              "description": "Invalid operation",
              "operationDate": null,
              "operationTime": null,
              "status": ""
            }
            """;

        mockMvc.perform(post("/api/operations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title")
                        .value("Title is required"))
                .andExpect(jsonPath("$.operationDate")
                        .value("Operation date is required"))
                .andExpect(jsonPath("$.operationTime")
                        .value("Operation time is required"))
                .andExpect(jsonPath("$.status")
                        .value("Status is required"));
    }

    @Test
    void shouldCreateOperation() throws Exception {

        OperationRecord savedRecord = new OperationRecord();
        savedRecord.setId(5L);
        savedRecord.setTitle("Server Upgrade");
        savedRecord.setDescription("Upgrade application server");
        savedRecord.setOperationDate(LocalDate.of(2026, 9, 25));
        savedRecord.setOperationTime(LocalTime.of(15, 30));
        savedRecord.setStatus("PLANNED");

        when(service.create(org.mockito.ArgumentMatchers.any(OperationRecord.class)))
                .thenReturn(savedRecord);

        String validJson = """
            {
              "title": "Server Upgrade",
              "description": "Upgrade application server",
              "operationDate": "2026-09-25",
              "operationTime": "15:30:00",
              "status": "PLANNED"
            }
            """;

        mockMvc.perform(post("/api/operations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.title").value("Server Upgrade"))
                .andExpect(jsonPath("$.description")
                        .value("Upgrade application server"))
                .andExpect(jsonPath("$.operationDate")
                        .value("2026-09-25"))
                .andExpect(jsonPath("$.operationTime")
                        .value("15:30:00"))
                .andExpect(jsonPath("$.status").value("PLANNED"));
    }

    @Test
    void shouldUpdateOperation() throws Exception {

        OperationRecord updatedRecord = new OperationRecord();
        updatedRecord.setId(5L);
        updatedRecord.setTitle("Updated Server Upgrade");
        updatedRecord.setDescription("Server upgrade completed");
        updatedRecord.setOperationDate(LocalDate.of(2026, 9, 26));
        updatedRecord.setOperationTime(LocalTime.of(16, 0));
        updatedRecord.setStatus("COMPLETED");

        when(service.update(
                org.mockito.ArgumentMatchers.eq(5L),
                org.mockito.ArgumentMatchers.any(OperationRecord.class)))
                .thenReturn(java.util.Optional.of(updatedRecord));

        String updateJson = """
            {
              "title": "Updated Server Upgrade",
              "description": "Server upgrade completed",
              "operationDate": "2026-09-26",
              "operationTime": "16:00:00",
              "status": "COMPLETED"
            }
            """;

        mockMvc.perform(put("/api/operations/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.title")
                        .value("Updated Server Upgrade"))
                .andExpect(jsonPath("$.description")
                        .value("Server upgrade completed"))
                .andExpect(jsonPath("$.operationDate")
                        .value("2026-09-26"))
                .andExpect(jsonPath("$.operationTime")
                        .value("16:00:00"))
                .andExpect(jsonPath("$.status")
                        .value("COMPLETED"));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistingOperation() throws Exception {

        when(service.update(
                org.mockito.ArgumentMatchers.eq(9999L),
                org.mockito.ArgumentMatchers.any(OperationRecord.class)))
                .thenReturn(java.util.Optional.empty());

        String updateJson = """
            {
              "title": "Missing Operation",
              "description": "This operation does not exist",
              "operationDate": "2026-09-26",
              "operationTime": "16:00:00",
              "status": "PLANNED"
            }
            """;

        mockMvc.perform(put("/api/operations/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteOperation() throws Exception {

        OperationRecord record = new OperationRecord();
        record.setId(5L);
        record.setTitle("Server Upgrade");
        record.setDescription("Upgrade application server");
        record.setOperationDate(LocalDate.of(2026, 9, 25));
        record.setOperationTime(LocalTime.of(15, 30));
        record.setStatus("PLANNED");

        when(service.getById(5L))
                .thenReturn(java.util.Optional.of(record));

        mockMvc.perform(delete("/api/operations/5"))
                .andExpect(status().isNoContent());

        org.mockito.Mockito.verify(service).delete(5L);
    }

    @Test
    void shouldReturn404WhenDeletingNonExistingOperation() throws Exception {

        when(service.getById(9999L))
                .thenReturn(java.util.Optional.empty());

        mockMvc.perform(delete("/api/operations/9999"))
                .andExpect(status().isNotFound());

        org.mockito.Mockito.verify(
                service,
                org.mockito.Mockito.never()
        ).delete(9999L);
    }

    @Test
    void shouldGetOperationsPage() throws Exception {

        OperationRecord record = new OperationRecord();
        record.setId(1L);
        record.setTitle("System Maintenance");
        record.setDescription("Database server maintenance");
        record.setOperationDate(LocalDate.of(2026, 9, 21));
        record.setOperationTime(LocalTime.of(10, 30));
        record.setStatus("PLANNED");

        org.springframework.data.domain.Page<OperationRecord> page =
                new org.springframework.data.domain.PageImpl<>(
                        List.of(record)
                );

        when(service.getAll(
                org.mockito.ArgumentMatchers.any(
                        org.springframework.data.domain.Pageable.class
                )))
                .thenReturn(page);

        mockMvc.perform(get("/api/operations/page")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title")
                        .value("System Maintenance"))
                .andExpect(jsonPath("$.content[0].status")
                        .value("PLANNED"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
    @Test
    void shouldReturn400WhenPageSizeExceedsLimit() throws Exception {

        mockMvc.perform(get("/api/operations/page")
                        .param("page", "0")
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Page size must not exceed 100"));
    }

    @Test
    void shouldSearchOperationsWithDateRangeAndSorting() throws Exception {

        OperationRecord record = new OperationRecord();
        record.setId(1L);
        record.setTitle("System Maintenance");
        record.setDescription("Database server maintenance");
        record.setOperationDate(LocalDate.of(2026, 9, 21));
        record.setOperationTime(LocalTime.of(10, 30));
        record.setStatus("PLANNED");

        org.springframework.data.domain.Page<OperationRecord> resultPage =
                new org.springframework.data.domain.PageImpl<>(
                        List.of(record)
                );

        when(service.search(
                org.mockito.ArgumentMatchers.eq(
                        LocalDate.of(2026, 9, 20)),
                org.mockito.ArgumentMatchers.eq(
                        LocalDate.of(2026, 9, 24)),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.any(
                        org.springframework.data.domain.Pageable.class)
        )).thenReturn(resultPage);

        mockMvc.perform(get("/api/operations/search")
                        .param("startDate", "2026-09-20")
                        .param("endDate", "2026-09-24")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "title")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title")
                        .value("System Maintenance"))
                .andExpect(jsonPath("$.content[0].operationDate")
                        .value("2026-09-21"))
                .andExpect(jsonPath("$.content[0].status")
                        .value("PLANNED"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
    @Test
    void shouldReturn400WhenSearchSortFieldIsInvalid() throws Exception {

        mockMvc.perform(get("/api/operations/search")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "abc")
                        .param("direction", "desc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Invalid sort field: abc"));
    }

    @Test
    void shouldReturn400WhenSearchDirectionIsInvalid() throws Exception {

        mockMvc.perform(get("/api/operations/search")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "title")
                        .param("direction", "test"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Sort direction must be 'asc' or 'desc'"));
    }

    @Test
    void shouldReturn400WhenEndDateIsMissing() throws Exception {

        when(service.search(
                org.mockito.ArgumentMatchers.eq(
                        LocalDate.of(2026, 9, 20)),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.any(
                        org.springframework.data.domain.Pageable.class)
        )).thenThrow(new IllegalArgumentException(
                "startDate and endDate must be provided together"
        ));

        mockMvc.perform(get("/api/operations/search")
                        .param("startDate", "2026-09-20")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("startDate and endDate must be provided together"));
    }

    @Test
    void shouldGetOperationSummary() throws Exception {

        com.opsflow.backend.dto.OperationSummary summary =
                new com.opsflow.backend.dto.OperationSummary(
                        10L,
                        4L,
                        2L,
                        4L
                );

        when(service.getSummary(
                org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.isNull()
        )).thenReturn(summary);

        mockMvc.perform(
                        get("/api/operations/summary")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.total").value(10)
                )
                .andExpect(
                        jsonPath("$.planned").value(4)
                )
                .andExpect(
                        jsonPath("$.inProgress").value(2)
                )
                .andExpect(
                        jsonPath("$.completed").value(4)
                );

        org.mockito.Mockito.verify(service)
                .getSummary(null, null);
    }

    @Test
    void shouldGetOperationSummaryForDateRange() throws Exception {

        LocalDate startDate =
                LocalDate.of(2026, 9, 20);

        LocalDate endDate =
                LocalDate.of(2026, 9, 25);

        com.opsflow.backend.dto.OperationSummary summary =
                new com.opsflow.backend.dto.OperationSummary(
                        6L,
                        3L,
                        1L,
                        2L
                );

        when(service.getSummary(
                startDate,
                endDate
        )).thenReturn(summary);

        mockMvc.perform(
                        get("/api/operations/summary")
                                .param(
                                        "startDate",
                                        "2026-09-20"
                                )
                                .param(
                                        "endDate",
                                        "2026-09-25"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.total").value(6)
                )
                .andExpect(
                        jsonPath("$.planned").value(3)
                )
                .andExpect(
                        jsonPath("$.inProgress").value(1)
                )
                .andExpect(
                        jsonPath("$.completed").value(2)
                );

        org.mockito.Mockito.verify(service)
                .getSummary(
                        startDate,
                        endDate
                );
    }

    @Test
    void shouldReturn400WhenSummaryDateRangeIsIncomplete()
            throws Exception {

        LocalDate startDate =
                LocalDate.of(2026, 9, 20);

        when(service.getSummary(
                startDate,
                null
        )).thenThrow(
                new IllegalArgumentException(
                        "startDate and endDate must be provided together"
                )
        );

        mockMvc.perform(
                        get("/api/operations/summary")
                                .param(
                                        "startDate",
                                        "2026-09-20"
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.error")
                                .value(
                                        "startDate and endDate must be provided together"
                                )
                );

        org.mockito.Mockito.verify(service)
                .getSummary(
                        startDate,
                        null
                );
    }
}