#!/bin/bash

echo ""
echo "Applying migration EuSendGoodsAddress"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /euSendGoodsAddress                        controllers.EuSendGoodsAddressController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /euSendGoodsAddress                        controllers.EuSendGoodsAddressController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeEuSendGoodsAddress                  controllers.EuSendGoodsAddressController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeEuSendGoodsAddress                  controllers.EuSendGoodsAddressController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "euSendGoodsAddress.title = euSendGoodsAddress" >> ../conf/messages.en
echo "euSendGoodsAddress.heading = euSendGoodsAddress" >> ../conf/messages.en
echo "euSendGoodsAddress.checkYourAnswersLabel = euSendGoodsAddress" >> ../conf/messages.en
echo "euSendGoodsAddress.error.required = Enter euSendGoodsAddress" >> ../conf/messages.en
echo "euSendGoodsAddress.error.length = EuSendGoodsAddress must be 100 characters or less" >> ../conf/messages.en
echo "euSendGoodsAddress.change.hidden = EuSendGoodsAddress" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryEuSendGoodsAddressUserAnswersEntry: Arbitrary[(EuSendGoodsAddressPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[EuSendGoodsAddressPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryEuSendGoodsAddressPage: Arbitrary[EuSendGoodsAddressPage.type] =";\
    print "    Arbitrary(EuSendGoodsAddressPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(EuSendGoodsAddressPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration EuSendGoodsAddress completed"
