# ChromeOS Annotated Field Management Dashboard

This project is a lite ChromeOS dashboard focusing on annotated fields (asset id, location, and user).  The aim is to provide core functionality currently missing from the official admin toolset, such as the ability to bulk update many devices' annotated fields and easily visualize breakdowns across annotated fields.

## Requirements
- Maven 3
- Java 1.8+


## Installation
First clone the repository and checkout `master`.  Create a new directory `/src/main/resources`, and create two new files inside named `api_key.txt` and `client_info.json`.  Place your ____________ inside of `api_key.txt` and place _________ inside of `client_info.json`.  You can now build and run the project with `mvn package appengine:run`.

## Features

### Secure Login
The project uses OAuth2 to gain permission to display and update a user's devices.
![A flow chart diagram depicting the website's login flow.](/demo/auth-flow.png)

### Pagination
The main landing page of the website is a simple table showing all of the user's devices in a paginated view.  Users can change the pagination size or scroll through pages to see more devices, which are loaded as needed.  There is also a caching system to ensure devices already fetched do not need to be fetched again.
![The main landing page of the website.](/demo/pagination.png)

### Aggregation & Visualization
Users can aggregate their devices by an arbitrary number of annotated fields.  The aggregated data is provided in two formats: as a simple table, allowing for the data to be easily parsed and understood, and as a series of nested pie charts in the side pane, which can potentially provide visual insights as to how a user's devices are distributed.  
![Aggregating by location and user.](/demo/aggregation.png)

### Bulk Updating
Users can update the annotated fields of an arbitrary number of devices with the press of a single button.  To determine which fields the user is trying to update, we look at what their current aggregation is.  For example, if they are currently aggregating by Location and User and attempt to bulk update, we assume they wish to update the location and user of the selected devices.  A loading bar is displayed while the server completes the requested update, and then the result of the update is displayed to the user: either that everything went as planned and all the devices were updated, or that something went wrong along with a list of devices which could not be updated.  The server utilizes parallelization to update the devices in a timely manner.
![Confirmation modal for updating devices](/demo/update-init.png)
![Success notification after successfully updating devices](/demo/update-completed.png)

### Accessibility
Screen reader accessibility was an important concern for the website.  We wanted to ensure administrators accessing the website via a screen reader could have a streamlined and painless experience.  To this end, we made sure all essential inputs on the website were easily reachable via keyboard and clearly described their purpose via aria-labels.  We also sought to remove repetition in the screen reader in some locations where it would read certain information twice.
