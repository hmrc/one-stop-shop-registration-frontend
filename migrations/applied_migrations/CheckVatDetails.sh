#!/bin/bash

echo ""
echo "Applying migration CheckVatDetails"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /checkVatDetails                        controllers.CheckVatDetailsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /checkVatDetails                        controllers.CheckVatDetailsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeCheckVatDetails                  controllers.CheckVatDetailsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeCheckVatDetails                  controllers.CheckVatDetailsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "checkVatDetails.title = checkVatDetails" >> ../conf/messages.en
echo "checkVatDetails.heading = checkVatDetails" >> ../conf/messages.en
echo "checkVatDetails.checkYourAnswersLabel = checkVatDetails" >> ../conf/messages.en
echo "checkVatDetails.error.required = Select yes if checkVatDetails" >> ../conf/messages.en
echo "checkVatDetails.change.hidden = CheckVatDetails" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryCheckVatDetailsUserAnswersEntry: Arbitrary[(CheckVatDetailsPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[CheckVatDetailsPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryCheckVatDetailsPage: Arbitrary[CheckVatDetailsPage.type] =";\
    print "    Arbitrary(CheckVatDetailsPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(CheckVatDetailsPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration CheckVatDetails completed"
