#!/bin/bash

echo ""
echo "Applying migration IntendToSellGoodsThisQuarter"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /intendToSellGoodsThisQuarter                        controllers.IntendToSellGoodsThisQuarterController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /intendToSellGoodsThisQuarter                        controllers.IntendToSellGoodsThisQuarterController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeIntendToSellGoodsThisQuarter                  controllers.IntendToSellGoodsThisQuarterController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeIntendToSellGoodsThisQuarter                  controllers.IntendToSellGoodsThisQuarterController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "intendToSellGoodsThisQuarter.title = intendToSellGoodsThisQuarter" >> ../conf/messages.en
echo "intendToSellGoodsThisQuarter.heading = intendToSellGoodsThisQuarter" >> ../conf/messages.en
echo "intendToSellGoodsThisQuarter.checkYourAnswersLabel = intendToSellGoodsThisQuarter" >> ../conf/messages.en
echo "intendToSellGoodsThisQuarter.error.required = Select yes if intendToSellGoodsThisQuarter" >> ../conf/messages.en
echo "intendToSellGoodsThisQuarter.change.hidden = IntendToSellGoodsThisQuarter" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryIntendToSellGoodsThisQuarterUserAnswersEntry: Arbitrary[(IntendToSellGoodsThisQuarterPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[IntendToSellGoodsThisQuarterPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryIntendToSellGoodsThisQuarterPage: Arbitrary[IntendToSellGoodsThisQuarterPage.type] =";\
    print "    Arbitrary(IntendToSellGoodsThisQuarterPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(IntendToSellGoodsThisQuarterPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration IntendToSellGoodsThisQuarter completed"
