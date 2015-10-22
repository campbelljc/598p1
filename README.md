# README #

This repository hosts the code and data for COMP-598 Fall 2015, Mini Project 1, by Jian (Ethan) Li, Yann Aublet-Longpré, and Jonathan Campbell.

### Set up the Eclipse Workspace ###

The linear regression code is written in Java as an Eclipse project.

Open Eclipse, open the root folder of the repository (the default folder name is comp598project1) as a new Eclipse workspace.

Once the workspace is loaded, go to File -> Import and select "Existing Projects into Workspace" under "General". Select the folder "MiniProject1" and import.

### Part 1 and Part 2 ###

The entry for Part 1 and Part 2 can be found in the file Part1.java and Part2.java, in the folder/package regression.

### Running Sentiment Analysis ###

The language model file (stanford-corenlp-3.5.2-models.jar)for the Stanford CoreNLP library is too big in size, therefore not included in the code. Please visit the offical website, download the file and put it it the ./lib/ folder.

The Sentiment Analysis tool can be found in the folder/package "preprocessing" of the Eclipse project.

### Running the crawler ###

The web crawler requires Python 2.7+, as well as the Spynner Python library and its dependencies: PyQt (v4.43+), libxml2, libxslt, lxml, autopy, Xtest (Linux only), and pybloom. All these libraries (perhaps except PyQt, which might have to be downloaded from the PyQt website depending on the operating system) can be installed using the pip command in Python.

Once everything is installed, a simple call to "python crawler.py" will start the crawler which will begin crawling the CBC news site starting from the main page (http://www.cbc.ca/news/).

### Running the combine.py script ###

This script will load all the article metadata from the data/ folder that the crawler creates, and parse and transform the metadata into the features for the linear regression algorithm to use. It will save all features into a cbc.csv file in the same directory. The script requires the unicodecsv Python library to be installed.
