#!/bin/bash

echo ""
echo "Applying migration DeleteTradingName"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /deleteTradingName                        controllers.DeleteTradingNameController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /deleteTradingName                        controllers.DeleteTradingNameController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeDeleteTradingName                  controllers.DeleteTradingNameController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeDeleteTradingName                  controllers.DeleteTradingNameController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "deleteTradingName.title = deleteTradingName" >> ../conf/messages.en
echo "deleteTradingName.heading = deleteTradingName" >> ../conf/messages.en
echo "deleteTradingName.checkYourAnswersLabel = deleteTradingName" >> ../conf/messages.en
echo "deleteTradingName.error.required = Select yes if deleteTradingName" >> ../conf/messages.en
echo "deleteTradingName.change.hidden = DeleteTradingName" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryDeleteTradingNameUserAnswersEntry: Arbitrary[(DeleteTradingNamePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[DeleteTradingNamePage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryDeleteTradingNamePage: Arbitrary[DeleteTradingNamePage.type] =";\
    print "    Arbitrary(DeleteTradingNamePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(DeleteTradingNamePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration DeleteTradingName completed"
