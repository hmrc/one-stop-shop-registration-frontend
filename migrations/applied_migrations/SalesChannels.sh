#!/bin/bash

echo ""
echo "Applying migration SalesChannels"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /salesChannels                        controllers.SalesChannelsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /salesChannels                        controllers.SalesChannelsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeSalesChannels                  controllers.SalesChannelsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeSalesChannels                  controllers.SalesChannelsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "salesChannels.title = salesChannels" >> ../conf/messages.en
echo "salesChannels.heading = salesChannels" >> ../conf/messages.en
echo "salesChannels.option1 = Option 1" >> ../conf/messages.en
echo "salesChannels.option2 = Option 2" >> ../conf/messages.en
echo "salesChannels.checkYourAnswersLabel = salesChannels" >> ../conf/messages.en
echo "salesChannels.error.required = Select salesChannels" >> ../conf/messages.en
echo "salesChannels.change.hidden = SalesChannels" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrarySalesChannelsUserAnswersEntry: Arbitrary[(SalesChannelsPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[SalesChannelsPage.type]";\
    print "        value <- arbitrary[SalesChannels].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrarySalesChannelsPage: Arbitrary[SalesChannelsPage.type] =";\
    print "    Arbitrary(SalesChannelsPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitrarySalesChannels: Arbitrary[SalesChannels] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(SalesChannels.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(SalesChannelsPage.type, JsValue)] ::";\
    next }1' ../test/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test/generators/UserAnswersGenerator.scala

echo "Migration SalesChannels completed"
