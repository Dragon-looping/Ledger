package com.ledger.os.controller;

import com.ledger.os.service.SseAlertService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final SseAlertService sseAlertService;

    public EventController(SseAlertService sseAlertService) {
        this.sseAlertService = sseAlertService;
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents() {
        return sseAlertService.createEmitter();
    }
}
