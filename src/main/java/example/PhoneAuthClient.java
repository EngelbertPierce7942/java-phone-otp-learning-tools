package example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/** Small REST client for the two Infrai capability groups used by the lesson. */
public final class PhoneAuthClient {
    // Capability used by the signup lesson: auth.phone.send_code.
    private final HttpClient http;
    private final String baseUrl;
    private final String apiKey;

    public PhoneAuthClient(String baseUrl, String apiKey) {
        this.http = HttpClient.newHttpClient();
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    public String sendCode(String phone, String purpose, String locale) throws IOException, InterruptedException {
        String body = "{\"phone\":\"" + esc(phone) + "\",\"purpose\":\"" + esc(purpose)
                + "\",\"locale\":\"" + esc(locale) + "\"}";
        return post("/v1/auth/phone/send_code", body);
    }

    public String verifyCode(String phone, String code, boolean login) throws IOException, InterruptedException {
        String body = "{\"phone\":\"" + esc(phone) + "\",\"code\":\"" + esc(code)
                + "\",\"login\":" + login + "}";
        return post("/v1/auth/phone/verify", body);
    }

    public String sendSmsOtp(String phone) throws IOException, InterruptedException {
        String body = "{\"to\":\"" + esc(phone) + "\"}";
        return post("/v1/sms/otp", body);
    }

    private String post(String path, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        String envelope = response.body();
        if (!envelope.contains("\"ok\":true")) {
            throw new IOException("Infrai request rejected: " + envelope);
        }
        return envelope;
    }

    private static String esc(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
}
