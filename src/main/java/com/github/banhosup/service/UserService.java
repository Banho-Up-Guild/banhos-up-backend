package com.github.banhosup.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.banhosup.model.User;
import jdk.vm.ci.meta.Local;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class UserService {

    private AtomicInteger contador = new AtomicInteger(0);

    private Map<String, User> mapUser = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        mapUser.put("fulano", new User(contador.incrementAndGet(),
                "fulano", "Fulano da silva",
                "fulano@email.com", "(11) 97876-6655", LocalDate.parse("2000-10-20")));
    }
}
