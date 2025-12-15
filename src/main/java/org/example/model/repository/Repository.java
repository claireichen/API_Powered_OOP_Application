package org.example.model.repository;

import java.io.IOException;

public interface Repository<T> {
    void save(T entity) throws IOException;
    T load() throws IOException;
}
