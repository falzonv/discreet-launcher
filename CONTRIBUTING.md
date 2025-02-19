
# Contributing to Discreet Launcher

Hello,

First of all, thank you very much for your interest in this project!

As I explained in ticket [#314](https://github.com/falzonv/discreet-launcher/issues/314), no more new features or options will be implemented in Discreet Launcher.

However, there are still ways to contribute:

- [Report a bug](#report-a-bug)
- [Create or update a translation](#create-or-update-a-translation)

You can read more details about each of them below.  
Globally, be patient and remember that I do all of this on my spare time during evenings and weekends ;-)

*I like to improve my code following feedback but have to admit that doing code review is not my cup of tea.  
Consequently, any pull request related to code will be closed.*

Best regards.

---

## Report a bug

When something is not working as expected, I will always appreciate that you report it.

Here are some guidelines to make it easier for me to solve your issue:

- Before creating a new ticket, **please check if this bug has already been reported** and feel free to add comments to an existing ticket. You may have new information that could help to solve the issue.
- **Give a description as clear as possible** of the bug you are facing, you can also include screenshots or videos if you think this will **make the issue easier to reproduce**.
- Please **indicate the version of Discreet Launcher** that you are currently using, you can find this information on top of the "About" dialog (look for something like "v1.0.0") **and your Android version** if possible.


## Create or update a translation

The list of existing translations can be found in ticket [#80](https://github.com/falzonv/discreet-launcher/issues/80#issue-932460225), which I also use to notify translators when updates are needed.

If you are willing to create a new translation, feel free to contact me using the "Issues" or "Pull requests" tab.  
Here is the list of things that should be prepared for a new translation:

- The file [app/src/main/res/values/strings.xml](https://github.com/falzonv/discreet-launcher/blob/main/app/src/main/res/values/strings.xml) has to be copied into a new folder `app/src/main/res/values-<code>/` where `<code>` is the 2-letter locale code for your translation ([see the list of available codes](https://gitlab.com/fdroid/fdroidclient/-/tree/master/app/src/main/res)).
- The files below are for F-Droid metadata and have to be copied into a new folder `fastlane/metadata/android/<code>/` where `<code>` is the 2-letter locale code for your translation ([see the list of available codes](https://gitlab.com/fdroid/fdroidclient/-/tree/master/metadata)).
    - [fastlane/metadata/android/en-US/title.txt](https://github.com/falzonv/discreet-launcher/blob/main/fastlane/metadata/android/en-US/title.txt)
    - [fastlane/metadata/android/en-US/short_description.txt](https://github.com/falzonv/discreet-launcher/blob/main/fastlane/metadata/android/en-US/short_description.txt) (limited to 80 characters)
    - [fastlane/metadata/android/en-US/full_description.txt](https://github.com/falzonv/discreet-launcher/blob/main/fastlane/metadata/android/en-US/full_description.txt)
- In the Pull Request description, provide me the texts for the [banner](https://github.com/falzonv/discreet-launcher/blob/main/fastlane/metadata/android/en-US/images/featureGraphic.png) and [screenshots](https://github.com/falzonv/discreet-launcher/blob/main/docs/assets/img/screenshots_total_en.jpg):
```
Banner:
  Discreet Launcher
for a distraction-free
     home screen


Screenshot 1:
      Enjoy a clean home screen,
while accessing everything in an instant!


Screenshot 2:
Quick access to your favorite applications...
               (swipe down)


Screenshot 3:
...or to any other application!
         (swipe up)


Screenshot 4:
Access favorites from anywhere,
  thanks to the notification!
```

