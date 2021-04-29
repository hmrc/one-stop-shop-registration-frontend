#!/bin/bash

echo ""
echo "Applying migration AddAdditionalEuVatDetails"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /addAdditionalEuVatDetails                        controllers.AddAdditionalEuVatDetailsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /addAdditionalEuVatDetails                        controllers.AddAdditionalEuVatDetailsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeAddAdditionalEuVatDetails                  controllers.AddAdditionalEuVatDetailsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeAddAdditionalEuVatDetails                  controllers.AddAdditionalEuVatDetailsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "addAdditionalEuVatDetails.title = addAdditionalEuVatDetails" >> ../conf/messages.en
echo "addAdditionalEuVatDetails.heading = addAdditionalEuVatDetails" >> ../conf/messages.en
echo "addAdditionalEuVatDetails.checkYourAnswersLabel = addAdditionalEuVatDetails" >> ../conf/messages.en
echo "addAdditionalEuVatDetails.error.required = Select yes if addAdditionalEuVatDetails" >> ../conf/messages.en
echo "addAdditionalEuVatDetails.change.hidden = AddAdditionalEuVatDetails" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAddAdditionalEuVatDetailsUserAnswersEntry: Arbitrary[(AddAdditionalEuVatDetailsPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[AddAdditionalEuVatDetailsPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAddAdditionalEuVatDetailsPage: Arbitrary[AddAdditionalEuVatDetailsPage.type] =";\
    print "    Arbitrary(AddAdditionalEuVatDetailsPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(AddAdditionalEuVatDetailsPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration AddAdditionalEuVatDetails completed"
