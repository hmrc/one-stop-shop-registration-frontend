#!/bin/bash

echo ""
echo "Applying migration PreviouslyRegistered"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /previouslyRegistered                        controllers.PreviouslyRegisteredController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /previouslyRegistered                        controllers.PreviouslyRegisteredController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changePreviouslyRegistered                  controllers.PreviouslyRegisteredController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changePreviouslyRegistered                  controllers.PreviouslyRegisteredController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "previouslyRegistered.title = previouslyRegistered" >> ../conf/messages.en
echo "previouslyRegistered.heading = previouslyRegistered" >> ../conf/messages.en
echo "previouslyRegistered.checkYourAnswersLabel = previouslyRegistered" >> ../conf/messages.en
echo "previouslyRegistered.error.required = Select yes if previouslyRegistered" >> ../conf/messages.en
echo "previouslyRegistered.change.hidden = PreviouslyRegistered" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryPreviouslyRegisteredUserAnswersEntry: Arbitrary[(PreviouslyRegisteredPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[PreviouslyRegisteredPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryPreviouslyRegisteredPage: Arbitrary[PreviouslyRegisteredPage.type] =";\
    print "    Arbitrary(PreviouslyRegisteredPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(PreviouslyRegisteredPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration PreviouslyRegistered completed"
