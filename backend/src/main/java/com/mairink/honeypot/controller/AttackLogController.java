package com.mairink.honeypot.controller;

import com.mairink.honeypot.model.AttackLog;
import com.mairink.honeypot.repository.AttackLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AttackLogController {

    @Autowired
    private AttackLogRepository repository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping("/health")
    public Map<String, String> healthCheck() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("message", "Backend funcionando corretamente");
        return status;
    }

    @GetMapping("/test-attack")
    public AttackLog createTestAttack() {
        AttackLog testAttack = new AttackLog();
        testAttack.setSourceIp("192.168.1.100");
        testAttack.setSourcePort(54321);
        testAttack.setTargetPort(2222);
        testAttack.setProtocol("TEST");
        testAttack.setPayload("Ataque de teste via API");
        
        AttackLog saved = repository.save(testAttack);
        messagingTemplate.convertAndSend("/topic/attacks", saved);
        return saved;
    }

    @PostMapping("/attacks")
    public AttackLog receiveAttack(@RequestBody AttackLog attackLog) {
        System.out.println("Recebido ataque de: " + attackLog.getSourceIp());
        AttackLog saved = repository.save(attackLog);
        messagingTemplate.convertAndSend("/topic/attacks", saved);
        return saved;
    }

    @GetMapping("/attacks")
    public List<AttackLog> getAllAttacks() {
        return repository.findAll();
    }
}
