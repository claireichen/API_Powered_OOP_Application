package org.example.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class APIClientSingletonTest {

    @Test
    void getInstanceReturnsSameObject() {
        APIClient c1 = APIClient.getInstance();
        APIClient c2 = APIClient.getInstance();

        assertNotNull(c1);
        assertSame(c1, c2, "APIClient should be a singleton");
    }
}
