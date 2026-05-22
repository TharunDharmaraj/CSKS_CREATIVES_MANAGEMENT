# 📱 CSKS Creatives Management  
**Admin and Employee Management Android Application**  
Designed to manage employees, tasks, and finances for [CSKS Creatives](https://cskscreatives.com/)

---

## 📌 Features At a Glance

- 🔐 Role-based access: Admin / Employee
- 📬 Real-time notifications with Firebase Cloud Messaging
- 🧾 Financial dashboards: View payments, dues, and summaries
- ⏱️ Employee performance tracking via task time logs
- 🗂️ Task status workflow (similar to Jira): Backlog → In Progress → Review → Revision → Completed
- 🧮 Payment tracking with support for Partial / Full payments
- 📅 Leave request & approval system with support for Full Day / Half Day requests
- 🔍 Filter and search tasks by status, priority, or keywords
- 💾 Offline-first with persistent login via local cache
- 📈 Client-wise and Admin-wise monthly/yearly financial insights
- 🚀 Optimized performance with pagination (7 records/fetch) and manual "Force Fetch" override

---0

## 👥 User Roles
> Login is required for both roles. Session is cached locally until logout.

- **Admin**
- **Employee**

---

## 🔐 Admin Privileges

### ✅ Employee & Client Management
- Add, edit, and manage employees and clients.

### ✅ Task Management
- Create tasks and assign them to specific clients and employees.
- Reassign tasks to different employees. Notifications sent to both old and new assignees.
- Add comments to tasks (assignee receives notification).
- Edit all task details.

### 🎯 Task Fields Include:
- Task Title  
- Task Description  
- Estimated Duration  
- Task Cost  
- Task Priority (Critical, High, Medium, Low)  
- Task Status

### 📈 Task Status Flow (Jira-like):
- `Backlog` → `In Progress` → `In Review` → `Revision N` → `Completed`
- `Blocked` can occur at any stage

### 💰 Payment Tracking
- Mark tasks as:
  - `Not Paid`
  - `Partially Paid` (Track multiple partial payments)
  - `Fully Paid` (Automatically marked when total received)

### 📊 Financial Overview
- View **overall finances** (Year-wise / Month-wise)
- View **client-specific financials** (Year-wise / Month-wise)

### 🧑‍💼 Employee Performance Tracking
- Track time taken per task
- Filter and analyze performance data

### 🔍 Advanced Search & Filters
- Search and filter tasks by:
  - Paid Status
  - Task Status
  - Priority
  - Description

### 🗓️ Leave Management
- Approve or reject employee leave requests
- View leave duration (Full Day vs. Half Day) with visual badges
- Track weighted leave totals (Half Day = 0.5, Full Day = 1.0)
- View monthly summaries of actual days taken

### 🔔 Notifications
- When:
  - Employee changes task status
  - Employee comments on a task
  - Employee requests/re-requests/withdraws leave

---

## 👨‍💼 Employee Privileges

### 📋 Task Interaction
- View current and completed tasks
- View Time Taken for Tasks
- Change task status
- Comment on tasks

### 🗓️ Leave Management
- Request leave with duration selection (Full Day / Half Day)
- Re-request if rejected
- Withdraw future leave requests
- View real-time status and duration badges

### 🔔 Notifications
- When:
  - Admin approves/rejects leave
  - Admin assigns a task
  - Admin comments on assigned task

---

## 🧠 App Architecture

> Built using **Layered Architecture** for clean separation of concerns:

### 🔹 UI Layer
- Jetpack Compose-based Screens
- App Navigation
- ViewModels for state management

### 🔹 Domain Layer
- Use Cases
- Business Models

### 🔹 Data Layer
- Repositories
- Data Sources:
  - **Remote:** Firebase Firestore  
  - **Local:** RoomDB (Offline-first architecture)
- **Optimization Logic:**
  - Default paginated fetching (7 records) to reduce Firestore read costs and improve UI responsiveness.
  - Manual "Force Fetch" override (with 10s cooldown) to bypass cache/pagination when immediate data sync is required.

### 🧩 Dependency Injection
- Managed using **Dagger Hilt**

---

## 🔗 Cloud Functions Repository

> Cloud backend logic handled via Firebase Functions  
> Handles data syncing, notifications, and performance optimization

👉 [CSKS_CREATIVES_CLOUD_FUNCTIONS (GitHub)](https://github.com/TharunDharmaraj/CSKS_CREATIVES_CLOUD_FUNCTIONS)

---

## 🚀 How to Run

Follow these steps to run the project locally on your machine:

### 🔧 Prerequisites

- Android Studio Giraffe or later
- Firebase account
- Firebase Project with **Billing Enabled**  
  _(Required for Cloud Functions, Firestore Triggers, and Notifications)_

### 🛠️ Steps

#### 1. Clone the Repository
```bash
git clone https://github.com/your-username/csks-creatives-management.git
cd csks-creatives-management

Set Up Firebase

    Go to Firebase Console

    Create a new Firebase project or use an existing one

    Enable Billing on your Firebase project

    Enable the following Firebase services:

        🔥 Firestore Database

        🔐 Firebase Authentication

        📲 Cloud Messaging

4. Add google-services.json

    In Firebase Console:
    Go to Project Settings > General

    Download google-services.json for your Android app

    Place it in the root of your project’s /app folder

    Clone the [CSKS_CREATIVES_CLOUD_FUNCTIONS (GitHub)](https://github.com/TharunDharmaraj/CSKS_CREATIVES_CLOUD_FUNCTIONS) repo, deploy it in Cloud Console

    Commands to deploy the Cloud Function, by navigating to the repo folder
    For 1st Time Setup:
          - npm install -g firebase-tools
          - npm install firebase-admin firebase-functions
          - firebase init firestore
          - firebase init functions

      After that:
            - firebase init
            - firebase login
            - firebase deploy --only functions  

5. Run the App

```

---

> For business inquiries or demo requests, please contact [CSKS Creatives](https://cskscreatives.com/)
