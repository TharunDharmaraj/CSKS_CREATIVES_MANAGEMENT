# CSKS_CREATIVES_MANAGEMENT
Admin and Employee Management Android Application
Android App Designed to Manage Employees and get a Overview of the Financials of [CSKS Creatives](https://cskscreatives.com/)

**User Roles:
- Admin
- Employee

**Previledges - Admin:**
- Add a Employees, Clients
- Create a task, assign it to respecitive Clients
- Assign a task to specific employee (Employee Will get Notified)
- ReAssign a task to other employee (Both Employees Will get Notified)
- Add comments to the task (Employee Will get Notified)
- Task Fields:
       - Task Title
       - Task Decription
       - Task Cost
       - Task Estimate
       - Task Priority etc.. (Similar to Jira)
- See the progress of the task through task status and Change status of the tasks, Task Status Types:
       - Backlog
       - In Progress
       - In Review
       - Revision 1
       - Revision 2 ....
       - Revision N
       - Blocked
       - Completed
- Add project cost of a task, when client pays, Mark the task as either FULLY_PAID or PARTIALLY_PAID, depending on the client's payment
       - Not Paid
       - Partially Paid ('N' Partial Payments equalling to Total Cost, will lead to Fully Paid)
       - Fully Paid
**- View the Overall Finances of the Admin by Year, Month
- View the Overall Financials of the Client by Year, Month**
- Track the Preformance of Employees, through Time Taken, In seperate Screens, Filter Accordingly
- Approve / Reject Employee Leave Requests
- Search / Filter through tasks by Paid Status, Task Status, Task Description etc...
- Easily Manage Employees, while Having a look at the Financials
- Previledge to Edit the Created tasks
- Assign Priority to the tasks
       - Critical
       - High
       - Medium
       - Low
- Receivies Notification when any of the conditions is met
      - Employee requests for a leave
      - Employee changes the task status
      - Employee Comments inside task

**Previledges - Employee**
- View the current Assigned Tasks, pick task with High Priority
- View the already completed tasks
- Change task statuses
- Comment inside tasks
- Request Leave, re request leave, if rejected
- Withdraw already applied future leave requests
- Can only view the fields inside tasks, except changing task status (Fields Related to cost is hidden to Employee)
- Receive Notifications when any of the conditions is met
      - Admin Approves a leave
      - Admin Rejects a leave
      - Admin Assigns a Task
      - Admin Comments inside task, assigned to that employee



Cloud functions repo for the same: https://github.com/TharunDharmaraj/CSKS_CREATIVES_CLOUD_FUNCTIONS
