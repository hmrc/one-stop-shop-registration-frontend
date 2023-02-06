#!/bin/bash

echo ""
echo "Applying migration SellsGoodsToEUConsumerMethod"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /sellsGoodsToEUConsumerMethod                        controllers.SellsGoodsToEUConsumerMethodController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /sellsGoodsToEUConsumerMethod                        controllers.SellsGoodsToEUConsumerMethodController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeSellsGoodsToEUConsumerMethod                  controllers.SellsGoodsToEUConsumerMethodController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeSellsGoodsToEUConsumerMethod                  controllers.SellsGoodsToEUConsumerMethodController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "sellsGoodsToEUConsumerMethod.title = sellsGoodsToEUConsumerMethod" >> ../conf/messages.en
echo "sellsGoodsToEUConsumerMethod.heading = sellsGoodsToEUConsumerMethod" >> ../conf/messages.en
echo "sellsGoodsToEUConsumerMethod.checkYourAnswersLabel = sellsGoodsToEUConsumerMethod" >> ../conf/messages.en
echo "sellsGoodsToEUConsumerMethod.error.required = Select yes if sellsGoodsToEUConsumerMethod" >> ../conf/messages.en
echo "sellsGoodsToEUConsumerMethod.change.hidden = SellsGoodsToEUConsumerMethod" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrarySellsGoodsToEUConsumerMethodUserAnswersEntry: Arbitrary[(SellsGoodsToEUConsumerMethodPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[SellsGoodsToEUConsumerMethodPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrarySellsGoodsToEUConsumerMethodPage: Arbitrary[SellsGoodsToEUConsumerMethodPage.type] =";\
    print "    Arbitrary(SellsGoodsToEUConsumerMethodPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(SellsGoodsToEUConsumerMethodPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration SellsGoodsToEUConsumerMethod completed"
