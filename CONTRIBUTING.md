# Contributing to Discreet Launcher

Hello,

First of all, thank you very much for your interest in this project!

This document provides guidelines about the various ways to contribute:

1. [Reporting a bug](#reporting-a-bug)
2. [Asking for a new feature or improvement](#asking-for-a-new-feature-or-improvement)
3. [Contributing to code](#contributing-to-code)

For now, translations in other languages than French or English are not planned.  
You can read more details about the [reason](#why-other-translations-are-not-planned) at the bottom of this document.

Best regards.

---

## Reporting a bug

When you discover something which is not working as expected, it is always appreciated that you [create a ticket](https://github.com/falzonv/discreet-launcher/issues) to report it.  

Here are some guidelines to make it easier for me to solve your issue:

- Before creating a new ticket, **check if this bug has already been reported by someone else**.
If it has, feel free to add a comment to the existing ticket to share any new information that may help to solve it.
- **Give a description as clear as possible** of the bug you are facing, you can also include screenshots or videos if you think this will **make the issue easier to reproduce**.
- Please **indicate the version of Discreet Launcher** that you are currently using, you can find this information at the end of the "Settings / Help" page (look for something like "v1.0.0").
- Sometimes it can be useful to mention your Android version if you know it.

It should also be noted that **the F-Droid build and publishing process is automatic and usually take 4 to 5 days to complete**.
Consequently, when a bug is corrected, the new release will not be available immediately in F-Droid. You can check the [releases](https://github.com/falzonv/discreet-launcher/releases) section to see what has not been detected yet by F-Droid.

Finally, **tickets can be created in English or in French** according to your preference (if you start in French the discussion will continue in French, otherwise the default language will be English).


## Asking for a new feature or improvement

New ideas are always welcome, so if you think about something that could improve the application, feel free to suggest it by [creating a ticket](https://github.com/falzonv/discreet-launcher/issues).

Here are some guidelines about suggesting improvements:

- Before creating a new ticket, **check if this feature or improvement has already been requested by someone else**.
If it has, feel free to add a comment to the existing ticket to share any new information about this feature.
- **Give a description as clear as possible** of the feature or improvement you would like to see implemented, you can also include screenshots or videos if you think this will **make the request easier to understand**.
- Be patient, remember that I do this on my spare time ;-)

Finally, there are **some things that will not be implemented** for now because the general ideas of this launcher are to provide a distraction-free home screen, to not ask for unnecessary permissions and to stay stable and functional (that is, not overloaded with options that are difficult to maintain and create unexpected bugs impossible to reproduce).  
*=> Examples of features not planned: widgets support, manual sort in the complete applications list, folders, applications renaming.*


## Contributing to code

Here we are touching a difficult part :-)  
To make it simple, I am not against code contributions but I am very perfectionnist.

If there is a change you would like to make (for example if you forked this project to implement your own features and think that some of them could be integrated in the root project), it might be good that you start by opening a ticket to give me a bit of context and explain what you would like to integrate.

Regarding style guidelines, if you look at the source code, you will quickly notice that:

- There are comments everywhere: all the methods and classes have a Javadoc comment, most blocks of lines have a comment line right above them which act as a sort of heading for the block, etc.
- In addition, all these comments are complete and clear sentences in, hopefully, correct English.
- Variables, objects and methods names are also quite clear and self describing.
For example, the object which represent the applications list is called <code>applicationsList</code>, not <code>app_lst</code> or just <code>list</code>.
- Whenever possible, I try to make the indentation and general formatting elegant and symetrical.
For example, the opening <code>{</code> of a class or method is most of the time on the next line at the same level than the closing <code>}</code> character.
- There is a space before the ";" character ending each line.
This is because in France we put spaces before and after this character.
All the comments are in English to allow more people to read them but, being French myself, I decided to keep this space before the ";".
- Commits are using the "single line of less than 50 characters" format and could complete the sentence "If this commit is applied, it will...".

As long as your proposal fits in this general pattern (looking at the current source code files should quickly show you if this is the case or not) and is adding value to the project, it will probably be happily accepted.


## Why other translations are not planned?

The reason is that I want to manage only languages that I can speak myself and, for now, this includes only French and English.
Indeed, in my opinion, supporting a new language is more than just translating the "strings.xml" file and the descriptions (and taking new screenshots).

Creating the initial files for a new language is certainly not easy but this is only the first step of the process.
Once the new language is offered, all these translations ("strings.xml", descriptions, screenshots) must be kept updated from release to release.
If I cannot do this myself, it means that before publishing each realease I have to contact all the persons who wrote the translations to make the updates (even when this is minor, most releases are changing something about the texts), and possibly chase them if they are not responding.
To be honest, I prefer to focus on improving the application and solving bugs than managing all this process.

Users should also be able to report their issues in their own language if this language is supported by the application.
Even if English is used most of the time on the Internet, if someone was opening an issue in French, I would reply to him/her in French and the discussion would continue in French.  
Consequently, to really support another language, the translator would be needed here as well and would have to play a "(wo)man-in-the-middle" role between the user and me (to explain me the issue, forward my questions to the user, translate the answers and so on).

Finally, what happens if, after one or two years, one of the translators becomes unavailable for some reasons?  
Then I am stuck when a new release should be published and need translations update or when an issue arrives in a language that I know nothing about.
Possible solutions would be to find a new translator (but where? and what if he asked to be paid each time since the project itself does not bring any money?) or drop the translation in the next release (but what about the users?).

These are the reasons for which I do not plan to have the application translated in other languages than English and French for now.