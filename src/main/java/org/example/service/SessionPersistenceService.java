package org.example.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.model.domain.Session;

import java.io.*;

public class SessionPersistenceService {

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public void save(Session session, File file) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(session, writer);
        }
    }

    public Session load(File file) throws IOException {
        try (Reader reader = new FileReader(file)) {
            return gson.fromJson(reader, Session.class);
        }
    }
}
