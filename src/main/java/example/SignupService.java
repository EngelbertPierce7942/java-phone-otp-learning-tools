package example;

import java.io.IOException;

/** Domain workflow: a course-platform learner gets a code, then a session. */
public final class SignupService {
    private final PhoneAuthClient client;

    public SignupService(PhoneAuthClient client) { this.client = client; }

    public String beginSignup(String phone) throws IOException, InterruptedException {
        if (phone == null || phone.isBlank()) throw new IllegalArgumentException("phone is required");
        return client.sendCode(phone, "signup", "en-US");
    }

    public String completeSignup(String phone, String code) throws IOException, InterruptedException {
        if (code == null || code.isBlank()) throw new IllegalArgumentException("code is required");
        return client.verifyCode(phone, code, false);
    }
}
