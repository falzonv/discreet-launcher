
# Contributing to Discreet Launcher

Hello,

First of all, thank you very much for your interest in this project!

There are several ways to contribute:


- [Report a bug](#report-a-bug)
- [Suggest a new feature or improvement](#suggest-a-new-feature-or-improvement)
- [Create and maintain a translation](#create-and-maintain-a-translation)

You can read more details about each of them below.  
Globally, be patient and remember that I do all of this on my spare time during evenings and weekends ;-)

*I like to improve my code following feedback but have to admit that doing code review is not my cup of tea.  
For now I prefer to manage all the code myself as the project is small enough to allow it. This is what personally motivates me in maintaining this project.  
Consequently, don't be surprised if pull requests related to code are closed.  
Instead, feel free to suggest improvements in the "Issues" tab so we can discuss them together.*

Best regards.

---

## Report a bug

When something is not working as expected, I will always appreciate that you report it.

Here are some guidelines to make it easier for me to solve your issue:

- Before creating a new ticket, **please check if this bug has already been reported** and feel free to add comments to an existing ticket. You may have new information that could help to solve the issue.
- **Give a description as clear as possible** of the bug you are facing, you can also include screenshots or videos if you think this will **make the issue easier to reproduce**.
- Please **indicate the version of Discreet Launcher** that you are currently using, you can find this information at the end of the "Settings / Help" page (look for something like "v1.0.0") **and your Android version** if possible.


## Suggest a new feature or improvement

If you think about something that could improve Discreet Launcher, feel free to suggest it!

Here are some guidelines about suggesting improvements:

- Before creating a new ticket, **please check if this feature or improvement has already been suggested** and feel free to add comments to an existing ticket to share your point of view about this feature.
- **Give a description as clear as possible** of the feature or improvement you would like to see implemented, you can also include screenshots or videos if you think this will **make the request easier to understand**.
- Please note that **some things will not be implemented** (ex: widgets support) because the goals of this launcher are to provide a distraction-free home screen, to use as few permissions as possible while staying light, stable and easy to maintain.


## Create and maintain a translation

If you are willing to create and maintain a new translation over time, feel free to contact me using the "Issues" or "Pull requests" tab.

Here are some guidelines about translations:

- All the strings of Discreet Launcher are located in the folder [app/src/main/res/values](https://github.com/falzonv/discreet-launcher/tree/main/app/src/main/res/values) and distributed over files `strings.xml`, `strings_settings.xml` and `strings_help.xml`.
- The changelog is not intended to be translated, the string *changelog_folder* has to contain the value `changelog-en`.
- At the end of `strings.xml`, you will find a string named *translation_credit* which will be displayed below the *About* section and should contain a translation of the following text:
> [Locale (ex: Russian)] translation provided by [first and last names].  
> If you need to contact Vincent (developer), please do so in English or French.

Finally, I should also warn you that create and maintain a translation over time can be time-consuming because almost every release introduces a few new strings or modify some of the existing ones.
