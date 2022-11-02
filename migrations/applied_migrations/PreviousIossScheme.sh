#!/bin/bash

echo ""
echo "Applying migration PreviousIossScheme"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /previousIossScheme                        controllers.PreviousIossSchemeController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /previousIossScheme                        controllers.PreviousIossSchemeController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changePreviousIossScheme                  controllers.PreviousIossSchemeController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changePreviousIossScheme                  controllers.PreviousIossSchemeController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "previousIossScheme.title = previousIossScheme" >> ../conf/messages.en
echo "previousIossScheme.heading = previousIossScheme" >> ../conf/messages.en
echo "previousIossScheme.option1 = Option 1" >> ../conf/messages.en
echo "previousIossScheme.option2 = Option 2" >> ../conf/messages.en
echo "previousIossScheme.checkYourAnswersLabel = previousIossScheme" >> ../conf/messages.en
echo "previousIossScheme.error.required = Select previousIossScheme" >> ../conf/messages.en
echo "previousIossScheme.change.hidden = PreviousIossScheme" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryPreviousIossSchemeUserAnswersEntry: Arbitrary[(PreviousIossSchemePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[PreviousIossSchemePage.type]";\
    print "        value <- arbitrary[PreviousIossScheme].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryPreviousIossSchemePage: Arbitrary[PreviousIossSchemePage.type] =";\
    print "    Arbitrary(PreviousIossSchemePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryPreviousIossScheme: Arbitrary[PreviousIossScheme] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(PreviousIossScheme.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(PreviousIossSchemePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration PreviousIossScheme completed"
