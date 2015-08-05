
Blackberry-Sample-App
=====

Sample app and documentation for the Blackberry Federal SDK using Quarry API.

Requirements
-----------

The list below outline the predominant open source technologies:

*	Blackberry Email and MDS Services Simulators 4.1.2
*	Blackberry Java SDK 7.1.0.10
*	Eclipse SDK Version: 3.7.2

Instructions (Windows 7)
-----------
1. Install the softwares listed in the Requirements section. NOTE: install Blackberry Email and MDS Services Simulators 4.1.2 in a location not requiring administrative rights like C:\ or the simulator will not have internet connectivity 
2. Create a new Blackberry project in Eclipse and configure the project to run Java SDK 7.1.0.10
3. Add the [DOL Blackberry-Sample-App](https://github.com/USDepartmentofLabor/Blackberry-Sample-App/issues) source code to the Eclipse project path
4. Open src\com\rim\samples\device\httpdemo\HTTPDemo.java and add your Quarry API key to CONTENTYPE_X_APIKEY variable 
5. Open the Blackberry Email and MDS Services Simulators 4.1.2 the MDS folder. Run (run.bat if using windows)
6. Return to Eclipse and run the simulator
7. When the simulator loads, you can navigate to the HTTPdemo's app and enter the DOL URL for data connectivity. 


Licenses
-----------

All of the code in this repository is public domain software.


Contact Information
-----------

All questions, comments, help and suggestions can be submited to the [DOL Blackberry-Sample-App](https://github.com/USDepartmentofLabor/Blackberry-Sample-App/issues) Github repository.


