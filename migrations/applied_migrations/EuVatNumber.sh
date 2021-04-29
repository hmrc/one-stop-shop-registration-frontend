#!/bin/bash

echo ""
echo "Applying migration EuVatNumber"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /euVatNumber                        controllers.EuVatNumberController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /euVatNumber                        controllers.EuVatNumberController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeEuVatNumber                  controllers.EuVatNumberController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeEuVatNumber                  controllers.EuVatNumberController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "euVatNumber.title = euVatNumber" >> ../conf/messages.en
echo "euVatNumber.heading = euVatNumber" >> ../conf/messages.en
echo "euVatNumber.checkYourAnswersLabel = euVatNumber" >> ../conf/messages.en
echo "euVatNumber.error.required = Enter euVatNumber" >> ../conf/messages.en
echo "euVatNumber.error.length = EuVatNumber must be 100 characters or less" >> ../conf/messages.en
echo "euVatNumber.change.hidden = EuVatNumber" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryEuVatNumberUserAnswersEntry: Arbitrary[(EuVatNumberPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[EuVatNumberPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryEuVatNumberPage: Arbitrary[EuVatNumberPage.type] =";\
    print "    Arbitrary(EuVatNumberPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(EuVatNumberPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration EuVatNumber completed"
