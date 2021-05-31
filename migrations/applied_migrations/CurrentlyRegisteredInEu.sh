#!/bin/bash

echo ""
echo "Applying migration CurrentlyRegisteredInEu"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /currentlyRegisteredInEu                        controllers.CurrentlyRegisteredInEuController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /currentlyRegisteredInEu                        controllers.CurrentlyRegisteredInEuController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeCurrentlyRegisteredInEu                  controllers.CurrentlyRegisteredInEuController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeCurrentlyRegisteredInEu                  controllers.CurrentlyRegisteredInEuController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "currentlyRegisteredInEu.title = currentlyRegisteredInEu" >> ../conf/messages.en
echo "currentlyRegisteredInEu.heading = currentlyRegisteredInEu" >> ../conf/messages.en
echo "currentlyRegisteredInEu.checkYourAnswersLabel = currentlyRegisteredInEu" >> ../conf/messages.en
echo "currentlyRegisteredInEu.error.required = Select yes if currentlyRegisteredInEu" >> ../conf/messages.en
echo "currentlyRegisteredInEu.change.hidden = CurrentlyRegisteredInEu" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryCurrentlyRegisteredInEuUserAnswersEntry: Arbitrary[(CurrentlyRegisteredInEuPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[CurrentlyRegisteredInEuPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryCurrentlyRegisteredInEuPage: Arbitrary[CurrentlyRegisteredInEuPage.type] =";\
    print "    Arbitrary(CurrentlyRegisteredInEuPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(CurrentlyRegisteredInEuPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration CurrentlyRegisteredInEu completed"
