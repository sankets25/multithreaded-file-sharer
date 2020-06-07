# Multi-threaded file sharer

A client-server architecture based multi-threaded file sharer coded in Java.

• Client chooses a unique username.
• Server forks a thread for every client which connects.
• Client adds new files to his/her the sending directory.
• Other clients receive the new file, or an update if the file exists.
