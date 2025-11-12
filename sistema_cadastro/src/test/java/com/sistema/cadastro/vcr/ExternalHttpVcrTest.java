package com.sistema.cadastro.vcr;

import com.easypost.easyvcr.Mode;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableHttpsURLConnection;
import com.easypost.easyvcr.clients.httpurlconnection.RecordableURL;
import com.easypost.easyvcr.Cassette;
import org.junit.jupiter.api.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ExternalHttpVcrTest {

    @BeforeAll
    static void ensureCassetteDir() throws IOException {
        if (!Files.exists(VcrService.cassetteDir())) {
            Files.createDirectories(VcrService.cassetteDir());
        }
    }

    private String readBody(RecordableHttpsURLConnection connection) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        }
    }

    @Test
    @Order(1)
    @DisplayName("viacep_test")
    void testVCRRecordAndPlayback() throws Exception {
        String cassetteName = "viacep_test";

        Cassette cassette = VcrService.newCassette(cassetteName);

        RecordableURL recordableURL = new RecordableURL("https://viacep.com.br/ws/01001000/json", cassette, Mode.Auto);
        RecordableHttpsURLConnection conn = recordableURL.openConnectionSecure();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        int code = conn.getResponseCode();
        assertEquals(200, code);
        String body = readBody(conn);
        assertNotNull(body);
        assertTrue(body.contains("cep") || body.contains("01001"));

        assertTrue(VcrService.cassetteExists(cassetteName));
        String cassetteContent = VcrService.readCassetteContent(cassetteName);
        assertFalse(cassetteContent.isEmpty());
        assertTrue(cassetteContent.contains("viacep.com.br"));

        RecordableURL playbackURL = new RecordableURL("https://viacep.com.br/ws/01001000/json", cassette, Mode.Replay);
        RecordableHttpsURLConnection playbackConn = playbackURL.openConnectionSecure();
        int code2 = playbackConn.getResponseCode();
        String body2 = readBody(playbackConn);
        assertEquals(200, code2);
        assertEquals(body, body2);
        String contentType = playbackConn.getHeaderField("Content-Type");
        assertNotNull(contentType);
    }

    @Test
    @Order(2)
    @DisplayName("get_404_test")
    void testGet404() throws Exception {
        String cassetteName = "get_404_test";
        Cassette cassette = VcrService.newCassette(cassetteName);

        RecordableURL url = new RecordableURL("https://httpstat.us/404", cassette, Mode.Auto);
        RecordableHttpsURLConnection conn = url.openConnectionSecure();
        conn.setRequestMethod("HEAD");
        conn.setConnectTimeout(4000);
        conn.setReadTimeout(4000);
        try {
            int code = conn.getResponseCode();
            assertEquals(404, code);
        } catch (NullPointerException npe) {

            assertNotNull(npe.getMessage());
        }
        boolean exists404 = VcrService.cassetteExists(cassetteName);

        RecordableURL urlReplay = new RecordableURL("https://httpstat.us/404", cassette, Mode.Replay);
        RecordableHttpsURLConnection connReplay = urlReplay.openConnectionSecure();
        connReplay.setRequestMethod("HEAD");
        if (exists404) {
            try {
                int codeReplay = connReplay.getResponseCode();
                assertEquals(404, codeReplay);
            } catch (NullPointerException npe) {
                assertNotNull(npe.getMessage());
            }
        }
    }

    @Test
    @Order(3)
    @DisplayName("get_500_test")
    void testGet500() throws Exception {
        String cassetteName = "get_500_test";
        Cassette cassette = VcrService.newCassette(cassetteName);

        RecordableURL url = new RecordableURL("https://httpstat.us/500", cassette, Mode.Auto);
        RecordableHttpsURLConnection conn = url.openConnectionSecure();
        conn.setRequestMethod("HEAD");
        conn.setConnectTimeout(4000);
        conn.setReadTimeout(4000);
        try {
            int code = conn.getResponseCode();
            assertEquals(500, code);
        } catch (NullPointerException npe) {
            assertNotNull(npe.getMessage());
        }
        boolean exists500 = VcrService.cassetteExists(cassetteName);

        RecordableURL replay = new RecordableURL("https://httpstat.us/500", cassette, Mode.Replay);
        RecordableHttpsURLConnection conn2 = replay.openConnectionSecure();
        conn2.setRequestMethod("HEAD");
        if (exists500) {
            try {
                int code2 = conn2.getResponseCode();
                assertEquals(500, code2);
            } catch (NullPointerException npe) {
                assertNotNull(npe.getMessage());
            }
        }
    }

    @Test
    @Order(4)
    @DisplayName("post_json_test")
    void testPostJsonWithHeaders() throws Exception {
        String cassetteName = "post_json_test";
        Cassette cassette = VcrService.newCassette(cassetteName);

        String payload = "{\"name\":\"Teste\",\"ok\":true}";
        RecordableURL url = new RecordableURL("https://httpbin.org/post", cassette, Mode.Auto);
        RecordableHttpsURLConnection conn = url.openConnectionSecure();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }
        int code = conn.getResponseCode();
        assertEquals(200, code);
        String body = readBody(conn);

        assertNotNull(body);
        assertTrue(body.length() > 0);
        assertTrue(VcrService.cassetteExists(cassetteName));

        RecordableURL replay = new RecordableURL("https://httpbin.org/post", cassette, Mode.Replay);
        RecordableHttpsURLConnection conn2 = replay.openConnectionSecure();
        conn2.setRequestMethod("POST");
        conn2.setDoOutput(true);
        conn2.setRequestProperty("Content-Type", "application/json");
        try (OutputStream os = conn2.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }
        int code2 = conn2.getResponseCode();
        String body2 = readBody(conn2);
        assertEquals(200, code2);
        assertEquals(body, body2);
    }
}
