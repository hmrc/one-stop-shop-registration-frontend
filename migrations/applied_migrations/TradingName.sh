#!/bin/bash

echo ""
echo "Applying migration TradingName"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /tradingName                        controllers.TradingNameController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /tradingName                        controllers.TradingNameController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeTradingName                  controllers.TradingNameController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeTradingName                  controllers.TradingNameController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "tradingName.title = tradingName" >> ../conf/messages.en
echo "tradingName.heading = tradingName" >> ../conf/messages.en
echo "tradingName.checkYourAnswersLabel = tradingName" >> ../conf/messages.en
echo "tradingName.error.required = Enter tradingName" >> ../conf/messages.en
echo "tradingName.error.length = TradingName must be 100 characters or less" >> ../conf/messages.en
echo "tradingName.change.hidden = TradingName" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryTradingNameUserAnswersEntry: Arbitrary[(TradingNamePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[TradingNamePage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryTradingNamePage: Arbitrary[TradingNamePage.type] =";\
    print "    Arbitrary(TradingNamePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(TradingNamePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration TradingName completed"
