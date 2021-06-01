#!/bin/bash

echo ""
echo "Applying migration AddPreviousRegistration"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /addPreviousRegistration                        controllers.AddPreviousRegistrationController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /addPreviousRegistration                        controllers.AddPreviousRegistrationController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeAddPreviousRegistration                  controllers.AddPreviousRegistrationController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeAddPreviousRegistration                  controllers.AddPreviousRegistrationController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "addPreviousRegistration.title = addPreviousRegistration" >> ../conf/messages.en
echo "addPreviousRegistration.heading = addPreviousRegistration" >> ../conf/messages.en
echo "addPreviousRegistration.checkYourAnswersLabel = addPreviousRegistration" >> ../conf/messages.en
echo "addPreviousRegistration.error.required = Select yes if addPreviousRegistration" >> ../conf/messages.en
echo "addPreviousRegistration.change.hidden = AddPreviousRegistration" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAddPreviousRegistrationUserAnswersEntry: Arbitrary[(AddPreviousRegistrationPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[AddPreviousRegistrationPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAddPreviousRegistrationPage: Arbitrary[AddPreviousRegistrationPage.type] =";\
    print "    Arbitrary(AddPreviousRegistrationPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(AddPreviousRegistrationPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration AddPreviousRegistration completed"
