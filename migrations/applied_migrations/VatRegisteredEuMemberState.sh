#!/bin/bash

echo ""
echo "Applying migration VatRegisteredEuMemberState"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /vatRegisteredEuMemberState                        controllers.VatRegisteredEuMemberStateController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /vatRegisteredEuMemberState                        controllers.VatRegisteredEuMemberStateController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeVatRegisteredEuMemberState                  controllers.VatRegisteredEuMemberStateController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeVatRegisteredEuMemberState                  controllers.VatRegisteredEuMemberStateController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "euCountry.title = vatRegisteredEuMemberState" >> ../conf/messages.en
echo "euCountry.heading = vatRegisteredEuMemberState" >> ../conf/messages.en
echo "euCountry.checkYourAnswersLabel = vatRegisteredEuMemberState" >> ../conf/messages.en
echo "euCountry.error.required = Enter vatRegisteredEuMemberState" >> ../conf/messages.en
echo "euCountry.error.length = VatRegisteredEuMemberState must be 100 characters or less" >> ../conf/messages.en
echo "euCountry.change.hidden = VatRegisteredEuMemberState" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryVatRegisteredEuMemberStateUserAnswersEntry: Arbitrary[(VatRegisteredEuMemberStatePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[VatRegisteredEuMemberStatePage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryVatRegisteredEuMemberStatePage: Arbitrary[VatRegisteredEuMemberStatePage.type] =";\
    print "    Arbitrary(VatRegisteredEuMemberStatePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(VatRegisteredEuMemberStatePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration VatRegisteredEuMemberState completed"
