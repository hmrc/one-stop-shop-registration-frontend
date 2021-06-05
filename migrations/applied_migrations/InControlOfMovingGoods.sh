#!/bin/bash

echo ""
echo "Applying migration InControlOfMovingGoods"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /inControlOfMovingGoods                        controllers.InControlOfMovingGoodsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /inControlOfMovingGoods                        controllers.InControlOfMovingGoodsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeInControlOfMovingGoods                  controllers.InControlOfMovingGoodsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeInControlOfMovingGoods                  controllers.InControlOfMovingGoodsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "inControlOfMovingGoods.title = inControlOfMovingGoods" >> ../conf/messages.en
echo "inControlOfMovingGoods.heading = inControlOfMovingGoods" >> ../conf/messages.en
echo "inControlOfMovingGoods.checkYourAnswersLabel = inControlOfMovingGoods" >> ../conf/messages.en
echo "inControlOfMovingGoods.error.required = Select yes if inControlOfMovingGoods" >> ../conf/messages.en
echo "inControlOfMovingGoods.change.hidden = InControlOfMovingGoods" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryInControlOfMovingGoodsUserAnswersEntry: Arbitrary[(InControlOfMovingGoodsPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[InControlOfMovingGoodsPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryInControlOfMovingGoodsPage: Arbitrary[InControlOfMovingGoodsPage.type] =";\
    print "    Arbitrary(InControlOfMovingGoodsPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(InControlOfMovingGoodsPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration InControlOfMovingGoods completed"
