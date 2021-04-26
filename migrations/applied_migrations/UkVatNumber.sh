#!/bin/bash

echo ""
echo "Applying migration UkVatNumber"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /ukVatNumber                        controllers.UkVatNumberController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /ukVatNumber                        controllers.UkVatNumberController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeUkVatNumber                  controllers.UkVatNumberController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeUkVatNumber                  controllers.UkVatNumberController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "ukVatNumber.title = ukVatNumber" >> ../conf/messages.en
echo "ukVatNumber.heading = ukVatNumber" >> ../conf/messages.en
echo "ukVatNumber.checkYourAnswersLabel = ukVatNumber" >> ../conf/messages.en
echo "ukVatNumber.error.required = Enter ukVatNumber" >> ../conf/messages.en
echo "ukVatNumber.error.length = UkVatNumber must be 11 characters or less" >> ../conf/messages.en
echo "ukVatNumber.change.hidden = UkVatNumber" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryUkVatNumberUserAnswersEntry: Arbitrary[(UkVatNumberPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[UkVatNumberPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryUkVatNumberPage: Arbitrary[UkVatNumberPage.type] =";\
    print "    Arbitrary(UkVatNumberPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(UkVatNumberPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration UkVatNumber completed"
