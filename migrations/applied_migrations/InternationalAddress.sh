#!/bin/bash

echo ""
echo "Applying migration InternationalAddress"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /internationalAddress                        controllers.InternationalAddressController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /internationalAddress                        controllers.InternationalAddressController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeInternationalAddress                  controllers.InternationalAddressController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeInternationalAddress                  controllers.InternationalAddressController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "internationalAddress.title = internationalAddress" >> ../conf/messages.en
echo "internationalAddress.heading = internationalAddress" >> ../conf/messages.en
echo "internationalAddress.line1 = line1" >> ../conf/messages.en
echo "internationalAddress.line2 = line2" >> ../conf/messages.en
echo "internationalAddress.checkYourAnswersLabel = InternationalAddress" >> ../conf/messages.en
echo "internationalAddress.error.line1.required = Enter line1" >> ../conf/messages.en
echo "internationalAddress.error.line2.required = Enter line2" >> ../conf/messages.en
echo "internationalAddress.error.line1.length = line1 must be 100 characters or less" >> ../conf/messages.en
echo "internationalAddress.error.line2.length = line2 must be 100 characters or less" >> ../conf/messages.en
echo "internationalAddress.line1.change.hidden = line1" >> ../conf/messages.en
echo "internationalAddress.line2.change.hidden = line2" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryInternationalAddressUserAnswersEntry: Arbitrary[(InternationalAddressPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[InternationalAddressPage.type]";\
    print "        value <- arbitrary[InternationalAddress].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryInternationalAddressPage: Arbitrary[InternationalAddressPage.type] =";\
    print "    Arbitrary(InternationalAddressPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryInternationalAddress: Arbitrary[InternationalAddress] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        line1 <- arbitrary[String]";\
    print "        line2 <- arbitrary[String]";\
    print "      } yield InternationalAddress(line1, line2)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(InternationalAddressPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration InternationalAddress completed"
