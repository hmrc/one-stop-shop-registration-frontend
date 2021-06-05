#!/bin/bash

echo ""
echo "Applying migration BankDetails"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /bankDetails                        controllers.BankDetailsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /bankDetails                        controllers.BankDetailsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeBankDetails                  controllers.BankDetailsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeBankDetails                  controllers.BankDetailsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "bankDetails.title = bankDetails" >> ../conf/messages.en
echo "bankDetails.heading = bankDetails" >> ../conf/messages.en
echo "bankDetails.accountName = accountName" >> ../conf/messages.en
echo "bankDetails.bic = bic" >> ../conf/messages.en
echo "bankDetails.checkYourAnswersLabel = BankDetails" >> ../conf/messages.en
echo "bankDetails.error.accountName.required = Enter accountName" >> ../conf/messages.en
echo "bankDetails.error.bic.required = Enter bic" >> ../conf/messages.en
echo "bankDetails.error.accountName.length = accountName must be 100 characters or less" >> ../conf/messages.en
echo "bankDetails.error.bic.length = bic must be 8 characters or less" >> ../conf/messages.en
echo "bankDetails.accountName.change.hidden = accountName" >> ../conf/messages.en
echo "bankDetails.bic.change.hidden = bic" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryBankDetailsUserAnswersEntry: Arbitrary[(BankDetailsPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[BankDetailsPage.type]";\
    print "        value <- arbitrary[BankDetails].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryBankDetailsPage: Arbitrary[BankDetailsPage.type] =";\
    print "    Arbitrary(BankDetailsPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryBankDetails: Arbitrary[BankDetails] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        accountName <- arbitrary[String]";\
    print "        bic <- arbitrary[String]";\
    print "      } yield BankDetails(accountName, bic)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(BankDetailsPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration BankDetails completed"
