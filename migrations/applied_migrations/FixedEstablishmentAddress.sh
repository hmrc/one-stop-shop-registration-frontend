#!/bin/bash

echo ""
echo "Applying migration FixedEstablishmentAddress"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /fixedEstablishmentAddress                        controllers.FixedEstablishmentAddressController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /fixedEstablishmentAddress                        controllers.FixedEstablishmentAddressController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeFixedEstablishmentAddress                  controllers.FixedEstablishmentAddressController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeFixedEstablishmentAddress                  controllers.FixedEstablishmentAddressController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "fixedEstablishmentAddress.title = fixedEstablishmentAddress" >> ../conf/messages.en
echo "fixedEstablishmentAddress.heading = fixedEstablishmentAddress" >> ../conf/messages.en
echo "fixedEstablishmentAddress.field1 = field1" >> ../conf/messages.en
echo "fixedEstablishmentAddress.field2 = field2" >> ../conf/messages.en
echo "fixedEstablishmentAddress.checkYourAnswersLabel = FixedEstablishmentAddress" >> ../conf/messages.en
echo "fixedEstablishmentAddress.error.field1.required = Enter field1" >> ../conf/messages.en
echo "fixedEstablishmentAddress.error.field2.required = Enter field2" >> ../conf/messages.en
echo "fixedEstablishmentAddress.error.field1.length = field1 must be 100 characters or less" >> ../conf/messages.en
echo "fixedEstablishmentAddress.error.field2.length = field2 must be 100 characters or less" >> ../conf/messages.en
echo "fixedEstablishmentAddress.field1.change.hidden = field1" >> ../conf/messages.en
echo "fixedEstablishmentAddress.field2.change.hidden = field2" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryFixedEstablishmentAddressUserAnswersEntry: Arbitrary[(FixedEstablishmentAddressPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[FixedEstablishmentAddressPage.type]";\
    print "        value <- arbitrary[FixedEstablishmentAddress].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryFixedEstablishmentAddressPage: Arbitrary[FixedEstablishmentAddressPage.type] =";\
    print "    Arbitrary(FixedEstablishmentAddressPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryFixedEstablishmentAddress: Arbitrary[FixedEstablishmentAddress] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        field1 <- arbitrary[String]";\
    print "        field2 <- arbitrary[String]";\
    print "      } yield FixedEstablishmentAddress(field1, field2)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(FixedEstablishmentAddressPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration FixedEstablishmentAddress completed"
