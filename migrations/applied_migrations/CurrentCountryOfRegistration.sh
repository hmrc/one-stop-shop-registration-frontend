#!/bin/bash

echo ""
echo "Applying migration CurrentCountryOfRegistration"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /currentCountryOfRegistration                        controllers.CurrentCountryOfRegistrationController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /currentCountryOfRegistration                        controllers.CurrentCountryOfRegistrationController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeCurrentCountryOfRegistration                  controllers.CurrentCountryOfRegistrationController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeCurrentCountryOfRegistration                  controllers.CurrentCountryOfRegistrationController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "currentCountryOfRegistration.title = currentCountryOfRegistration" >> ../conf/messages.en
echo "currentCountryOfRegistration.heading = currentCountryOfRegistration" >> ../conf/messages.en
echo "currentCountryOfRegistration.option1 = Option 1" >> ../conf/messages.en
echo "currentCountryOfRegistration.option2 = Option 2" >> ../conf/messages.en
echo "currentCountryOfRegistration.checkYourAnswersLabel = currentCountryOfRegistration" >> ../conf/messages.en
echo "currentCountryOfRegistration.error.required = Select currentCountryOfRegistration" >> ../conf/messages.en
echo "currentCountryOfRegistration.change.hidden = CurrentCountryOfRegistration" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryCurrentCountryOfRegistrationUserAnswersEntry: Arbitrary[(CurrentCountryOfRegistrationPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[CurrentCountryOfRegistrationPage.type]";\
    print "        value <- arbitrary[CurrentCountryOfRegistration].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryCurrentCountryOfRegistrationPage: Arbitrary[CurrentCountryOfRegistrationPage.type] =";\
    print "    Arbitrary(CurrentCountryOfRegistrationPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryCurrentCountryOfRegistration: Arbitrary[CurrentCountryOfRegistration] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(CurrentCountryOfRegistration.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(CurrentCountryOfRegistrationPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration CurrentCountryOfRegistration completed"
