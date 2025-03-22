Spell4Wiki Contribution Guidelines
==================================

PLEASE READ THESE GUIDELINES CAREFULLY BEFORE ANY CONTRIBUTION!

## How to Use Spell4Wiki

Watch the tutorial videos below and make your contribution.

| Language        | Video Link     | Creator |
| :------------- | :-------------: | :------------- |
| Tamil     | [Play Video](https://youtu.be/4y5I1sUW1ys) | [Manimaran](https://commons.wikimedia.org/wiki/User:Manimaran96) |
| English   | [Introduction](https://youtu.be/IMku3FL7s3I)<br/>[Play Video](https://youtu.be/Fu4kQcv04kA) | [Ganesh](https://commons.wikimedia.org/wiki/User:Libreaim)


You are always welcome to make video for your language. then share link or video file by <a href="#communication">communication mediums</a> 


## Spell4Wiki Improvements

### Add your Wiktionary language into Spell4Wiki

We need some basic information for adding your contribution language into Spell4Wiki.
So, please fill [this form](https://docs.google.com/forms/d/e/1FAIpQLSciqHNw1ZtH1kp2zz2DlKFmIbRZw2K7fhcJdxYNAr6TiAsN2A/viewform) or raise [issue here](https://github.com/manimaran96/Spell4Wiki/issues/new?assignees=manimaran96&labels=add+new+language&template=add_language_request.md&title=).

**Provide details** - [Fill this form](https://docs.google.com/forms/d/e/1FAIpQLSciqHNw1ZtH1kp2zz2DlKFmIbRZw2K7fhcJdxYNAr6TiAsN2A/viewform) or [Raise issue](https://github.com/manimaran96/Spell4Wiki/issues/new?assignees=manimaran96&labels=add+new+language&template=add_language_request.md&title=)<br/><br/>
**Existing language details** - [Form responses](https://docs.google.com/spreadsheets/d/14c8s7UN-8eCDA-qU73l_yXhLdxdjYfuzubAl9RrehuQ/edit?usp=sharing) & [Github issues](https://github.com/manimaran96/Spell4Wiki/issues?q=label:"add+new+language")


Needed Data
 
| Field        | Data           |
| ------------- |:-------------:|
| Language name     | Tamil |  |
| Language code      | ta      |
| Language direction | LTR     |
| Local name | தமிழ் |
| Category name<br/>in Wiktionary | பகுப்பு:தமிழ்-ஒலிக்கோப்புகளில்லை |
| Category name<br/>in Commons | Files uploaded by spell4wiki in ta,<br/>Tamil pronunciation of the words with Tamil script |

Here,
* Language name -> Like Tamil, English, ...etc
* Language code -> Like ta, en, fr, ... etc
* Language direction -> Like English - LTR(LTR - Left To Right), Arabic - RTL(LTR - Right To Left)
* Local name -> Like தமிழ், English, Français...etc
* Category name in Wiktionary -> Better to mention in category of words without have audio or mention any category to be need audio. <br/>
Ex : <br/>
பகுப்பு:தமிழ்-ஒலிக்கோப்புகளில்லை - https://ta.wiktionary.org/wiki/பகுப்பு:தமிழ்-ஒலிக்கோப்புகளில்லை, <br/>
Category:en:Fruits - https://en.wiktionary.org/wiki/Category:en:Fruits, <br/>
Category:Apple cultivars - https://en.wiktionary.org/wiki/Category:Apple_cultivars
* Category name in Commons -> All audio files are uploaded under this commons category. More category then separate with coma(,). 


### Create Category for your language in Commons

In [Files uploaded by spell4wiki](https://commons.wikimedia.org/wiki/Category:Files_uploaded_by_spell4wiki) category contains all the files uploaded using Spell4Wiki.
So, create one subcategory for your language specific(```Files uploaded by spell4wiki in language-code```). Like ```Files uploaded by spell4wiki in ta```.

Ex : For Tamil - https://commons.wikimedia.org/wiki/Category:Files_uploaded_by_spell4wiki_in_ta
 

## Issue reporting/feature requests

* Search the [existing issues](https://github.com/manimaran96/Spell4Wiki/issues) first to make sure your issue/feature
hasn't been reported/requested before.
* Check whether your issue/feature is already fixed/implemented.
* Check if the issue still exists in the latest release/beta version.
* If you are an Android developer, you are always welcome to fix an issue or implement a feature yourself. PRs welcome!
* We use English & Tamil for development. Issues in other languages will be closed and ignored.
* Please only add *one* issue at a time. Do not put multiple issues into one thread.

## Crash reporting

* We collect the app crash details in app cache. So, You will see the dialog for sending the crash information to developer. Whenever opening the app after crash happened. 
* Please send the crash information to developer via email. 
* You'll see exactly what is sent, the system is 100% transparent.


## Translation

* Follow the [instructions](#code-contribution) and [steps to start changes](#getting-started).
* Create new branch. Ex : ```language-ta``` 
* Checkout this new branch.
* Then, Create ```values-language-code``` folder inside the ```app/src/main/res/``` folder. (Ex : ```values-ta```, ```values-hi```, ... etc.)
* Create ```strings.xml``` file inside the ```values-language-code``` folder.
* Copy all the English text from following file```app/src/main/res/values/strings.xml``` path - [link](https://github.com/manimaran96/Spell4Wiki/blob/master/app/src/main/res/values/strings.xml)
* Translate all the strings into your language.
* Finally do commit and give the PR.

Note,
* Don't translate ```Don't translate``` part and ```translatable="false``` strings.
* Avoid to translate feature names. Like Spell4Wiki, Spell4WordList, etc.

If you have any questions/doubt, don't hesitate to contact by <a href="#communication">communication</a> details.

## Code contribution

* Do not bring non-free software (e.g. binary blobs) into the project. Also, make sure you do not introduce Google
  libraries.
* Make changes on a separate branch with a meaningful name, not on the master neither dev branch. This is commonly known as *feature branch workflow*. You
  may then send your changes as a pull request (PR) on GitHub.
* When submitting changes, you confirm that your code is licensed under the terms of the
  [GNU General Public License v3](https://www.gnu.org/licenses/gpl-3.0.html).
* Please test (compile and run) your code before you submit changes! Ideally, provide test feedback in the PR
  description. Untested code will **not** be merged!
* Make sure your PR is up-to-date with the rest of the code.

### Getting Started
1. Fork the repository on the GitHub page by clicking the Fork button. This makes a fork of the project under your GitHub account.
2. Clone your fork to your machine. 
```
git clone https://github.com/<Your_Username>/Spell4Wiki
```
3. Create a new branch named after your change. 
```
git checkout -b your-branch-name
``` 
Here, ```checkout``` switches to a branch, ```-b``` specifies that the branch is a new one.

4. Any time you get a good chunk of work done it's good to make a commit. You can either uses Android Studio's built-in UI for doing this or running the commands:
```
git add .
git commit -m "Describe the changes in this commit here."
```
5. Once your all changes done then submit your changes. 
6. Make sure your branch is up-to-date with the ```master``` branch. Run:
```
git fetch
git rebase origin/master
```
It may refuse to start the rebase if there's changes that haven't been committed, so make sure you've added and committed everything. 
If there were changes on master to any of the parts of files you worked on, a conflict will arise when you rebase. 
[Resolving a merge conflict](https://help.github.com/articles/resolving-a-merge-conflict-using-the-command-line) is a good guide to help with this. 

7. After committing the resolution, you can run below command to finish the rebase.
```git rebase --continue``` 

8. If you want to cancel, like if you make some mistake in resolving the conflict, you can always do 
```
git rebase --abort
```
9. Push your local branch to your fork on GitHub by running 
```
git push origin your-change-name
```
10. Then, go to the [original project page](https://github.com/manimaran96/Spell4Wiki/) and make a pull request. Select your fork/branch and use ```master``` as the base branch.

Wait for feedback on your pull request and be ready to make some changes.

If you have any questions, don't hesitate to open an issue or contact by <a href="#communication">communication details</a>. 
Please also ask before you start implementing a new big feature.

## Communication

* Join Telegram Group - [Spell4Wiki Telegram Group](https://t.me/spell4wiki) 
* If you want to get in touch with the developer you can send an email to <a href="mailto:manimarankumar96@gmail.com">manimarankumar96@gmail.com</a> or [@manimarank](https://t.me/manimaran_k) in Telegram.
* Feel free to post suggestions, changes, ideas etc. on GitHub or Telegram!

## Donation for Development
* To support developers, you can do a donation : [Donation details](https://github.com/manimaran96/Spell4Wiki/blob/master/docs/DONATION.md#donation--spell4wiki-app)
