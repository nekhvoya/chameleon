# Chameleon

This is a small library comparing images. 
It's goal is to provide an easy comparison tool allowing to verify correctness of images.

## Use cases

This library can be used for automation testing of the GUI components.
It can be easily integrated with any e2e tests written in Java to compare the screenshots of the UI state at different stages of test execution.

## How to configure

All one needs to do is 
* to set paths to the reference and test results directories `chameleon.properties` file
* implement taking a screenshot at the end of each test and save it (the static method `saveScreenshot()` can be used)
* call `compare()` at the end of the test run (in the `AfterAll` hook)
* save reference files in the configured reference directory

After the completion of the image comparison, diff files will be generated for the images that had deviations compared to the reference images.

## How does it work
After the execution of each test (certain test steps) a screenshot is made. The screenshot represents the current UI state.
After the completion of the test run, image analysis starts. Newly taken screenshots are compared against the references.
If the screenshot is different from the reference, the result of the comparison is considered failed and a diff image is created highlighting all the difference between the screenshot and the reference.
