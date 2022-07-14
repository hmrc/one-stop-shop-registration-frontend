#!/bin/bash

echo ""
echo "Applying migration EuSendGoodsTradingName"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /euSendGoodsTradingName                        controllers.EuSendGoodsTradingNameController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /euSendGoodsTradingName                        controllers.EuSendGoodsTradingNameController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeEuSendGoodsTradingName                  controllers.EuSendGoodsTradingNameController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeEuSendGoodsTradingName                  controllers.EuSendGoodsTradingNameController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "euSendGoodsTradingName.title = euSendGoodsTradingName" >> ../conf/messages.en
echo "euSendGoodsTradingName.heading = euSendGoodsTradingName" >> ../conf/messages.en
echo "euSendGoodsTradingName.checkYourAnswersLabel = euSendGoodsTradingName" >> ../conf/messages.en
echo "euSendGoodsTradingName.error.required = Enter euSendGoodsTradingName" >> ../conf/messages.en
echo "euSendGoodsTradingName.error.length = EuSendGoodsTradingName must be 100 characters or less" >> ../conf/messages.en
echo "euSendGoodsTradingName.change.hidden = EuSendGoodsTradingName" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryEuSendGoodsTradingNameUserAnswersEntry: Arbitrary[(EuSendGoodsTradingNamePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[EuSendGoodsTradingNamePage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryEuSendGoodsTradingNamePage: Arbitrary[EuSendGoodsTradingNamePage.type] =";\
    print "    Arbitrary(EuSendGoodsTradingNamePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(EuSendGoodsTradingNamePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration EuSendGoodsTradingName completed"
