
# one-stop-shop-registration-frontend

This is the repository for One Stop Shop Registration Frontend

Backend: https://github.com/hmrc/one-stop-shop-registration

Stub: https://github.com/hmrc/one-stop-shop-registration-stub

One Stop Shop Registration Service
------------

The main function of this service is to allow traders to register to pay VAT on distance sales of goods from 
Northern Ireland to the EU. This will then provide them with an OSS enrolment that will allow access to other 
OSS services - Returns (one-stop-shop-returns-frontend) and Exclusions (one-stop-shop-exclusions-frontend).

Once a trader has been registered, there are a few other functions that are available in the service:

Amend - this allows the trader to amend details they used on their original registration to keep
their information up to date.

Rejoin - Once a trader has left the One Stop Shop service, if they would like to rejoin, they can access this
option and all of their previous registration data will be offered to reuse and edit on the rejoin.

Summary of APIs
------------

This service utilises various APIs from other platforms in order to obtain and store information required for the 
registration process.

ETMP:
- HMRC VAT registration details are retrieved
- Submitted registration details are passed to ETMP for storing and later querying against

Core:
- EU VAT registrations are verified with Core to check for any exclusions

Note: locally (and on staging) these connections are stubbed via one-stop-shop-registration-stub.

Requirements
------------

This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), so needs at least a [JRE] to run.

## Run the application locally via Service Manager

```
sm2 --start ONE_STOP_SHOP_ALL -r
```

### To run the application locally from the repository, execute the following:

The service needs to run in testOnly mode in order to access the testOnly get-passcodes endpoint for BTA and email
verification.

```
sm2 --stop ONE_STOP_SHOP_REGISTRATION_FRONTEND
```
and 
```
sbt run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes
```

### Running correct version of mongo
Mongo 6 with a replica set is required to run the service. Please refer to the MDTP Handbook for instructions on how to run this


Using the application
------------

Access the Authority Wizard to log in:
http://localhost:9949/auth-login-stub/gg-sign-in

Enter the following details on this page and submit:
- Redirect URL: http://localhost:10200/pay-vat-on-goods-sold-to-eu/northern-ireland-register
- Affinity Group: Organisation
- Enrolments:
- Enrolment Key: HMRC-MTD-VAT
- Identifier Name: VRN
- Identifier Value: 100000001

### Registration journey

It is recommended to use VRN 100000001 for a straightforward registration journey, hence why this one is used as
the "Identifier Value" above. Other scenarios can be found in one-stop-shop-registration-stub.

To enter the registration journey, you will need to complete the initial filter questions as follows:
1. Is your business already registered for the One Stop Shop Union scheme in an EU country? 
- No
2. Will your business sell goods from Northern Ireland to consumers in the EU?
- Yes
3. Is your principal place of business in Northern Ireland?
- Yes

Continue through the journey, completing each question through to the final check-your-answers page and submit the 
registration. 

Email verification: 
Use the test-only endpoint (http://localhost:10190/pay-vat-on-goods-sold-to-eu/register-for-import-one-stop-shop/test-only/get-passcodes)
in a separate tab to generate a passcode that can be entered into the email verification page, following adding 
an email to the /business-contact-details page


Note: you can refer to the Registration.feature within one-stop-shop-registration-journey-tests if any examples of data 
to input are required.

### Amend registration journey

Access the Authority Wizard to log in:
http://localhost:9949/auth-login-stub/gg-sign-in

Enter the following details on this page and submit:
- Redirect URL: http://localhost:10200/pay-vat-on-goods-sold-to-eu/northern-ireland-register/start-amend-journey
- Affinity Group: Organisation
- Enrolments (there are two rows this time):
- Enrolment Key: HMRC-MTD-VAT
- Identifier Name: VRN
- Identifier Value: 100000002
- Enrolment Key: HMRC-OSS-ORG
- Identifier Name: VRN
- Identifier Value: 100000002

It is recommended to use VRN 100000002 for a regular amend journey, however alternatives can be found in the 
one-stop-shop-registration-stub.

### Rejoin registration journey

Access the Authority Wizard to log in:
http://localhost:9949/auth-login-stub/gg-sign-in

Enter the following details on this page and submit:
- Redirect URL: http://localhost:10200/pay-vat-on-goods-sold-to-eu/northern-ireland-register/start-rejoin-journey
- Affinity Group: Organisation
- Enrolments (there are two rows this time):
- Enrolment Key: HMRC-MTD-VAT
- Identifier Name: VRN
- Identifier Value: 600000050
- Enrolment Key: HMRC-OSS-ORG
- Identifier Name: VRN
- Identifier Value: 600000050

It is recommended to use VRN 600000050 for a regular rejoin journey, however alternatives can be found in the
one-stop-shop-registration-stub.


Unit and Integration Tests
------------

To run the unit and integration tests, you will need to open an sbt session on the terminal.

### Unit Tests

To run all tests, run the following command in your sbt session:
```
test
```

To run a single test, run the following command in your sbt session:
```
testOnly <package>.<SpecName>
```

An asterisk can be used as a wildcard character without having to enter the package, as per the example below:
```
testOnly *AddTradingNameControllerSpec
```

### Integration Tests

To run all tests, run the following command in your sbt session:
```
it:test
```

To run a single test, run the following command in your sbt session:
```
it:testOnly <package>.<SpecName>
```

An asterisk can be used as a wildcard character without having to enter the package, as per the example below:
```
it:testOnly *AuthenticatedSessionRepositorySpec
```

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
