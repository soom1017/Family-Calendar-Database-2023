# Family-Calendar-Database-2023
Family Calendar Project in Database System class, 2023 Fall (ITE2038)

This project contains a family calendar application, implemented in **Java** and connected to a **postgresql DB**.

With this calendar, you can manage your schedule, search events with keywords, and invite family members to an event. You can also set reminders to help you remember when something is coming up soon.
> GUI reference: [Google Calendar](https://calendar.google.com/calendar/u/0/r)

## Overview
- [src/](./src) directory contains Java source code of the calendar.
- You can see execution screens in the [screenshots/](./screenshots) directory.

## How to execute
1. Install postgresql DBMS, and create table with `$ psql -U [user] -d [database] -a -f calendar/data/migration/createTable.sql`.
    - Make sure the name of user, database are same with the information in [ConnectionPool.java](./src/calendar/data/connection/ConnectionPool.java)
2. In the `src/` directory, compile codes with `$ javac calendar/*/*.java`.
3. Make sure you have jdk (up-to-date) in the computer. Execute with `$ java -cp [jdk path] calendar.gui.MainFrame`.
