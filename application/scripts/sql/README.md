# B-Fabric Database Commands Management

***

### Commands Management Files

* The following files have to be maintained by all developers:

    * `dump-update` : all commands needed to run the code based on the last production database dump
    * `initial-update` : all commands needed to run the code based on the last initial database dump
    * Add all required database commands whenever the code requires the corresponding changes in the database.

* Besides, there are files for specific configurations: `settings-update-xxx`
  