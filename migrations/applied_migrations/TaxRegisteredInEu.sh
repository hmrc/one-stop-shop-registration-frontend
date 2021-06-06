#!/bin/bash

echo ""
echo "Applying migration TaxRegisteredInEu"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /taxRegisteredInEu                        controllers.TaxRegisteredInEuController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /taxRegisteredInEu                        controllers.TaxRegisteredInEuController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeTaxRegisteredInEu                  controllers.TaxRegisteredInEuController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeTaxRegisteredInEu                  controllers.TaxRegisteredInEuController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "taxRegisteredInEu.title = taxRegisteredInEu" >> ../conf/messages.en
echo "taxRegisteredInEu.heading = taxRegisteredInEu" >> ../conf/messages.en
echo "taxRegisteredInEu.checkYourAnswersLabel = taxRegisteredInEu" >> ../conf/messages.en
echo "taxRegisteredInEu.error.required = Select yes if taxRegisteredInEu" >> ../conf/messages.en
echo "taxRegisteredInEu.change.hidden = TaxRegisteredInEu" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryTaxRegisteredInEuUserAnswersEntry: Arbitrary[(TaxRegisteredInEuPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[TaxRegisteredInEuPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryTaxRegisteredInEuPage: Arbitrary[TaxRegisteredInEuPage.type] =";\
    print "    Arbitrary(TaxRegisteredInEuPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(TaxRegisteredInEuPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration TaxRegisteredInEu completed"
