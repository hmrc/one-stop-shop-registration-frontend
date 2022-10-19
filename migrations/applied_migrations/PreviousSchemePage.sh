#!/bin/bash

echo ""
echo "Applying migration PreviousSchemePage"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /previousSchemePage                        controllers.PreviousSchemePageController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /previousSchemePage                        controllers.PreviousSchemePageController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changePreviousSchemePage                  controllers.PreviousSchemePageController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changePreviousSchemePage                  controllers.PreviousSchemePageController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "previousSchemePage.title = previousSchemePage" >> ../conf/messages.en
echo "previousSchemePage.heading = previousSchemePage" >> ../conf/messages.en
echo "previousSchemePage.ossu = One Stop Shop UInion" >> ../conf/messages.en
echo "previousSchemePage.ossnu = One Stop Shop non-Union" >> ../conf/messages.en
echo "previousSchemePage.checkYourAnswersLabel = previousSchemePage" >> ../conf/messages.en
echo "previousSchemePage.error.required = Select previousSchemePage" >> ../conf/messages.en
echo "previousSchemePage.change.hidden = PreviousSchemePage" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryPreviousSchemePageUserAnswersEntry: Arbitrary[(PreviousSchemePagePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[PreviousSchemePagePage.type]";\
    print "        value <- arbitrary[PreviousSchemePage].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryPreviousSchemePagePage: Arbitrary[PreviousSchemePagePage.type] =";\
    print "    Arbitrary(PreviousSchemePagePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryPreviousSchemePage: Arbitrary[PreviousSchemePage] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(PreviousSchemePage.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(PreviousSchemePagePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration PreviousSchemePage completed"
