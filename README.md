# 📱 CSKS Creatives Management  
**Admin and Employee Management Android Application**  
Designed to manage employees, tasks, and finances for [CSKS Creatives](https://cskscreatives.com/)

---

## 📌 Features At a Glance

- 🔐 Role-based access (Admin / Employee)
- 📬 Real-time Notifications
- 🧾 Financial Insights (Admin & Client)
- ⏱️ Employee Performance Tracking
- 🗂️ Task Search, Filters, and Status Workflow
- 🗓️ Comprehensive Leave Management
- 💾 Session Persistence with Offline Support

---

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

### 🔔 Notifications
- When:
  - Employee changes task status
  - Employee comments on a task
  - Employee requests/re-requests/withdraws leave

---

## 👨‍💼 Employee Privileges

### 📋 Task Interaction
- View current and completed tasks
- Change task status (except financial fields)
- Comment on tasks

### 🗓️ Leave Management
- Request leave
- Re-request if rejected
- Withdraw future leave requests

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
- ViewModels for state management

### 🔹 Domain Layer
- Use Cases
- Business Models

### 🔹 Data Layer
- Repositories
- Data Sources:
  - **Remote:** Firebase Firestore  
  - **Local:** RoomDB (Offline-first architecture)

### 🧩 Dependency Injection
- Managed using **Dagger Hilt**

---

## 🔗 Cloud Functions Repository

> Cloud backend logic handled via Firebase Functions  
> Handles data syncing, notifications, and performance optimization

👉 [CSKS_CREATIVES_CLOUD_FUNCTIONS (GitHub)](https://github.com/TharunDharmaraj/CSKS_CREATIVES_CLOUD_FUNCTIONS)

---

> For business inquiries or demo requests, please contact [CSKS Creatives](https://cskscreatives.com/)
