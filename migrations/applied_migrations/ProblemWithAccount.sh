#!/bin/bash

echo ""
echo "Applying migration ProblemWithAccount"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /problemWithAccount                       controllers.ProblemWithAccountController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "problemWithAccount.title = problemWithAccount" >> ../conf/messages.en
echo "problemWithAccount.heading = problemWithAccount" >> ../conf/messages.en

echo "Migration ProblemWithAccount completed"
