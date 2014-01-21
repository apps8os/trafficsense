A Hello-World for ContextLogger3
========

* Basically following the instructions in Section 5.6 of the master thesis of Nalin Chaudhary (Aalto, 2013).
* Notable changes compared to the default Hello-World:
 * assets/default_config.json
 * AndroidManifest.xml


TOOLS
========

* Archived sqlite3 databases are found in sdcard/com.example.ctxlogtest/mainPipeline/archive
* These databases are encrypted and can be decrypted using funf_analyze/dbdecrypt.py from https://code.google.com/p/funf-open-sensing-framework.samples/
* A modified version of dbdecrypt.py and a driving script are included in db_tools/


TODO
========

1. How to enable more Probes without crashing on Android 4?
2. How to control its sqlite3 archiving behaviour?
   Currently it creates lots of encrypted .db files, some of which are empty.
3. There are more features from funf_analyze remain unexplored.
4. Only 'application event' is demonstrated in this app. The other use of Application Probe is 'application activity'. (Section 5.6.3 of Nalin)
