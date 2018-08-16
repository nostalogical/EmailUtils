# Email Domains

### Overview

Given an input of email addresses, ideally from a text file, this program will output the top 10 most commonly occuring email domains along with their count numbers.

### Using Project

Clone the repository and open it locally as a gradle project. 

Running the main() function in the EmailDomainsApplication class will read in a list of email addresses and process them. The processing will extract the domain name (e.g. from "test@gmail.com", "gmail.com" is the domain) and count how often it occurs in the list. The list of domains along with their frequency count will then be sorted, first by frequency and then alphabetically. The top 10 (or less) domains and their counts will then be outputted to the console.

By default the application will read in the contents of the file "input_email.txt" from the same directory, which comes supplied with a list of 200 emails. The input form is determined in the main() function in EmailDomainsApplication, to use another source the function can be altered. The output in the console is handled in the function displayOutput(), but be aware if altering the output that truncating the results to the top 10 happens in service class.

There are a series of JUnit tests in the EmailDomainsTest class which describe and confirm the expected behaviour of the service. 