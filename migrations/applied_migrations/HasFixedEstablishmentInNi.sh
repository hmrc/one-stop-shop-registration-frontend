#!/bin/bash

echo ""
echo "Applying migration HasFixedEstablishmentInNi"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /hasFixedEstablishmentInNi                        controllers.HasFixedEstablishmentInNiController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /hasFixedEstablishmentInNi                        controllers.HasFixedEstablishmentInNiController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeHasFixedEstablishmentInNi                  controllers.HasFixedEstablishmentInNiController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeHasFixedEstablishmentInNi                  controllers.HasFixedEstablishmentInNiController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "hasFixedEstablishmentInNi.title = hasFixedEstablishmentInNi" >> ../conf/messages.en
echo "hasFixedEstablishmentInNi.heading = hasFixedEstablishmentInNi" >> ../conf/messages.en
echo "hasFixedEstablishmentInNi.checkYourAnswersLabel = hasFixedEstablishmentInNi" >> ../conf/messages.en
echo "hasFixedEstablishmentInNi.error.required = Select yes if hasFixedEstablishmentInNi" >> ../conf/messages.en
echo "hasFixedEstablishmentInNi.change.hidden = HasFixedEstablishmentInNi" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryHasFixedEstablishmentInNiUserAnswersEntry: Arbitrary[(HasFixedEstablishmentInNiPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[HasFixedEstablishmentInNiPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryHasFixedEstablishmentInNiPage: Arbitrary[HasFixedEstablishmentInNiPage.type] =";\
    print "    Arbitrary(HasFixedEstablishmentInNiPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(HasFixedEstablishmentInNiPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration HasFixedEstablishmentInNi completed"
