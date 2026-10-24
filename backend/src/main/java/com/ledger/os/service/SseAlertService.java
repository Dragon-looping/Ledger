package com.ledger.os.service;

import com.ledger.os.event.BudgetThresholdEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseAlertService {

    private static final Logger log = LoggerFactory.getLogger(SseAlertService.class);
    private static final Long DEFAULT_TIMEOUT = 30 * 60 * 1000L; // 30 minutes

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            emitters.remove(emitter);
        });
        emitter.onError(e -> {
            log.warn("SSE connection error: {}", e.getMessage());
            emitters.remove(emitter);
        });

        // Send initial connection event
        try {
            emitter.send(SseEmitter.event()
                    .name("CONNECTED")
                    .data(Map.of(
                            "status", "CONNECTED",
                            "message", "Ledger OS Real-time Alert Stream Active",
                            "timestamp", Instant.now().toString()
                    )));
        } catch (IOException e) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    @EventListener
    public void handleBudgetThresholdEvent(BudgetThresholdEvent event) {
        log.info("Broadcasting BudgetThresholdEvent via SSE: {}", event);

        Map<String, Object> payload = Map.of(
                "level", event.getLevel().name(),
                "ruleName", event.getRule().getName(),
                "period", event.getPeriod().toString(),
                "limit", event.getStatus().limit().getAmount(),
                "spent", event.getStatus().spent().getAmount(),
                "percentUsed", event.getStatus().percentUsed(),
                "currency", event.getStatus().limit().getCurrencyCode(),
                "triggeringTransaction", event.getTriggeringTransaction().describe(),
                "timestamp", Instant.now().toString()
        );

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("BUDGET_ALERT")
                        .data(payload));
            } catch (Exception e) {
                emitters.remove(emitter);
            }
        }
    }

    public int getActiveEmitterCount() {
        return emitters.size();
    }
}
