#!/bin/bash

echo ""
echo "Applying migration DeleteAllTradingNames"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /deleteAllTradingNames                        controllers.DeleteAllTradingNamesController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /deleteAllTradingNames                        controllers.DeleteAllTradingNamesController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeDeleteAllTradingNames                  controllers.DeleteAllTradingNamesController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeDeleteAllTradingNames                  controllers.DeleteAllTradingNamesController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "deleteAllTradingNames.title = deleteAllTradingNames" >> ../conf/messages.en
echo "deleteAllTradingNames.heading = deleteAllTradingNames" >> ../conf/messages.en
echo "deleteAllTradingNames.checkYourAnswersLabel = deleteAllTradingNames" >> ../conf/messages.en
echo "deleteAllTradingNames.error.required = Select yes if deleteAllTradingNames" >> ../conf/messages.en
echo "deleteAllTradingNames.change.hidden = DeleteAllTradingNames" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryDeleteAllTradingNamesUserAnswersEntry: Arbitrary[(DeleteAllTradingNamesPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[DeleteAllTradingNamesPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryDeleteAllTradingNamesPage: Arbitrary[DeleteAllTradingNamesPage.type] =";\
    print "    Arbitrary(DeleteAllTradingNamesPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(DeleteAllTradingNamesPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration DeleteAllTradingNames completed"
