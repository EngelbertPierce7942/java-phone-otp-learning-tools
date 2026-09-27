package example;

public final class SignupServiceTest {
    public static void main(String[] args) throws Exception {
        PhoneAuthClient client = new PhoneAuthClient("http://localhost:1", "test");
        SignupService service = new SignupService(client);
        try { service.beginSignup(" "); throw new AssertionError("blank phone should be rejected"); }
        catch (IllegalArgumentException expected) { System.out.println("signup validation passed"); }
    }
}
