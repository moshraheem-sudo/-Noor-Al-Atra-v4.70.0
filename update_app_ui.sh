#!/bin/bash
awk '
NR==1383 {
    print "            // Collapsible Prayer Times Card (replacing Suggestions Banner)"
    print "            item {"
    print "                CollapsibleHomePrayerCard("
    print "                    prayerData = prayerData,"
    print "                    nextPrayer = nextPrayer,"
    print "                    liveTime = liveTime,"
    print "                    selectedCity = selectedCity,"
    print "                    currentLanguage = appLanguage,"
    print "                    onNavigateToPrayerTimes = { navController.navigate(\"prayer_times\") }"
    print "                )"
    print "            }"
    skip=1
}
NR==1558 {
    skip=0
    next
}
!skip {
    print $0
}
' app/src/main/java/com/example/ui/AppUI.kt > app/src/main/java/com/example/ui/AppUI.kt.tmp
mv app/src/main/java/com/example/ui/AppUI.kt.tmp app/src/main/java/com/example/ui/AppUI.kt
