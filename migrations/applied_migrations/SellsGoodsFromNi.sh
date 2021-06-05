#!/bin/bash

echo ""
echo "Applying migration SellsGoodsFromNi"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /sellsGoodsFromNi                        controllers.SellsGoodsFromNiController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /sellsGoodsFromNi                        controllers.SellsGoodsFromNiController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeSellsGoodsFromNi                  controllers.SellsGoodsFromNiController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeSellsGoodsFromNi                  controllers.SellsGoodsFromNiController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "sellsGoodsFromNi.title = sellsGoodsFromNi" >> ../conf/messages.en
echo "sellsGoodsFromNi.heading = sellsGoodsFromNi" >> ../conf/messages.en
echo "sellsGoodsFromNi.checkYourAnswersLabel = sellsGoodsFromNi" >> ../conf/messages.en
echo "sellsGoodsFromNi.error.required = Select yes if sellsGoodsFromNi" >> ../conf/messages.en
echo "sellsGoodsFromNi.change.hidden = SellsGoodsFromNi" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrarySellsGoodsFromNiUserAnswersEntry: Arbitrary[(SellsGoodsFromNiPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[SellsGoodsFromNiPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrarySellsGoodsFromNiPage: Arbitrary[SellsGoodsFromNiPage.type] =";\
    print "    Arbitrary(SellsGoodsFromNiPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(SellsGoodsFromNiPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration SellsGoodsFromNi completed"
