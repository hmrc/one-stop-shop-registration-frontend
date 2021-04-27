#!/bin/bash

echo ""
echo "Applying migration UkVatRegisteredPostcode"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /ukVatRegisteredPostcode                        controllers.UkVatRegisteredPostcodeController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /ukVatRegisteredPostcode                        controllers.UkVatRegisteredPostcodeController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeUkVatRegisteredPostcode                  controllers.UkVatRegisteredPostcodeController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeUkVatRegisteredPostcode                  controllers.UkVatRegisteredPostcodeController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "ukVatRegisteredPostcode.title = ukVatRegisteredPostcode" >> ../conf/messages.en
echo "ukVatRegisteredPostcode.heading = ukVatRegisteredPostcode" >> ../conf/messages.en
echo "ukVatRegisteredPostcode.checkYourAnswersLabel = ukVatRegisteredPostcode" >> ../conf/messages.en
echo "ukVatRegisteredPostcode.error.required = Enter ukVatRegisteredPostcode" >> ../conf/messages.en
echo "ukVatRegisteredPostcode.error.length = UkVatRegisteredPostcode must be 9 characters or less" >> ../conf/messages.en
echo "ukVatRegisteredPostcode.change.hidden = UkVatRegisteredPostcode" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryUkVatRegisteredPostcodeUserAnswersEntry: Arbitrary[(UkVatRegisteredPostcodePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[UkVatRegisteredPostcodePage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryUkVatRegisteredPostcodePage: Arbitrary[UkVatRegisteredPostcodePage.type] =";\
    print "    Arbitrary(UkVatRegisteredPostcodePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(UkVatRegisteredPostcodePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration UkVatRegisteredPostcode completed"
