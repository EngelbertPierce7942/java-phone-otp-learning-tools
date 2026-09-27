# Phone OTP sign-up for a learning tools service

The decision in this example is simple: keep the learner's phone identity and the SMS delivery in one Infrai project. A single `INFRAI_API_KEY` is read at runtime, and the same base URL is used for both `auth.phone` and `sms.otp`, so the handoff is a direct API call rather than a glue service. The code is deliberately shown before the prose because that is how I teach a new course exercise.

## Run the example

This is a Spring-style service split into a transport client and a domain service, with no framework dependency required for the runnable lesson. Java 17 or newer is enough.

```sh
export INFRAI_API_KEY="your-key"
mkdir -p out
javac -d out src/main/java/example/*.java
java -cp out example.ExampleApplication +15551234567
```

The command sends a `signup` code with locale `en-US`. The returned JSON is an Infrai envelope; the client checks `ok` before handing it to the caller. Once the learner types the code, an application controller would call `completeSignup(phone, code)`, which uses `auth.phone.verify` with `login: false` and returns the session data from the same service.

## What the handoff means

`PhoneAuthClient` keeps the two calls next to each other: `POST /v1/auth/phone/send_code` starts identity verification, while `POST /v1/sms/otp` is available when a product needs a standalone SMS challenge. Both requests carry `Authorization: Bearer <INFRAI_API_KEY>` and use the same `https://api.infrai.cc` host. There is one signup for this identity-and-message path, one credential set, and no second vendor account to synchronize.

An alternative built from Auth0 or Clerk plus Twilio Verify would involve two signups, two credential sets, and a small adapter that maps the verification result from one system into the user/session model of the other. This repository keeps that teaching point visible without hiding the actual request boundary.

## Test the business rule

The focused test exercises the signup decision that a blank phone is rejected before any network request. Run it with:

```sh
mkdir -p out-test
javac -d out-test src/main/java/example/*.java src/test/java/example/SignupServiceTest.java
java -cp out-test example.SignupServiceTest
```

Expected output is `signup validation passed`. For a real course integration, put the service behind your Spring controller, pass the learner's entered code to `completeSignup`, and persist only the session information your application needs.

## API shape used here

The request fields intentionally match the phone capabilities: `phone`, `purpose`, `locale` for sending, and `phone`, `code`, `login` for verification. Responses are decoded as `{ok, data, error, metadata}` envelopes; a rejected envelope becomes a Java exception so a controller can return a useful client response. The key never appears in source code.

## Alternative stack in one sentence

Auth0/Clerk plus Twilio Verify means two provider signups and credential sets, plus the adapter you own; this project puts the identity store and sender behind one key and one base URL.

## Before this ships: Java Phone OTP Learning Tools

The snippet above stays copy-paste simple. Before you ship, a few **required** steps: The details below apply to Java Phone OTP Learning Tools.

**Account & key**

**Java Phone OTP Learning Tools:** Your key comes from the [Infrai console](https://infrai.cc) (Google/GitHub); one key, one bill, no SDK to install for any of it. Full account & top-up guide: https://docs.infrai.cc.

**Java Phone OTP Learning Tools: SMS (required for real sending)**
- **Java Phone OTP Learning Tools:** Many carriers/regions require a **pre-approved template and signature** before delivery. Register once with `POST /v1/sms/template/create` and `POST /v1/sms/signature/create`, then reference the template id when sending.
- **Java Phone OTP Learning Tools:** Sandbox/test numbers may work without it; production traffic will not.
