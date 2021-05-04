#!/bin/bash

echo ""
echo "Applying migration DeleteEuVatDetails"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /deleteEuVatDetails                        controllers.DeleteEuVatDetailsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /deleteEuVatDetails                        controllers.DeleteEuVatDetailsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeDeleteEuVatDetails                  controllers.DeleteEuVatDetailsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeDeleteEuVatDetails                  controllers.DeleteEuVatDetailsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "deleteEuVatDetails.title = deleteEuVatDetails" >> ../conf/messages.en
echo "deleteEuVatDetails.heading = deleteEuVatDetails" >> ../conf/messages.en
echo "deleteEuVatDetails.checkYourAnswersLabel = deleteEuVatDetails" >> ../conf/messages.en
echo "deleteEuVatDetails.error.required = Select yes if deleteEuVatDetails" >> ../conf/messages.en
echo "deleteEuVatDetails.change.hidden = DeleteEuVatDetails" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryDeleteEuVatDetailsUserAnswersEntry: Arbitrary[(DeleteEuVatDetailsPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[DeleteEuVatDetailsPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryDeleteEuVatDetailsPage: Arbitrary[DeleteEuVatDetailsPage.type] =";\
    print "    Arbitrary(DeleteEuVatDetailsPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(DeleteEuVatDetailsPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration DeleteEuVatDetails completed"
