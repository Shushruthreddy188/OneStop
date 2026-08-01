package com.onestop.admin.web;

import com.onestop.admin.service.AdminService;
import com.onestop.admin.web.dto.AdminDtos.DashboardDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AdminControllerTest {
    @Test
    void dashboardReturnsTheAggregatedServiceResult() {
        AdminService service = mock(AdminService.class);
        DashboardDto expected = new DashboardDto(100, 12, 3, 20, 18,
                new BigDecimal("999.00"), List.of());
        when(service.dashboard()).thenReturn(expected);

        assertThat(new AdminController(service).dashboard()).isSameAs(expected);
        verify(service).dashboard();
    }
}
