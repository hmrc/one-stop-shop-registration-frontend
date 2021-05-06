#!/bin/bash

echo ""
echo "Applying migration CannotRegisterForService"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /cannotRegisterForService                        controllers.CannotRegisterForServiceController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /cannotRegisterForService                        controllers.CannotRegisterForServiceController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeCannotRegisterForService                  controllers.CannotRegisterForServiceController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeCannotRegisterForService                  controllers.CannotRegisterForServiceController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "cannotRegisterForService.title = cannotRegisterForService" >> ../conf/messages.en
echo "cannotRegisterForService.heading = cannotRegisterForService" >> ../conf/messages.en
echo "cannotRegisterForService.checkYourAnswersLabel = cannotRegisterForService" >> ../conf/messages.en
echo "cannotRegisterForService.error.required = Enter cannotRegisterForService" >> ../conf/messages.en
echo "cannotRegisterForService.error.length = CannotRegisterForService must be 100 characters or less" >> ../conf/messages.en
echo "cannotRegisterForService.change.hidden = CannotRegisterForService" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryCannotRegisterForServiceUserAnswersEntry: Arbitrary[(CannotRegisterForServicePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[CannotRegisterForServicePage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryCannotRegisterForServicePage: Arbitrary[CannotRegisterForServicePage.type] =";\
    print "    Arbitrary(CannotRegisterForServicePage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(CannotRegisterForServicePage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration CannotRegisterForService completed"
