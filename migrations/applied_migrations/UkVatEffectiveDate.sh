#!/bin/bash

echo ""
echo "Applying migration UkVatEffectiveDate"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /ukVatEffectiveDate                  controllers.UkVatEffectiveDateController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /ukVatEffectiveDate                  controllers.UkVatEffectiveDateController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeUkVatEffectiveDate                        controllers.UkVatEffectiveDateController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeUkVatEffectiveDate                        controllers.UkVatEffectiveDateController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "ukVatEffectiveDate.title = UkVatEffectiveDate" >> ../conf/messages.en
echo "ukVatEffectiveDate.heading = UkVatEffectiveDate" >> ../conf/messages.en
echo "ukVatEffectiveDate.hint = For example, 12 11 2007" >> ../conf/messages.en
echo "ukVatEffectiveDate.checkYourAnswersLabel = UkVatEffectiveDate" >> ../conf/messages.en
echo "ukVatEffectiveDate.error.required.all = Enter the ukVatEffectiveDate" >> ../conf/messages.en
echo "ukVatEffectiveDate.error.required.two = The ukVatEffectiveDate" must include {0} and {1} >> ../conf/messages.en
echo "ukVatEffectiveDate.error.required = The ukVatEffectiveDate must include {0}" >> ../conf/messages.en
echo "ukVatEffectiveDate.error.invalid = Enter a real UkVatEffectiveDate" >> ../conf/messages.en
echo "ukVatEffectiveDate.change.hidden = UkVatEffectiveDate" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryUkVatEffectiveDateUserAnswersEntry: Arbitrary[(UkVatEffectiveDatePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[UkVatEffectiveDatePage.type]";\
    print "        value <- arbitrary[Int].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryUkVatEffectiveDatePage: Arbitrary[UkVatEffectiveDatePage.type] =";\
    print "    Arbitrary(UkVatEffectiveDatePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(UkVatEffectiveDatePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration UkVatEffectiveDate completed"
