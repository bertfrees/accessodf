@echo off
echo Spanish...
"C:\Program Files (x86)\Java\jdk1.6.0_17\bin\native2ascii.exe" -encoding utf-8 toolpanel_es_utf8.properties toolpanel_es.properties
echo Now delete "\ufeff" at the beginning of the .properties file.
@pause
exit