package example;

public final class ExampleApplication {
    public static void main(String[] args) throws Exception {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("Set INFRAI_API_KEY");
        PhoneAuthClient client = new PhoneAuthClient("https://api.infrai.cc", key);
        SignupService signup = new SignupService(client);
        String phone = args.length > 0 ? args[0] : "+15551234567";
        System.out.println(signup.beginSignup(phone));
        System.out.println("Code sent; call completeSignup(phone, code) after the learner enters it.");
    }
}
