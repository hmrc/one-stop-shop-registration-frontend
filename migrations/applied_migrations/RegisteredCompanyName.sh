#!/bin/bash

echo ""
echo "Applying migration RegisteredCompanyName"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /registeredCompanyName                        controllers.RegisteredCompanyNameController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /registeredCompanyName                        controllers.RegisteredCompanyNameController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeRegisteredCompanyName                  controllers.RegisteredCompanyNameController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeRegisteredCompanyName                  controllers.RegisteredCompanyNameController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "registeredCompanyName.title = registeredCompanyName" >> ../conf/messages.en
echo "registeredCompanyName.heading = registeredCompanyName" >> ../conf/messages.en
echo "registeredCompanyName.checkYourAnswersLabel = registeredCompanyName" >> ../conf/messages.en
echo "registeredCompanyName.error.required = Enter registeredCompanyName" >> ../conf/messages.en
echo "registeredCompanyName.error.length = RegisteredCompanyName must be 100 characters or less" >> ../conf/messages.en
echo "registeredCompanyName.change.hidden = RegisteredCompanyName" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryRegisteredCompanyNameUserAnswersEntry: Arbitrary[(RegisteredCompanyNamePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[RegisteredCompanyNamePage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryRegisteredCompanyNamePage: Arbitrary[RegisteredCompanyNamePage.type] =";\
    print "    Arbitrary(RegisteredCompanyNamePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(RegisteredCompanyNamePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration RegisteredCompanyName completed"
