#!/bin/bash

echo ""
echo "Applying migration CurrentlyRegisteredInCountry"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /currentlyRegisteredInCountry                        controllers.CurrentlyRegisteredInCountryController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /currentlyRegisteredInCountry                        controllers.CurrentlyRegisteredInCountryController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeCurrentlyRegisteredInCountry                  controllers.CurrentlyRegisteredInCountryController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeCurrentlyRegisteredInCountry                  controllers.CurrentlyRegisteredInCountryController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "currentlyRegisteredInCountry.title = currentlyRegisteredInCountry" >> ../conf/messages.en
echo "currentlyRegisteredInCountry.heading = currentlyRegisteredInCountry" >> ../conf/messages.en
echo "currentlyRegisteredInCountry.checkYourAnswersLabel = currentlyRegisteredInCountry" >> ../conf/messages.en
echo "currentlyRegisteredInCountry.error.required = Select yes if currentlyRegisteredInCountry" >> ../conf/messages.en
echo "currentlyRegisteredInCountry.change.hidden = CurrentlyRegisteredInCountry" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryCurrentlyRegisteredInCountryUserAnswersEntry: Arbitrary[(CurrentlyRegisteredInCountryPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[CurrentlyRegisteredInCountryPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryCurrentlyRegisteredInCountryPage: Arbitrary[CurrentlyRegisteredInCountryPage.type] =";\
    print "    Arbitrary(CurrentlyRegisteredInCountryPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(CurrentlyRegisteredInCountryPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration CurrentlyRegisteredInCountry completed"
