
# one-stop-shop-registration-frontend

This is the repository for One Stop Shop Registration Frontend

Backend: https://github.com/hmrc/one-stop-shop-registration

Stub: https://github.com/hmrc/one-stop-shop-registration-stub

Requirements
------------

This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), so needs at least a [JRE] to run.

## Run the application

To update from Nexus and start all services from the RELEASE version instead of snapshot
```
sm --start ONE_STOP_SHOP_ALL -r
```

### To run the application locally execute the following:

The service needs to run in testOnly mode in order to access the testOnly get-passcodes endpoint which will generate a passcode for email verification.
```
sm --stop ONE_STOP_SHOP_REGISTRATION_FRONTEND
```
and 
```
sbt run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes
```

### Running correct version of mongo
We have introduced a transaction to the call to be able to ensure that both the vatreturn and correction get submitted to mongo.
Your local mongo is unlikely to be running a latest enough version and probably not in a replica set.
To do this, you'll need to stop your current mongo instance (docker ps to get the name of your mongo docker then docker stop <name> to stop)
Run at least 4.0 with a replica set:
```  
docker run --restart unless-stopped -d -p 27017-27019:27017-27019 --name mongo4 mongo:4.0 --replSet rs0
```
Connect to said replica set:
```
docker exec -it mongo4 mongo
```
When that console is there:
```
rs.initiate()
```
You then should be running 4.0 with a replica set. You may have to re-run the rs.initiate() after you've restarted


### Using the application
To log in using the Authority Wizard provide "continue url", "affinity group" and "enrolments" as follows:
  
![image](https://user-images.githubusercontent.com/48218839/145985763-ffb28570-7679-46a9-96fa-e93996f03c23.png)

![image](https://user-images.githubusercontent.com/48218839/145842926-c318cb10-70c3-4186-a839-b1928c8e2625.png)
  
The VRN can be any 9-digit number.

To successfully register go through the journey providing the answers as follows:
  1.
  ![image](https://user-images.githubusercontent.com/48218839/145986022-f387e3d0-0a41-47d7-9d39-f3b290b8e3ea.png)
  
  2.
  ![image](https://user-images.githubusercontent.com/48218839/145986122-d6f513ba-be1a-4a8c-9e9a-671580719bcd.png)
  
  3.
  ![image](https://user-images.githubusercontent.com/48218839/145986164-4cd4a00a-ec91-4167-be36-e35b9232e672.png)

  4.
  ![image](https://user-images.githubusercontent.com/48218839/145986669-58fecb10-16b3-4822-9c8c-6efd3bff325a.png)

  5.
  ![image](https://user-images.githubusercontent.com/48218839/145986756-de78aad1-a215-42a0-a71b-aff8ee771103.png)

  6.
  ![image](https://user-images.githubusercontent.com/48218839/145986873-fbae18cd-ce3d-46fa-aa10-c9d51aa0cabc.png)

  7.
  After clicking continue on the Contact details page, you will see the email verification page.
  ![image](https://user-images.githubusercontent.com/36073378/203574815-a6fdba3a-59aa-41a7-827f-58b5382af95c.png)

Open a new tab and paste this url:
  8. 
  ```
  /pay-vat-on-goods-sold-to-eu/northern-ireland-register/test-only/get-passcodes
  ```

  This will generate a passcode to enter into the email verification page.
  ![image](https://user-images.githubusercontent.com/36073378/203574977-a8298624-bc88-4090-8e8f-4b9d2be0abf4.png)

Once you have pasted/entered the passcode into the input box on the email verification page and clicked continue and the email verification is successful,
you will need to change the port in the url back to 10200 in order to redirect to the bank details page.
  9.
  ![image](https://user-images.githubusercontent.com/36073378/203573868-4809d4c5-8728-4b2f-bcce-3d8ad8f0e2c3.png)

  10.
  ![image](https://user-images.githubusercontent.com/36073378/203574605-b3a54885-bf3f-45e0-b58c-9c2d7b0cfa4d.png)

Unit and Integration Tests
------------

To run the unit and integration tests, you will need to open an sbt session on the browser.

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
