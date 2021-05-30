#!/bin/bash

echo ""
echo "Applying migration HasFixedEstablishment"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /hasFixedEstablishment                        controllers.HasFixedEstablishmentController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /hasFixedEstablishment                        controllers.HasFixedEstablishmentController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeHasFixedEstablishment                  controllers.HasFixedEstablishmentController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeHasFixedEstablishment                  controllers.HasFixedEstablishmentController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "hasFixedEstablishment.title = hasFixedEstablishment" >> ../conf/messages.en
echo "hasFixedEstablishment.heading = hasFixedEstablishment" >> ../conf/messages.en
echo "hasFixedEstablishment.checkYourAnswersLabel = hasFixedEstablishment" >> ../conf/messages.en
echo "hasFixedEstablishment.error.required = Select yes if hasFixedEstablishment" >> ../conf/messages.en
echo "hasFixedEstablishment.change.hidden = HasFixedEstablishment" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryHasFixedEstablishmentUserAnswersEntry: Arbitrary[(HasFixedEstablishmentPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[HasFixedEstablishmentPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryHasFixedEstablishmentPage: Arbitrary[HasFixedEstablishmentPage.type] =";\
    print "    Arbitrary(HasFixedEstablishmentPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(HasFixedEstablishmentPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration HasFixedEstablishment completed"
