package com.otilm.common.credential.provider;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.Security;

public class KeyStoreTest {

    @Test
    public void testLoadKeyStore() throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        InputStream is = KeyStoreTest.class.getClassLoader().getResourceAsStream("trustStore.jks");
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(is, "123456".toCharArray());

        keyStore.aliases().asIterator().forEachRemaining(System.out::println);
    }
}
