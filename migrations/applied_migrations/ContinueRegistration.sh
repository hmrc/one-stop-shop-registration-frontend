#!/bin/bash

echo ""
echo "Applying migration ContinueRegistration"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /continueRegistration                        controllers.ContinueRegistrationController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /continueRegistration                        controllers.ContinueRegistrationController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeContinueRegistration                  controllers.ContinueRegistrationController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeContinueRegistration                  controllers.ContinueRegistrationController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "continueRegistration.title = continueRegistration" >> ../conf/messages.en
echo "continueRegistration.heading = continueRegistration" >> ../conf/messages.en
echo "continueRegistration.checkYourAnswersLabel = continueRegistration" >> ../conf/messages.en
echo "continueRegistration.error.required = Select yes if continueRegistration" >> ../conf/messages.en
echo "continueRegistration.change.hidden = ContinueRegistration" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryContinueRegistrationUserAnswersEntry: Arbitrary[(ContinueRegistrationPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[ContinueRegistrationPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryContinueRegistrationPage: Arbitrary[ContinueRegistrationPage.type] =";\
    print "    Arbitrary(ContinueRegistrationPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(ContinueRegistrationPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration ContinueRegistration completed"
