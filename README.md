# Course Projects Overview

This repository contains several assignments focused on game logic implementation, server-client communication, and object-oriented design. Below is an overview of the relevant projects:

## 1. TFTP Server and Client Implementation

### General Description:
In this assignment, you will implement an extended **TFTP (Trivial File Transfer Protocol)** server and client. The TFTP server allows multiple users to upload and download files, as well as announce when files are added or deleted. The communication between the server and clients is performed using a binary protocol supporting upload, download, and lookup of files.

### Key Features:
- **Login Command**: A client specifies a unique username upon connecting.
- **File Transfer Commands**: Uploading, downloading, and looking up files.
- **Bi-directional Message Passing**: Client-server and client-to-client communication.
- **Thread-per-client Model**: The server uses one thread for each client.

### Technologies:
- **C++**: TCP communication, Multi-threading, Server-Client Model

---
## 2. Set Card Game Implementation

### General Description:
In this assignment, you will implement a simple version of the **Set Card Game**. The game involves a deck of 81 cards, each with four features (color, number, shape, shading). The goal is for players to find sets of three cards where the features are either all the same or all different.

### Key Features:
- **Card Features**: Cards have four features (color, number, shape, shading).
- **Legal Set**: A set of 3 cards is legal if each feature is either all the same or all different.
- **Game Logic**: Implement the game logic to check if selected cards form a valid set.

### Technologies:
- **C++**: Game Logic, UI Handling, Event Processing

---

## Technologies Used Across Projects:
- **C++**: Object-Oriented Programming, Multi-threading, Memory Management, System Programming
- **Networking**: TCP/IP (TFTP Protocol)
- **Game Logic**: Set Card Game, Warehouse Simulation




---


## 3. Warehouse Management System (C++)

### General Description:
This assignment involves designing an object-oriented **Food Warehouse Management System** using C++. The system simulates warehouse operations, handling volunteers, customers, and orders, while focusing on memory management and avoiding memory leaks.

### Key Features:
- **Volunteer Roles**: Different roles for volunteers who manage orders and interact with customers.
- **Order Handling**: System handles customer orders and simulates the warehouse's daily operations.
- **Efficient Memory Management**: Emphasizes the Rule of 5 and proper memory handling to avoid memory leaks.

### Technologies:
- **C++**: Object-Oriented Programming, Memory Management, Standard Data Structures

---




## Conclusion:
These projects provide valuable hands-on experience in C++ and cover a wide range of concepts from server-client communication to game logic and object-oriented system design. Each project emphasizes practical application and efficient programming techniques in different domains.
