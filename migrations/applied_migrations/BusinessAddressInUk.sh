#!/bin/bash

echo ""
echo "Applying migration BusinessAddressInUk"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /businessAddressInUk                        controllers.BusinessAddressInUkController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /businessAddressInUk                        controllers.BusinessAddressInUkController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeBusinessAddressInUk                  controllers.BusinessAddressInUkController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeBusinessAddressInUk                  controllers.BusinessAddressInUkController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "businessAddressInUk.title = businessAddressInUk" >> ../conf/messages.en
echo "businessAddressInUk.heading = businessAddressInUk" >> ../conf/messages.en
echo "businessAddressInUk.checkYourAnswersLabel = businessAddressInUk" >> ../conf/messages.en
echo "businessAddressInUk.error.required = Select yes if businessAddressInUk" >> ../conf/messages.en
echo "businessAddressInUk.change.hidden = BusinessAddressInUk" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryBusinessAddressInUkUserAnswersEntry: Arbitrary[(BusinessAddressInUkPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[BusinessAddressInUkPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryBusinessAddressInUkPage: Arbitrary[BusinessAddressInUkPage.type] =";\
    print "    Arbitrary(BusinessAddressInUkPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(BusinessAddressInUkPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration BusinessAddressInUk completed"
