[VERSION FRANÇAISE EN DESSOUS](#contribuer-à-lanceur-discret)

# Contributing to Discreet Launcher

Hello,

First of all, thank you very much for your interest in this project!

This document provides guidelines about the various ways to contribute:

1. [Reporting a bug](#reporting-a-bug)
2. [Suggesting a new feature or improvement](#suggesting-a-new-feature-or-improvement)
3. [Contributing to code](#contributing-to-code)

For now, translations in other languages than English or French are not planned.  
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


## Suggesting a new feature or improvement

New ideas are always welcome, so if you think about something that could improve the application, feel free to suggest it by [creating a ticket](https://github.com/falzonv/discreet-launcher/issues).

Here are some guidelines about suggesting improvements:

- Before creating a new ticket, **check if this feature or improvement has already been suggested by someone else**.
If it has, feel free to add a comment to the existing ticket to share any new information about this feature.
- **Give a description as clear as possible** of the feature or improvement you would like to see implemented, you can also include screenshots or videos if you think this will **make the request easier to understand**.
- Be patient, remember that I do this on my spare time ;-)

There are **some things that will not be implemented** for now because the general ideas of this launcher are to provide a distraction-free home screen, to not ask for unnecessary permissions and to stay stable and functional (that is, not overloaded with options that are difficult to maintain and create unexpected bugs impossible to reproduce).  
*=> Some features not planned: widgets support, manual sort in the complete applications list, folders, applications renaming.*

Finally, **tickets can be created in English or in French** according to your preference (if you start in French the discussion will continue in French, otherwise the default language will be English).

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
For example, the opening "{" of a class or method is most of the time on the next line at the same level than the closing "}" character.
- There is a space before the semicolon (";") at the end of each line.
This is because in France we put spaces before and after this character.
All the comments are in English to allow more people to read them but, being French myself, I decided to keep this space before the ";".
- Commits are using the "single line of less than 50 characters" format and could complete the sentence "If this commit is applied, it will...".

As long as your proposal fits in this general pattern (looking at the current source code files should quickly show you if this is the case or not) and is adding value to the project, it should be be happily accepted.


## Why other translations are not planned?

The reason is that I want to manage only languages that I can speak myself and, for now, this includes only French and English.
Indeed, in my opinion, supporting a new language is more than just translating the "strings.xml" file and the descriptions (and taking new screenshots).

Creating the initial files for a new language is certainly not easy but this is only the first step of the process.
Once the new language is offered, all these translations ("strings.xml", descriptions, screenshots) must be kept updated from release to release.
If I cannot do this myself, it means that before publishing each realease I have to contact all the persons who wrote the translations to make the updates (even when this is minor, most releases are changing something about the texts), and possibly chase them if they are not responding.
To be honest, I prefer to focus on improving the application and solving bugs than managing all this process.

Users should also be able to report their issues in their own language if this language is supported by the application.
Even if English is used most of the time on the Internet, if someone was opening a ticket in French, I would reply to him/her in French and the discussion would continue in French.  
Consequently, to really support another language, the translator would be needed here as well and would have to play a "(wo)man-in-the-middle" role between the user and me (to explain me the issue, forward my questions to the user, translate the answers and so on).

Finally, what happens if, after one or two years, one of the translators becomes unavailable for some reasons?  
Then I am stuck when a new release should be published and need translations update or when an issue arrives in a language that I know nothing about.
Possible solutions would be to find a new translator (but where? and what if he asked to be paid each time since the project itself does not bring any money?) or drop the translation in the next release (but what about the users?).

These are the reasons for which I do not plan to have the application translated in other languages than English and French for now.

---

[ENGLISH VERSION ABOVE](#contributing-to-discreet-launcher)

# Contribuer à Lanceur Discret

Bonjour,

Tout d'abord, merci beaucoup de votre intérêt pour ce projet !

Ce document fournit des indications sur les diverses façons de contribuer :

1. [Signaler un bug](#signaler-un-bug)
2. [Suggérer une nouvelle fonctionnalité ou une amélioration](#suggérer-une-nouvelle-fonctionnalité-ou-une-amélioration)
3. [Contribuer au code](#contribuer-au-code)

Pour l'instant, les traductions dans d'autres langues que le français et l'anglais ne sont pas prévues.  
Vous pouvez lire plus de détails sur la [raison](#pourquoi-dautres-traductions-ne-sont-pas-prévues-) à la fin de ce document.

Cordialement.

---

## Signaler un bug

Quand vous découvrez quelque chose qui ne fonctionne pas comme prévu, le signaler en [créant un ticket](https://github.com/falzonv/discreet-launcher/issues) est toujours apprécié.

Voici quelques recommandations pour rendre votre souci plus facile à résoudre :

- Avant de créer un ticket, **vérifiez si ce bug a déjà été signalé par quelqu'un d'autre**.
Si c'est le cas, n'hésitez pas à ajouter un commentaire dans le ticket existant pour partager toute nouvelle information qui pourrait aider à résoudre le problème.
- **Donnez une description aussi claire que possible** du problème que vous rencontrez, vous pouvez aussi ajouter des captures d'écran ou des vidéos si vous pensez que cela **rendra le bug plus facile à reproduire**.
- Veuillez **indiquer la version de Lanceur Discret** que vous utilisez actuellement, vous pouvez trouver cette information à la fin de la page "Paramètres / Aide" (cherchez quelque chose qui ressemble à "v1.0.0").
- Parfois il peut être utile de mentionner votre version d'Android si vous la connaissez.

Il est à noter que **le processus de compilation et de publication par F-Droid est automatique et prend généralement 4 à 5 jours**.
Par conséquent, quand un bug est corrigé, la nouvelle version ne sera pas disponible immédiatement dans F-Droid. Vous pouvez vérifier la page nommée "[Releases](https://github.com/falzonv/discreet-launcher/releases)" pour voir ce qui n'a pas encore été détecté par F-Droid (en anglais).

Enfin, **les tickets peuvent être créés en français ou en anglais** selon votre préférence (si vous commencez en français, la conversation continuera en français, sinon la langue par défaut sera l'anglais).


## Suggérer une nouvelle fonctionnalité ou une amélioration

Les nouvelles idées sont toujours les bienvenues, alors si vous pensez à quelque chose qui pourrait améliorer l'application, n'hésitez pas à le suggérer en [créant un ticket](https://github.com/falzonv/discreet-launcher/issues).

Voici quelques recommandations à ce sujet :

- Avant de créer un ticket, **vérifiez si cette fonctionalité ou amélioration a déjà été suggérée par quelqu'un d'autre**.
Si c'est le cas, n'hésitez pas à ajouter un commentaire dans le ticket existant pour partager toute nouvelle information à propos de cette fonctionnalité.
- **Donnez une description aussi claire que possible** de la fonctionnalité ou de l'amélioration que vous voudriez voir dévelopée, vous pouvez aussi ajouter des captures d'écran ou des vidéos si vous pensez que cela **rendra la demande plus facile à comprendre**.
- Soyez patient(e), n'oubliez pas que je fais cela sur mon temps libre ;-)

Il y a **plusieurs choses qui ne seront pas mises en place** pour l'instant car les objectifs généraux de ce lanceur sont de fournir un écran libre de distractions, de ne pas avoir besoin de permissions inutiles et rester stable et fonctionnel (c'est à dire, pas surchargé avec des options diffiiles à maintenir qui créent des bugs impossibles à reproduire).  
*=> Parmi ce qui n'est pas prévu : support des widgets, tri manuel dans la liste d'applications complète, dossiers, renommage des applications.*

Enfin, **les tickets peuvent être créés en français ou en anglais** selon votre préférence (si vous commencez en français, la conversation continuera en français, sinon la langue par défaut sera l'anglais).

## Contribuer au code

Là on touche un sujet sensible :-)  
Pour dire les choses simplement, je ne suis pas contre les contributions au code mais je suis très perfectionniste.

S'il y a un changement que vous voudriez faire (par exemple si vous avez cloné le projet pour vous faire vos propres fonctionnalités et que vous pensez que certaines pour être intégrées dans le projet d'origine), cela serait bien de commencer par ouvrir un ticket pour me donner un peu de contexte et expliquer ce que vous voulez intégrer.

Concernant les règles de mise en forme, si vous regardez le code source, vous verrez rapidement que :

- Il y a des commentaires partout : toutes les méthodes et classes ont un commentaire Javadoc, la plupart des blocs de lignes ont une ligne de commentaire au dessus qui est en quelque sorte le titre du bloc, etc.
- De plus, tous ces commentaires sont des phrases complètes et claires (en anglais pour plus d'universalité).
- Les noms des variables, objets et méthodes sont aussi clairs et bien descriptifs.
Par exemple, l'objet qui représente la liste d'applications s'appelle <code>applicationsList</code>, pas <code>app_lst</code> ou juste <code>list</code>.
- Dans la mesure du possible, j'essaye de rendre l'indentation et la mise en forme élégantes et symétriques.
Par exemple, le "{" ouvrant d'une classe ou méthode est la plupart du temps sur la ligne suivante au même niveau que le symbole "}" fermant.
- Il y a un espace avant le point-virgule (";") à la fin de chaque ligne.
C'est parce qu'en France nous mettons des espaces avant et après ce caractère.
Tous les commentaires sont en anglais pour permettre à plus de personnes de les lire, mais étant moi-même français, j'ai décidé de garder cet espace avant le ";".
- Les commits utilisent le format "une ligne de moins de 50 caractères" et pourraient compléter la phrase "Si ce commit est appliqué, il va...".

Dès lors que votre proposition correspond à ce format général (en regardant les fichiers du code source actuel, vous verrez rapidement si c'est le cas ou non) et qu'elle ajoute de la valeur au projet, elle devrait être joyeusement acceptée.


## Pourquoi d'autres traductions ne sont pas prévues ?

La raison est que je ne veux gérer que des langues que je peux parler moi-même et, pour l'instant, cela n'inclut que le français et l'anglais.
En effet, je pense que supporter une nouvelle langue signifie plus que juste traduire le fichier "strings.xml" et les descriptions (et prendre de nouvelles captures d'écran).

Créer les fichiers initiaux dans une nouvelle langue n'est certes pas facile mais ce n'est que la première étape du processus.
Une fois la nouvelle langue proposée, toutes les traductions ("strings.xml", descriptions, captures d'écran) doivent être maintenues à jour d'une version à une autre.
Si je ne peux pas le faire moi-même, cela veut dire qu'avant de publier chaque version je dois contacter toutes les personnes qui ont écrit les traductions pour qu'ils fassent des mises à jour (même les versions mineures changent souvent quelques petites choses dans les textes), et parfois les relancer s'ils ne répondent pas.
Pour être honnête, je préfère me concentrer sur l'amélioration de l'application et la correction de bugs plutôt que gérer tout ce processus.

Si une langue est supportée par l'application, les utilisateurs devraient aussi pouvoir signaler des soucis dans cette langue.
Même si l'anglais est utilisé la plupart du temps sur Internet, si quelqu'un ouvrait un ticket en français, je lui répondrais en français et la conversation se poursuivrait en français.  
Par conséquent, pour vraiment supporter une autre langue, il y aurait aussi besoin d'un traducteur ici pour faire l'intermédiaire entre l'utilisateur et moi (m'expliquer le problème, transférer mes questions à l'utilisateur, traduire ses réponses et ainsi de suite).

Enfin, que se passe-t-il si, après un an ou deux, l'un des traducteurs devient indisponible pour diverses raisons ?  
Dans ce cas je suis coincé quand une nouvelle version doit être publiée et nécessite des traductions, ou quand un ticket arrive dans un language dont je ne connais rien.
Des solutions possibles serait de trouver un nouveau traducteur (mais où ? et s'il demande à être payé à chaque fois alors que le projet ne rapporte rien par lui-même ?) ou d'abandonner cette langue dans la prochaine version (mais comment vont réagir ses utilisateurs ?).

Ce sont les raisons pour lesquelles je ne prévois pas de traduire l'application dans d'autres langues que le français et l'anglais pour l'instant.

