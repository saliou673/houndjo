package com.houndjo.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.houndjo.domain.enumerations.AttendanceStatus;
import com.houndjo.domain.models.attendance.Attendance;
import com.houndjo.domain.ports.out.persistenceport.SessionPersistencePort;
import com.houndjo.domain.ports.out.persistenceport.StudentPersistencePort;
import com.houndjo.infrastructure.adapter.in.rest.controller.mapper.AttendanceDtoMapper;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import org.junit.jupiter.api.Test;

class AttendanceDtoMapperTest {
    @Test
    void shouldBatchSharedSessionAndStudentLookupsInsteadOfQueryingPerRow() {
        StudentPersistencePort students = mock(StudentPersistencePort.class);
        SessionPersistencePort sessions = mock(SessionPersistencePort.class);
        List<Attendance> records = LongStream.rangeClosed(1, 30)
                .mapToObj(id -> Attendance.create(7L, id, 9L, AttendanceStatus.PRESENT, null))
                .toList();
        var mapper = new AttendanceDtoMapper(students, sessions);
        assertThat(mapper.toDTOs(records)).hasSize(30);
        verify(students)
                .findByIdsAndOrganizationId(
                        LongStream.rangeClosed(1, 30).boxed().collect(Collectors.toSet()), 7L);
        verify(sessions).findByIdsAndOrganizationId(Set.of(9L), 7L);
        verifyNoMoreInteractions(students, sessions);
    }

    @Test
    void shouldNotQueryEmptyLists() {
        StudentPersistencePort students = mock(StudentPersistencePort.class);
        SessionPersistencePort sessions = mock(SessionPersistencePort.class);
        assertThat(new AttendanceDtoMapper(students, sessions).toDTOs(List.of()))
                .isEmpty();
        verifyNoInteractions(students, sessions);
    }
}
