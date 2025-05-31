# hvfilter
> Kotlin/Java Project
> --------------------
> Consists into a filter intendet to help electricians who deal with high voltage to identify in which structure is the leak of short-circuit current.
> # CSV Data Filtering App for Feeders
> This Android application allows filtering of data loaded from a CSV spreadsheet containing information about feeders, directions, structures, types, and distances (KM Local). Users can select a feeder, direction, enter a distance, and obtain filtered data based on these inputs.
>
> # Features
> CSV Data Loading: The app reads a CSV file located in res/raw/dados.csv, which includes the columns:
>
> - LDAT (Feeder)
> 
> - KM
>
> - Structure
>
> - Type
>
> - KM Local
>
> Feeder Selection Spinner: Users select a feeder from unique feeder options extracted from the spreadsheet.
>
> Direction Selection Spinner: After selecting a feeder, users choose the related direction dynamically populated based on the feeder.
>
> Distance Input: Users enter a numeric value for the distance to be used in filtering.

> Data Filtering:

> Results are filtered considering the feeder, selected direction, and entered distance.

> Only records where the KM Local value shares the same integer part as the input distance are displayed.

> Filtering accounts for direction to adjust the maximum distance calculation according to defined rules.

> Project Structure
> MainActivity.kt: Contains the main logic for:

> Loading and storing CSV data.

> Populating the spinners with filtered options.

> Executing data filtering based on user input.

> Displaying filtered results.

> activity_main.xml: Layout with:

> Spinner for feeder selection.

> Spinner for direction selection.

> Text input for distance entry.

Button to execute filtering.

TextView to display results.

dados.csv: CSV file containing spreadsheet data, placed inside res/raw/ folder.

How to Use
Clone or download the project and open it in Android Studio.

Make sure the dados.csv file is located in the res/raw/ folder.

Build and run the app on an Android device or emulator.

Select a feeder from the first spinner.

Choose a direction from the second spinner, which updates automatically.

Enter a numeric distance value in the input field.

Tap the Filter button to view data filtered according to your selections.

Dependencies
Standard Android SDK

No additional external libraries were used for CSV handling (Java’s built-in BufferedReader is used).

Possible Improvements
Add error handling for invalid or missing CSV data.

Implement pagination or scrolling for displaying large result sets.

Enhance UI with clearer visual feedback during filtering.

Allow dynamic spreadsheet import by users.

Contact
For questions, suggestions, or contributions, please contact:
Your Name / Contact Info Here
