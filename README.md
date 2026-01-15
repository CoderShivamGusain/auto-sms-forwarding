# Auto SMS Forwarding 

  Auto SMS Forwarding is an Android application that automatically forwards incoming SMS messages based on configurable rules such as sender number, message body keywords, and blacklist filters.
  It is designed to run efficiently in the background with minimal battery usage.
  
  This app was built to solve a real client requirement where specific types of SMS messages must be forwarded automatically without manual intervention.




Key Features

  1. Automatic SMS forwarding in the background

  2. Keyword-based message filtering

  3. Sender-based filtering (specific number or all senders)
  
  4. Blacklisted keywords support
  
  5. Multiple forwarding recipients supported
  
  6. Lightweight & battery-optimized background service
  
  7. Works on older Android versions (legacy-compatible structure)
  



How It Works

  The app listens for incoming SMS messages and applies the following rule-based logic:
  
    1. Receiving From
  
      Accepts messages from all senders
  
      Specific phone number → Accepts messages only from that number
  
    2. Body Keywords
  
      If any keyword listed here is found in the SMS body, the message is eligible for forwarding
      Example:
      otp, verification code
  
    3. Blacklist Keywords
  
      If any blacklist keyword is found in the SMS body, the message is NOT forwarded
  
      Example:
      jio, airtel
  
    4. Forwarding To
  
      One or more phone numbers
  
      The message is automatically forwarded to these numbers when all conditions are satisfied




App UI Overview

  The configuration screen allows users to define forwarding rules easily:
  
  Forwarding To → Destination phone number(s)
  
  Receiving From → Sender filter (* for all or a specific number)
  
  Body Keywords → Keywords required to forward a message
  
  Blacklist → Keywords that block forwarding
  
  The app runs silently in the background once enabled.




Project Structure
    app/
     ├── data_model/
     │   ├── Preferences.java
     │   └── RecipientListItem.java
     ├── event/
     │   ├── SMSReceiver.java
     │   └── SMSSender.java
     ├── security_model/
     │   └── RuntimePermissions.java
     └── ui/
         └── RecipientListActivity.java


  SMSReceiver → Listens to incoming messages
  
  SMSSender → Handles SMS forwarding
  
  Preferences → Stores rules and settings
  
  RuntimePermissions → Manages Android SMS permissions




Permissions Used

  RECEIVE_SMS
  
  READ_SMS
  
  SEND_SMS
  
  These permissions are required for the core functionality of the app.




Tech Stack

  Language: Java
  
  Platform: Android
  
  Build System: Gradle
  
  Architecture: Event-based (BroadcastReceiver + Services)
  
  Minimum SDK: Legacy-compatible




Example Use Case

  A business receives OTP or transaction messages on a central phone number and needs them forwarded automatically to customers or internal systems without manual forwarding.
  
  This app solves that problem reliably and efficiently.




License & Credits

  This project is licensed under the GNU General Public License v2.0 (GPL-2.0).
  
  This project was initially inspired by an existing open-source SMS forwarding repository.
  The current version includes significant architectural changes, automation, UI updates, filtering logic, and additional features, making it a distinct and extended implementation.
  
  Full license text is available in the LICENSE file.




Disclaimer
  
  This app is intended for educational and internal automation purposes only.
  Usage must comply with local laws and telecom regulations regarding SMS handling and forwarding.




Author

Shivam Gusain (CoderShivamGusain)
Backend Developer | Android Developer | Automation & Background Services
GitHub: https://github.com/CoderShivamGusain
