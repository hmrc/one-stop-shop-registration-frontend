#!/bin/bash

echo ""
echo "Applying migration DeleteAllEuDetails"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /deleteAllEuDetails                        controllers.DeleteAllEuDetailsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /deleteAllEuDetails                        controllers.DeleteAllEuDetailsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeDeleteAllEuDetails                  controllers.DeleteAllEuDetailsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeDeleteAllEuDetails                  controllers.DeleteAllEuDetailsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "deleteAllEuDetails.title = deleteAllEuDetails" >> ../conf/messages.en
echo "deleteAllEuDetails.heading = deleteAllEuDetails" >> ../conf/messages.en
echo "deleteAllEuDetails.checkYourAnswersLabel = deleteAllEuDetails" >> ../conf/messages.en
echo "deleteAllEuDetails.error.required = Select yes if deleteAllEuDetails" >> ../conf/messages.en
echo "deleteAllEuDetails.change.hidden = DeleteAllEuDetails" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryDeleteAllEuDetailsUserAnswersEntry: Arbitrary[(DeleteAllEuDetailsPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[DeleteAllEuDetailsPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryDeleteAllEuDetailsPage: Arbitrary[DeleteAllEuDetailsPage.type] =";\
    print "    Arbitrary(DeleteAllEuDetailsPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(DeleteAllEuDetailsPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration DeleteAllEuDetails completed"
