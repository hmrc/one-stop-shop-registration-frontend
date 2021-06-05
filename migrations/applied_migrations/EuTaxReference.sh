#!/bin/bash

echo ""
echo "Applying migration EuTaxReference"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /euTaxReference                        controllers.EuTaxReferenceController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /euTaxReference                        controllers.EuTaxReferenceController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeEuTaxReference                  controllers.EuTaxReferenceController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeEuTaxReference                  controllers.EuTaxReferenceController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "euTaxReference.title = euTaxReference" >> ../conf/messages.en
echo "euTaxReference.heading = euTaxReference" >> ../conf/messages.en
echo "euTaxReference.checkYourAnswersLabel = euTaxReference" >> ../conf/messages.en
echo "euTaxReference.error.required = Enter euTaxReference" >> ../conf/messages.en
echo "euTaxReference.error.length = EuTaxReference must be 100 characters or less" >> ../conf/messages.en
echo "euTaxReference.change.hidden = EuTaxReference" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryEuTaxReferenceUserAnswersEntry: Arbitrary[(EuTaxReferencePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[EuTaxReferencePage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryEuTaxReferencePage: Arbitrary[EuTaxReferencePage.type] =";\
    print "    Arbitrary(EuTaxReferencePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(EuTaxReferencePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration EuTaxReference completed"
