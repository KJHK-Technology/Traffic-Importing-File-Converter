# Traffic-Importing-File-Converter
Used to convert the scheduled traffic into WideOrbit Language

Instructions:
1. Download the sheet in the KJHK traffic spreadsheet as a tsv (tab separated values).
   
   Note: make sure the sheet titled "downloadMe" is not RED indicating that there was an error.
   If the sheet appears red and there is an error, check that all your values throughout your sheet
   are value. Go to Promos sheet --> click "Check all Values" (or delect invalids to delete the offenders,
   or "autofix invalids" to attempt to repair the invalid entries based on info in the promo sheet).
2. Run this program on the reception computer (Or any internally located computer).
3. Click the "Select .tsv file" button to select the file you have downloaded.
  
   Note: If this is not your first time running the program, at startup it will try to do some work
   for you. It will look in the directory where you selected a tsv LAST time for the MOST RECENTLY 
   EDITED tsv file. It will display the file it found, the date it was edited, and ask if you would
   like to load that file. 
   
   Upon selecting a file, the parsed days for the traffic is displayed in 7 date pickers. You may edit
   these dates to fit your needs. The checkboxes next to each date allow you to exclude dates as you 
   see fit. 
   
   To indicate that a date has been modified/not yet written to Wide Orbit dir, it is RED. 
   
4. Select the Wide Orbit directory where traffic is expected. Upon setting the first time, you should not have to
   ever change this. HOWEVER, if this is a network location, and the network location is unreachable (or this
   directory location no longer exists), the output directory will change to a place it DOES have access to. 
   Therefore, it is a good idea to be familiar with what the output directory SHOULD be, and take appropriate action 
   to gain access to the network and re-set the output location. 
   
5. Once you have ensured that the dates are what you want, and the output location is correct, click "Go".

   To indicate that files have been written (but not yet consumed) the written dates will be displayed YELLOW.
   
   As WideOrbit consumes these files, the displayed date corresponding to the consumed file will turn GREEN.
   Information is also displayed in the Info box. 
   
While modifying the dates for traffic, having the "auto increment from this date" checkbox checked will auto fill the 
6 days below the first date to the 6 days that follow the first. 
