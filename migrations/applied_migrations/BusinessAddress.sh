#!/bin/bash

echo ""
echo "Applying migration BusinessAddress"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /businessAddress                        controllers.BusinessAddressController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /businessAddress                        controllers.BusinessAddressController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeBusinessAddress                  controllers.BusinessAddressController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeBusinessAddress                  controllers.BusinessAddressController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "businessAddress.title = businessAddress" >> ../conf/messages.en
echo "businessAddress.heading = businessAddress" >> ../conf/messages.en
echo "businessAddress.Line1 = Line1" >> ../conf/messages.en
echo "businessAddress.Line2 = Line2" >> ../conf/messages.en
echo "businessAddress.checkYourAnswersLabel = BusinessAddress" >> ../conf/messages.en
echo "businessAddress.error.Line1.required = Enter Line1" >> ../conf/messages.en
echo "businessAddress.error.Line2.required = Enter Line2" >> ../conf/messages.en
echo "businessAddress.error.Line1.length = Line1 must be 100 characters or less" >> ../conf/messages.en
echo "businessAddress.error.Line2.length = Line2 must be 100 characters or less" >> ../conf/messages.en
echo "businessAddress.Line1.change.hidden = Line1" >> ../conf/messages.en
echo "businessAddress.Line2.change.hidden = Line2" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryBusinessAddressUserAnswersEntry: Arbitrary[(BusinessAddressPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[BusinessAddressPage.type]";\
    print "        value <- arbitrary[BusinessAddress].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryBusinessAddressPage: Arbitrary[BusinessAddressPage.type] =";\
    print "    Arbitrary(BusinessAddressPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryBusinessAddress: Arbitrary[BusinessAddress] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        Line1 <- arbitrary[String]";\
    print "        Line2 <- arbitrary[String]";\
    print "      } yield BusinessAddress(Line1, Line2)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(BusinessAddressPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration BusinessAddress completed"
