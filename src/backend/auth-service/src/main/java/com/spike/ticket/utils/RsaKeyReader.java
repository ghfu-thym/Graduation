package com.spike.ticket.utils;


import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Component
public class RsaKeyReader {
    public PrivateKey getPrivateKey() throws Exception {
        // 1. Đọc file từ thư mục resources
        InputStream is = getClass().getClassLoader().getResourceAsStream("private_key.pem");
        if (is == null) {
            throw new RuntimeException("Không tìm thấy file private_key.pem");
        }
        String key = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        // 2. Xóa bỏ các dòng header/footer và khoảng trắng
        String privateKeyPEM = key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");

        // 3. Giải mã Base64 và tạo Object PrivateKey
        byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);

        return keyFactory.generatePrivate(keySpec);
    }
}
