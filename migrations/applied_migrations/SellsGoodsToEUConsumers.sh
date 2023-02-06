#!/bin/bash

echo ""
echo "Applying migration SellsGoodsToEUConsumers"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /sellsGoodsToEUConsumers                        controllers.SellsGoodsToEUConsumersController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /sellsGoodsToEUConsumers                        controllers.SellsGoodsToEUConsumersController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeSellsGoodsToEUConsumers                  controllers.SellsGoodsToEUConsumersController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeSellsGoodsToEUConsumers                  controllers.SellsGoodsToEUConsumersController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "sellsGoodsToEUConsumers.title = sellsGoodsToEUConsumers" >> ../conf/messages.en
echo "sellsGoodsToEUConsumers.heading = sellsGoodsToEUConsumers" >> ../conf/messages.en
echo "sellsGoodsToEUConsumers.checkYourAnswersLabel = sellsGoodsToEUConsumers" >> ../conf/messages.en
echo "sellsGoodsToEUConsumers.error.required = Select yes if sellsGoodsToEUConsumers" >> ../conf/messages.en
echo "sellsGoodsToEUConsumers.change.hidden = SellsGoodsToEUConsumers" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrarySellsGoodsToEUConsumersUserAnswersEntry: Arbitrary[(SellsGoodsToEUConsumersPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[SellsGoodsToEUConsumersPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrarySellsGoodsToEUConsumersPage: Arbitrary[SellsGoodsToEUConsumersPage.type] =";\
    print "    Arbitrary(SellsGoodsToEUConsumersPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(SellsGoodsToEUConsumersPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration SellsGoodsToEUConsumers completed"
