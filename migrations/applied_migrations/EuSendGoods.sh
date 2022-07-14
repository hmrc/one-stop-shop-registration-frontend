#!/bin/bash

echo ""
echo "Applying migration EuSendGoods"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /euSendGoods                        controllers.EuSendGoodsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /euSendGoods                        controllers.EuSendGoodsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeEuSendGoods                  controllers.EuSendGoodsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeEuSendGoods                  controllers.EuSendGoodsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "euSendGoods.title = euSendGoods" >> ../conf/messages.en
echo "euSendGoods.heading = euSendGoods" >> ../conf/messages.en
echo "euSendGoods.checkYourAnswersLabel = euSendGoods" >> ../conf/messages.en
echo "euSendGoods.error.required = Select yes if euSendGoods" >> ../conf/messages.en
echo "euSendGoods.change.hidden = EuSendGoods" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryEuSendGoodsUserAnswersEntry: Arbitrary[(EuSendGoodsPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[EuSendGoodsPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryEuSendGoodsPage: Arbitrary[EuSendGoodsPage.type] =";\
    print "    Arbitrary(EuSendGoodsPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(EuSendGoodsPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration EuSendGoods completed"
