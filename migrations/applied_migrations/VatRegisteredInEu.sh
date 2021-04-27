#!/bin/bash

echo ""
echo "Applying migration VatRegisteredInEu"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /vatRegisteredInEu                        controllers.VatRegisteredInEuController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /vatRegisteredInEu                        controllers.VatRegisteredInEuController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeVatRegisteredInEu                  controllers.VatRegisteredInEuController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeVatRegisteredInEu                  controllers.VatRegisteredInEuController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "vatRegisteredInEu.title = vatRegisteredInEu" >> ../conf/messages.en
echo "vatRegisteredInEu.heading = vatRegisteredInEu" >> ../conf/messages.en
echo "vatRegisteredInEu.checkYourAnswersLabel = vatRegisteredInEu" >> ../conf/messages.en
echo "vatRegisteredInEu.error.required = Select yes if vatRegisteredInEu" >> ../conf/messages.en
echo "vatRegisteredInEu.change.hidden = VatRegisteredInEu" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryVatRegisteredInEuUserAnswersEntry: Arbitrary[(VatRegisteredInEuPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[VatRegisteredInEuPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryVatRegisteredInEuPage: Arbitrary[VatRegisteredInEuPage.type] =";\
    print "    Arbitrary(VatRegisteredInEuPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(VatRegisteredInEuPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration VatRegisteredInEu completed"
