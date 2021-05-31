#!/bin/bash

echo ""
echo "Applying migration PreviousEuCountry"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /previousEuCountry                        controllers.PreviousEuCountryController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /previousEuCountry                        controllers.PreviousEuCountryController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changePreviousEuCountry                  controllers.PreviousEuCountryController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changePreviousEuCountry                  controllers.PreviousEuCountryController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "previousEuCountry.title = previousEuCountry" >> ../conf/messages.en
echo "previousEuCountry.heading = previousEuCountry" >> ../conf/messages.en
echo "previousEuCountry.checkYourAnswersLabel = previousEuCountry" >> ../conf/messages.en
echo "previousEuCountry.error.required = Enter previousEuCountry" >> ../conf/messages.en
echo "previousEuCountry.error.length = PreviousEuCountry must be 100 characters or less" >> ../conf/messages.en
echo "previousEuCountry.change.hidden = PreviousEuCountry" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryPreviousEuCountryUserAnswersEntry: Arbitrary[(PreviousEuCountryPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[PreviousEuCountryPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryPreviousEuCountryPage: Arbitrary[PreviousEuCountryPage.type] =";\
    print "    Arbitrary(PreviousEuCountryPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(PreviousEuCountryPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration PreviousEuCountry completed"
