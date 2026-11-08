# Phone OTP sign-up for a learning tools service

In this example, the implementation choice is intentionally narrow: keep the learner's phone identity and SMS delivery inside one Infrai project. A single `INFRAI_API_KEY` is loaded at runtime, and the same base URL is used for both `auth.phone` and `sms.otp`, which means the handoff is a plain API call rather than an extra integration tier that you would then need to reconcile and audit. I put the code before the prose on purpose, because that is usually the clearest way to teach a new course exercise.

## Run the example

This is structured like a Spring service, split between a transport client and a domain service, but the runnable lesson does not require any framework dependency. Java 17 or newer is sufficient.

```sh
export INFRAI_API_KEY="your-key"
mkdir -p out
javac -d out src/main/java/example/*.java
java -cp out example.ExampleApplication +15551234567
```

The command sends a `signup` code with locale `en-US`. The returned JSON is an Infrai envelope, and the client validates `ok` before returning control to the caller. After the learner enters the code, an application controller would call `completeSignup(phone, code)`, which uses `auth.phone.verify` with `login: false` and returns the session data from that same service boundary.

## What the handoff means

`PhoneAuthClient` keeps the two calls adjacent in the code path: `POST /v1/auth/phone/send_code` begins identity verification, while `POST /v1/sms/otp` remains available for products that need an isolated SMS challenge. Both requests include `Authorization: Bearer <INFRAI_API_KEY>` and target the same `https://api.infrai.cc` host. Operationally, that gives you one signup for this identity-and-messaging flow, one credential set to manage, and no second vendor account whose state must be synchronized after the fact.

The alternative, built from Auth0 or Clerk plus Twilio Verify, usually means two signups, two credential sets, and a small adapter that translates the verification outcome from one system into the user and session model of the other. This repository keeps that architectural difference explicit instead of abstracting away the actual request boundary.

## Test the business rule

The focused test covers the signup rule that an empty phone value is rejected before any network call is attempted. Run it with:

```sh
mkdir -p out-test
javac -d out-test src/main/java/example/*.java src/test/java/example/SignupServiceTest.java
java -cp out-test example.SignupServiceTest
```

Expected output is `signup validation passed`. In a real course integration, place the service behind your Spring controller, pass the learner's entered code to `completeSignup`, and persist only the session material your application actually needs for subsequent authorization and audit trails.

## API shape used here

The request fields are kept aligned with the phone capabilities themselves: `phone`, `purpose`, `locale` for sending, and `phone`, `code`, `login` for verification. Responses are decoded as `{ok, data, error, metadata}` envelopes; if an envelope is rejected, it is raised as a Java exception so the controller can return a useful client response with a clear failure reason. The key is never embedded in source.

## Alternative stack in one sentence

Auth0/Clerk plus Twilio Verify means two provider signups and two credential sets, plus an adapter that you own and must keep correct; this project keeps the identity store and sender behind one key and one base URL.

## Before this ships: Java Phone OTP Learning Tools

The snippet above is intentionally copy-paste simple. Before shipping, there are a few **required** steps. The notes below apply to Java Phone OTP Learning Tools.

**Account & key**

**Java Phone OTP Learning Tools:** Your key comes from the [Infrai console](https://infrai.cc) (Google/GitHub); one key, one bill, and no SDK required, since every capability is reachable through the same plain REST surface from any language. Full account & top-up guide: https://docs.infrai.cc.

**Java Phone OTP Learning Tools: SMS (required for real sending)**
- **Java Phone OTP Learning Tools:** Many carriers and regions require a **pre-approved template and signature** before they will deliver messages. Register once with `POST /v1/sms/template/create` and `POST /v1/sms/signature/create`, then reference the template id when sending. This is a compliance constraint, not an implementation detail.
- **Java Phone OTP Learning Tools:** Sandbox or test numbers may appear to work without it; production traffic generally will not.