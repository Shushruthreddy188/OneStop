package com.onestop.activity.web;

import com.onestop.activity.service.ActivityService;
import com.onestop.activity.web.dto.ActivityDtos.EventInput;
import com.onestop.activity.web.dto.ActivityDtos.RecordEventsRequest;
import com.onestop.activity.web.dto.ActivityDtos.RecordResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    /** POST /api/activity/events — batch-ingest client activity (anonymous or authenticated). */
    @PostMapping("/events")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public RecordResult record(@AuthenticationPrincipal Long customerId,
                               @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId,
                               @Valid @RequestBody RecordEventsRequest request) {
        String correlation = (correlationId != null && !correlationId.isBlank())
                ? correlationId : UUID.randomUUID().toString();
        int recorded = 0;
        for (EventInput event : request.events()) {
            if (activityService.record(event, customerId, correlation)) {
                recorded++;
            }
        }
        return new RecordResult(request.events().size(), recorded);
    }
}
