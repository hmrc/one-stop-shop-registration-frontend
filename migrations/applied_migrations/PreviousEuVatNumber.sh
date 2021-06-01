#!/bin/bash

echo ""
echo "Applying migration PreviousEuVatNumber"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /previousEuVatNumber                        controllers.PreviousEuVatNumberController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /previousEuVatNumber                        controllers.PreviousEuVatNumberController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changePreviousEuVatNumber                  controllers.PreviousEuVatNumberController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changePreviousEuVatNumber                  controllers.PreviousEuVatNumberController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "previousEuVatNumber.title = previousEuVatNumber" >> ../conf/messages.en
echo "previousEuVatNumber.heading = previousEuVatNumber" >> ../conf/messages.en
echo "previousEuVatNumber.checkYourAnswersLabel = previousEuVatNumber" >> ../conf/messages.en
echo "previousEuVatNumber.error.required = Enter previousEuVatNumber" >> ../conf/messages.en
echo "previousEuVatNumber.error.length = PreviousEuVatNumber must be 100 characters or less" >> ../conf/messages.en
echo "previousEuVatNumber.change.hidden = PreviousEuVatNumber" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryPreviousEuVatNumberUserAnswersEntry: Arbitrary[(PreviousEuVatNumberPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[PreviousEuVatNumberPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryPreviousEuVatNumberPage: Arbitrary[PreviousEuVatNumberPage.type] =";\
    print "    Arbitrary(PreviousEuVatNumberPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(PreviousEuVatNumberPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration PreviousEuVatNumber completed"
