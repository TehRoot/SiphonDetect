##Siphon Detect - A java implementation of a slack bot that detects eve online moon siphons
###How to construct the params for the jar + steps
+ Build from maven to get jar file
+ FIRST PARAMETER = Slack Bot Key that you get from completing a bot key auth request to the owner
+ SECOND PARAMETER = EVE Online API Key ID
+ THIRD PARAMETER = EVE Online API Verification Code
+ FOURTH PARAMETER = Username of slack user you would like to ping in addition to the random channel
+ Example construction - " java -jar ApiSiphon.jar "bot-key" "key-id" "verification code" "


###Required API Roles
+ Corp Asset List

###Coming functionality
+ Ability to tell exactly which POS has a siphon on it beyond just the system, helping with multiple poses in a single system
