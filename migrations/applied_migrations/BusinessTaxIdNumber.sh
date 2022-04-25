#!/bin/bash

echo ""
echo "Applying migration BusinessTaxIdNumber"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /businessTaxIdNumber                        controllers.BusinessTaxIdNumberController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /businessTaxIdNumber                        controllers.BusinessTaxIdNumberController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeBusinessTaxIdNumber                  controllers.BusinessTaxIdNumberController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeBusinessTaxIdNumber                  controllers.BusinessTaxIdNumberController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "businessTaxIdNumber.title = businessTaxIdNumber" >> ../conf/messages.en
echo "businessTaxIdNumber.heading = businessTaxIdNumber" >> ../conf/messages.en
echo "businessTaxIdNumber.checkYourAnswersLabel = businessTaxIdNumber" >> ../conf/messages.en
echo "businessTaxIdNumber.error.required = Enter businessTaxIdNumber" >> ../conf/messages.en
echo "businessTaxIdNumber.error.length = BusinessTaxIdNumber must be 100 characters or less" >> ../conf/messages.en
echo "businessTaxIdNumber.change.hidden = BusinessTaxIdNumber" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryBusinessTaxIdNumberUserAnswersEntry: Arbitrary[(BusinessTaxIdNumberPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[BusinessTaxIdNumberPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryBusinessTaxIdNumberPage: Arbitrary[BusinessTaxIdNumberPage.type] =";\
    print "    Arbitrary(BusinessTaxIdNumberPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(BusinessTaxIdNumberPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration BusinessTaxIdNumber completed"
